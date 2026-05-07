package xyz.ytora.sqlux.sql.stage.update;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.Connector;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlRewriteContext;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.EntityMetas;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.orm.OrmUpdateValues;
import xyz.ytora.sqlux.orm.filler.OrmFieldFiller;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.model.*;
import xyz.ytora.sqlux.sql.stage.EntityWhereAppender;
import xyz.ytora.sqlux.sql.stage.TerminationStage;
import xyz.ytora.sqlux.translate.DialectFactory;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.*;
import java.util.function.Consumer;

/**
 * UPDATE 阶段。
 *
 * <p>用于构造 UPDATE 语句；该阶段可继续追加 JOIN、SET 和 WHERE，并可作为终止阶段提交执行。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class UpdateStage<T> implements TerminationStage<Integer> {

    private final UpdateQuery query;

    private boolean batchMode;

    private List<T> batchEntities = Collections.emptyList();

    /**
     * 创建 UPDATE 阶段。
     *
     * <p>示例：{@code SQL.update(User.class)} 会创建面向 {@code user} 表的 UPDATE 阶段。</p>
     *
     * @param table 更新目标表对应的实体类型；入参用于解析表名和默认别名
     */
    public UpdateStage(Class<T> table) {
        this.query = new UpdateQuery(table);
    }

    /**
     * 追加 INNER JOIN 子句。
     */
    public UpdateStage<T> join(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.INNER_JOIN, joinTable, null, on);
    }

    /**
     * 追加 INNER JOIN 子句并设置别名。
     */
    public UpdateStage<T> join(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.INNER_JOIN, joinTable, alias, on);
    }

    /**
     * 追加 LEFT JOIN 子句。
     */
    public UpdateStage<T> leftJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.LEFT_JOIN, joinTable, null, on);
    }

    /**
     * 追加 LEFT JOIN 子句并设置别名。
     */
    public UpdateStage<T> leftJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.LEFT_JOIN, joinTable, alias, on);
    }

    /**
     * 追加 RIGHT JOIN 子句。
     */
    public UpdateStage<T> rightJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.RIGHT_JOIN, joinTable, null, on);
    }

    /**
     * 追加 RIGHT JOIN 子句并设置别名。
     */
    public UpdateStage<T> rightJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.RIGHT_JOIN, joinTable, alias, on);
    }

    /**
     * 添加 SET 赋值项：{@code column = value}。
     */
    public <R> UpdateStage<T> set(ColFunction<R, ?> column, Object value) {
        ensureNotBatchMode();
        query.addAssignment(new Assignment(ColumnRef.from(column), value, false));
        return this;
    }

    /**
     * 添加 SET 字段赋值项：{@code leftColumn = rightColumn}。
     */
    public <L, R> UpdateStage<T> set(ColFunction<L, ?> leftColumn, ColFunction<R, ?> rightColumn) {
        ensureNotBatchMode();
        if (rightColumn == null) {
            query.addAssignment(new Assignment(ColumnRef.from(leftColumn), null, false));
            return this;
        }
        query.addAssignment(new Assignment(ColumnRef.from(leftColumn), ColumnRef.from(rightColumn), false));
        return this;
    }

    /**
     * 添加 SET 原始表达式值：{@code column = rawSql}。
     */
    public <R> UpdateStage<T> setRaw(ColFunction<R, ?> column, String rawSql) {
        ensureNotBatchMode();
        query.addAssignment(new Assignment(ColumnRef.from(column), rawSql, true));
        return this;
    }

    /**
     * 为实体对象中所有非空字段对应的 SET 赋值项。
     */
    public UpdateStage<T> set(T entity) {
        ensureNotBatchMode();
        if (!query.getTable().getTableClass().isInstance(entity)) {
            throw new IllegalArgumentException("UPDATE实体对象类型必须匹配目标表: "
                    + query.getTable().getTableClass().getName());
        }
        OrmFieldFiller.fillBeforeUpdate(entity);
        OrmUpdateValues updateValues = OrmMapper.readUpdateValues(entity);
        for (Map.Entry<String, Object> entry : updateValues.getValues().entrySet()) {
            query.addAssignment(new Assignment(ColumnRef.of(query.getTable().getTableClass(), entry.getKey()),
                    entry.getValue(), false));
        }
        if (updateValues.hasVersionLock()) {
            ColumnRef versionColumn = ColumnRef.of(query.getTable().getTableClass(), updateValues.getVersionColumn());
            query.addAssignment(new Assignment(versionColumn, new ColumnIncrement(1), false));
            query.setVersionLock(versionColumn, updateValues.getVersionValue());
        }
        return this;
    }

    /**
     * 批量修改，为实体对象中所有非空字段对应的 SET 赋值项。
     *
     * <p>该模式面向“按实体主键逐条更新”的场景。框架会使用实体主键列生成每一条
     * {@code WHERE} 条件，非空普通字段生成 {@code SET} 子句，乐观锁字段继续沿用
     * 单对象更新时的版本递增和版本条件逻辑。</p>
     *
     * <p>批量模式要求所有实体的可更新字段集合一致，这样才能复用同一条批处理 SQL。
     * 语法上推荐与 {@link #submitBatch()} 搭配：{@code SQL.update(User.class).set(users).submitBatch()}。</p>
     *
     * @param entities 待批量更新的实体集合；入参为 {@code null} 时不追加任何批量数据
     * @return 当前 UPDATE 阶段对象；出参用于继续链式调用公共 {@code where(...)} 条件
     */
    public UpdateStage<T> set(List<T> entities) {
        if (!query.getAssignments().isEmpty()) {
            throw new IllegalStateException("批量UPDATE不能与单条SET赋值混用");
        }
        if (entities == null) {
            throw new IllegalArgumentException("UPDATE批量实体集合不能为空");
        }
        this.batchMode = true;
        this.batchEntities = new ArrayList<>(entities);
        return this;
    }

    /**
     * 添加 WHERE 条件。
     */
    public UpdateStage<T> where(Consumer<ExpressionBuilder> whereExpr) {
        if (whereExpr != null) {
            ExpressionBuilder builder = new ExpressionBuilder(query.getContextHolder());
            whereExpr.accept(builder);
            query.setWhere(builder.toExpression());
        }
        return this;
    }

    /**
     * 根据实体对象中的非空字段添加 WHERE 条件。
     */
    @SafeVarargs
    public final <E extends AbsEntity> UpdateStage<T> where(E... whereObjs) {
        ExpressionBuilder builder = new ExpressionBuilder(query.getContextHolder());
        if (whereObjs != null) {
            for (E whereObj : whereObjs) {
                EntityWhereAppender.append(builder, whereObj);
            }
        }
        query.setWhere(builder.toExpression());
        return this;
    }

    /**
     * 执行 UPDATE，并返回影响行数。
     */
    @Override
    public Integer submit() {
        if (isBatchMode()) {
            int total = 0;
            for (int affected : submitBatch()) {
                total += affected;
            }
            return total;
        }
        return SQL.getSqluxGlobal().getExecutor().update(toSql());
    }

    /**
     * 批量执行 UPDATE，并返回每一条更新语句的影响行数。
     *
     * <p>该方法会为每个实体生成一组参数，并复用同一条 UPDATE SQL 执行 JDBC batch。
     * 如果实体之间的可更新字段集合不同，无法共享同一条 SQL，则会直接抛出异常提示调用方拆分批次。</p>
     *
     * @return 每一条 UPDATE 的影响行数；当前没有批量实体时返回空数组
     */
    public int[] submitBatch() {
        if (!isBatchMode()) {
            return new int[]{SQL.getSqluxGlobal().getExecutor().update(toSql())};
        }
        if (batchEntities.isEmpty()) {
            return new int[0];
        }
        BatchUpdateSql batch = buildBatchSql();
        if (batch == null) {
            return new int[0];
        }
        return SQL.getSqluxGlobal().getExecutor().updateBatch(batch.getSqlResult(), batch.getBatchParams());
    }

    /**
     * 翻译 UPDATE 语句，不执行数据库操作。
     */
    public SqlResult toSql() {
        if (isBatchMode()) {
            BatchUpdateSql batch = buildBatchSql();
            if (batch == null) {
                throw new IllegalStateException("UPDATE批量模式缺少实体数据");
            }
            return batch.getSqlResult();
        }
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.UPDATE, query));
        }
        return DialectFactory.getDialect(SQL.getSqluxGlobal().getDbType()).updateTranslator().translate(query);
    }

    /**
     * 使用指定数据库类型翻译 UPDATE 语句，不执行数据库操作。
     */
    public SqlResult toSql(DbType dbType) {
        if (isBatchMode()) {
            BatchUpdateSql batch = buildBatchSql(dbType);
            if (batch == null) {
                throw new IllegalStateException("UPDATE批量模式缺少实体数据");
            }
            return batch.getSqlResult();
        }
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.UPDATE, query));
        }
        return DialectFactory.getDialect(dbType).updateTranslator().translate(query);
    }

    /**
     * 按指定 JOIN 类型追加 JOIN 子句。
     */
    private UpdateStage<T> join(JoinType joinType, Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        if (on == null) {
            throw new IllegalArgumentException("ON条件不能为空");
        }
        String joinAlias = alias == null || alias.trim().isEmpty()
                ? query.getContextHolder().addTable(joinTable)
                : query.getContextHolder().addTable(joinTable, alias);
        ExpressionBuilder builder = new ExpressionBuilder(query.getContextHolder());
        on.accept(builder);
        query.addJoin(new JoinClause(joinType, new TableRef(joinTable, joinAlias), builder.toExpression()));
        return this;
    }

    /**
     * 判断当前 UPDATE 是否处于批量模式。
     *
     * @return 已设置批量实体时返回 {@code true}
     */
    private boolean isBatchMode() {
        return batchMode;
    }

    /**
     * 保证单条 SET 赋值模式不会与批量模式混用。
     */
    private void ensureNotBatchMode() {
        if (isBatchMode()) {
            throw new IllegalStateException("批量UPDATE不能再追加单条SET赋值");
        }
    }

    /**
     * 构建批量 UPDATE 所需的 SQL 和参数。
     *
     * @param dbType 数据库类型
     * @return 批量 UPDATE SQL；当前没有实体时返回 {@code null}
     */
    private BatchUpdateSql buildBatchSql(DbType dbType) {
        if (!isBatchMode()) {
            return null;
        }
        List<List<Object>> batchParams = new ArrayList<>();
        SqlResult firstSql = null;
        for (T entity : batchEntities) {
            SqlResult entitySql = toBatchEntitySql(dbType, entity);
            if (firstSql == null) {
                firstSql = entitySql;
            } else if (!firstSql.getSql().equals(entitySql.getSql())) {
                throw new IllegalStateException("UPDATE批量实体的可更新字段集合必须一致");
            }
            batchParams.add(entitySql.getParams());
        }
        return firstSql == null ? null : new BatchUpdateSql(firstSql, batchParams);
    }

    private BatchUpdateSql buildBatchSql() {
        if (!isBatchMode()) {
            return null;
        }
        List<List<Object>> batchParams = new ArrayList<>();
        SqlResult firstSql = null;
        for (T entity : batchEntities) {
            SqlResult entitySql = toBatchEntitySql(entity);
            if (firstSql == null) {
                firstSql = entitySql;
            } else if (!firstSql.getSql().equals(entitySql.getSql())) {
                throw new IllegalStateException("UPDATE批量实体的可更新字段集合必须一致");
            }
            batchParams.add(entitySql.getParams());
        }
        return firstSql == null ? null : new BatchUpdateSql(firstSql, batchParams);
    }

    /**
     * 将单个实体转换为批处理所需的 UPDATE SQL。
     *
     * @param dbType 数据库类型
     * @param entity 待更新实体
     * @return 单个实体对应的 UPDATE SQL
     */
    private SqlResult toBatchEntitySql(DbType dbType, T entity) {
        UpdateQuery batchQuery = buildBatchEntityQuery(entity);
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.UPDATE, batchQuery));
        }
        return DialectFactory.getDialect(dbType).updateTranslator().translate(batchQuery);
    }

    private SqlResult toBatchEntitySql(T entity) {
        UpdateQuery batchQuery = buildBatchEntityQuery(entity);
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.UPDATE, batchQuery));
        }
        return DialectFactory.getDialect(SQL.getSqluxGlobal().getDbType()).updateTranslator().translate(batchQuery);
    }

    /**
     * 基于单个实体构造批量 UPDATE 的结构化查询模型。
     *
     * @param entity 待更新实体
     * @return UPDATE 查询模型
     */
    private UpdateQuery buildBatchEntityQuery(T entity) {
        validateEntity(entity);
        OrmFieldFiller.fillBeforeUpdate(entity);
        OrmUpdateValues updateValues = OrmMapper.readUpdateValues(entity);

        UpdateQuery batchQuery = new UpdateQuery(query.getTable().getTableClass());
        batchQuery.getContextHolder().copyFrom(query.getContextHolder());
        for (JoinClause join : query.getJoins()) {
            batchQuery.addJoin(join);
        }

        Set<String> keyColumns = keyColumns();
        for (Map.Entry<String, Object> entry : updateValues.getValues().entrySet()) {
            if (keyColumns.contains(entry.getKey())) {
                continue;
            }
            batchQuery.addAssignment(new Assignment(ColumnRef.of(query.getTable().getTableClass(), entry.getKey()),
                    entry.getValue(), false));
        }
        if (updateValues.hasVersionLock()) {
            ColumnRef versionColumn = ColumnRef.of(query.getTable().getTableClass(), updateValues.getVersionColumn());
            batchQuery.addAssignment(new Assignment(versionColumn, new ColumnIncrement(1), false));
            batchQuery.setVersionLock(versionColumn, updateValues.getVersionValue());
        }
        if (batchQuery.getAssignments().isEmpty()) {
            throw new IllegalStateException("UPDATE批量实体缺少可更新字段");
        }

        batchQuery.setWhere(mergeWhere(query.getWhere(), buildKeyWhere(batchQuery, keyColumns, entity)));
        return batchQuery;
    }

    /**
     * 构造实体主键 WHERE 条件。
     *
     * @param batchQuery 当前批量 UPDATE 查询模型
     * @param keyColumns 主键列集合
     * @param entity 待更新实体
     * @return 主键 WHERE 条件
     */
    private ExpressionGroup buildKeyWhere(UpdateQuery batchQuery, Set<String> keyColumns, T entity) {
        ExpressionBuilder builder = new ExpressionBuilder(batchQuery.getContextHolder());
        for (String keyColumn : keyColumns) {
            Object keyValue = OrmMapper.readColumnValue(entity, keyColumn);
            if (keyValue == null) {
                throw new IllegalArgumentException("UPDATE批量实体主键不能为空: " + keyColumn);
            }
            builder.eq(ColumnRef.of(query.getTable().getTableClass(), keyColumn), keyValue);
        }
        return builder.toExpression();
    }

    /**
     * 合并公共 WHERE 条件和实体主键条件。
     *
     * @param commonWhere 调用方追加的公共 WHERE 条件
     * @param keyWhere 当前实体主键条件
     * @return 合并后的 WHERE 条件
     */
    private ExpressionGroup mergeWhere(ExpressionGroup commonWhere, ExpressionGroup keyWhere) {
        if (commonWhere == null || commonWhere.isEmpty()) {
            return keyWhere;
        }
        if (keyWhere == null || keyWhere.isEmpty()) {
            return commonWhere;
        }
        ExpressionGroup merged = new ExpressionGroup();
        merged.add(Connector.AND, commonWhere);
        merged.add(Connector.AND, keyWhere);
        return merged;
    }

    /**
     * 获取当前实体的主键列集合。
     *
     * @return 主键列集合
     */
    private Set<String> keyColumns() {
        return new LinkedHashSet<>(
                Arrays.asList(EntityMetas.get(query.getTable().getTableClass()).getKeyColumns())
        );
    }

    /**
     * 校验实体类型。
     *
     * @param entity 待更新实体
     */
    private void validateEntity(T entity) {
        if (!query.getTable().getTableClass().isInstance(entity)) {
            throw new IllegalArgumentException("UPDATE实体对象类型必须匹配目标表: "
                    + query.getTable().getTableClass().getName());
        }
    }

    /**
     * 批量 UPDATE 的 SQL 和参数封装。
     */
    private static final class BatchUpdateSql {

        private final SqlResult sqlResult;

        private final List<List<Object>> batchParams;

        private BatchUpdateSql(SqlResult sqlResult, List<List<Object>> batchParams) {
            this.sqlResult = sqlResult;
            this.batchParams = batchParams;
        }

        private SqlResult getSqlResult() {
            return sqlResult;
        }

        private List<List<Object>> getBatchParams() {
            return batchParams;
        }
    }
}
