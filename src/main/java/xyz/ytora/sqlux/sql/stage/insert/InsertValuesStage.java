package xyz.ytora.sqlux.sql.stage.insert;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlRewriteContext;
import xyz.ytora.sqlux.orm.EntityFieldMeta;
import xyz.ytora.sqlux.orm.EntityMetas;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.orm.filler.OrmFieldFiller;
import xyz.ytora.sqlux.orm.filler.OrmFillResult;
import xyz.ytora.sqlux.orm.id.OrmIdFillResult;
import xyz.ytora.sqlux.orm.id.OrmIdFiller;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.stage.TerminationStage;
import xyz.ytora.sqlux.sql.stage.select.AbsSelect;
import xyz.ytora.sqlux.translate.DialectFactory;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * INSERT VALUES 阶段。
 *
 * <p>该阶段是 INSERT 的终止阶段，调用 {@link #submit()} 会执行插入并返回影响行数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class InsertValuesStage implements TerminationStage<Integer> {

    private final InsertQuery query;

    private final List<Object> generatedKeyTargets = new ArrayList<>();

    /**
     * 创建 INSERT VALUES 阶段。
     *
     * <p>示例：{@code SQL.insert(User.class).into(User::getName)} 内部会创建该阶段，
     * 后续可继续调用 {@link #valuesRow(Object...)} 或 {@link #values(Object)}。</p>
     *
     * @param query INSERT 查询模型；入参保存目标表、插入字段和待插入行
     */
    public InsertValuesStage(InsertQuery query) {
        this.query = query;
    }

    /**
     * 添加一行待插入值。
     *
     * <p>示例：{@code into(User::getName, User::getAge).valuesRow("ytora", 18)}
     * 会生成一行参数 {@code ["ytora", 18]}。</p>
     *
     * @param values 当前行的字段值；入参顺序需要与 {@code into(...)} 中的字段顺序一致
     * @return 当前 INSERT VALUES 阶段对象；出参用于继续链式调用
     */
    public InsertValuesStage valuesRow(Object... values) {
        List<Object> row = new ArrayList<>();
        if (values != null) {
            row.addAll(Arrays.asList(values));
        }
        query.addRow(row);
        return this;
    }

    /**
     * 添加一个待插入实体对象。
     *
     * <p>该方法会按 {@code into(...)} 指定的字段，通过实体 getter 读取字段值；
     * 字段值实现 {@code IWriter} 时会先执行自定义写转换。执行 INSERT 后，如果 JDBC
     * 返回 generated keys，则支持的执行器会把主键回填到该实体对象。</p>
     *
     * <p>示例：{@code into(User::getName).values(user)} 会调用 {@code user.getName()}，
     * 并把返回值作为 INSERT 参数。</p>
     *
     * @param entity 待插入实体对象；入参不能为 {@code null}，字段读取依赖 getter
     * @return 当前 INSERT VALUES 阶段对象；出参用于继续链式调用
     */
    public InsertValuesStage values(Object entity) {
        OrmIdFillResult idFillResult = OrmIdFiller.fillBeforeInsert(entity);
        OrmFillResult fillResult = OrmFieldFiller.fillBeforeInsert(entity);
        appendAllColumns(entity);
        appendAutoColumns(idFillResult.getFilledColumns());
        appendAutoColumns(fillResult.getFilledColumns());
        query.addRow(OrmMapper.readInsertRow(entity, query.getColumns()));
        if (needDatabaseGeneratedKey(entity)) {
            generatedKeyTargets.add(entity);
        }
        return this;
    }

    /**
     * 批量添加待插入实体对象。
     *
     * <p>示例：{@code values(Arrays.asList(user1, user2))} 等价于连续调用
     * {@code values(user1).values(user2)}。</p>
     *
     * @param entities 待插入实体对象集合；入参为 {@code null} 时不添加任何行
     * @return 当前 INSERT VALUES 阶段对象；出参用于继续链式调用
     */
    public InsertValuesStage values(Iterable<?> entities) {
        if (entities == null) {
            return this;
        }
        for (Object entity : entities) {
            values(entity);
        }
        return this;
    }

    /**
     * 使用 SELECT 子查询作为插入来源。
     *
     * @param selectQuery SELECT子查询
     * @return 当前 INSERT VALUES 阶段对象
     */
    public InsertValuesStage select(AbsSelect selectQuery) {
        if (selectQuery == null) {
            throw new IllegalArgumentException("INSERT SELECT 子查询不能为空");
        }
        query.setSelectQuery(selectQuery.getQuery());
        return this;
    }

    /**
     * 执行 INSERT，并返回影响行数。
     *
     * <p>示例：{@code SQL.insert(User.class).into(User::getName).values(user).submit()}
     * 会先翻译 SQL，再调用当前注册的执行器执行；如果执行器支持 generated keys，会尝试回填主键。</p>
     *
     * @return 影响行数；出参来自当前 {@code SqlExecutor}
     */
    @Override
    public Integer submit() {
        return generatedKeyTargets.isEmpty()
                ? SQL.getSqluxGlobal().getExecutor().update(toSql())
                : SQL.getSqluxGlobal().getExecutor().update(toSql(), generatedKeyTargets);
    }

    /**
     * 批量执行 INSERT，并返回每一行的影响行数。
     *
     * <p>该方法会把多行 VALUES 翻译为单行 INSERT SQL，再通过执行器批量绑定每一行参数。
     * 语法仍然贴近 SQL：{@code values(...).submitBatch()} 表示把当前 VALUES 行作为批处理提交。</p>
     *
     * @return 每一行 INSERT 的影响行数
     */
    public int[] submitBatch() {
        return generatedKeyTargets.isEmpty()
                ? SQL.getSqluxGlobal().getExecutor().updateBatch(toBatchSql(), batchParams())
                : SQL.getSqluxGlobal().getExecutor().updateBatch(toBatchSql(), batchParams(), generatedKeyTargets);
    }

    /**
     * 翻译 INSERT 语句，不执行数据库操作。
     *
     * <p>示例：{@code into(User::getName).valuesRow("ytora").toSql()} 返回的 SQL
     * 类似 {@code INSERT INTO user (name) VALUES (?)}，参数为 {@code ["ytora"]}。</p>
     *
     * @return SQL翻译结果；出参包含 SQL 文本和有序参数
     */
    public SqlResult toSql() {
        return toSql(SQL.getSqluxGlobal().getDbType());
    }

    /**
     * 使用指定数据库类型翻译 INSERT 语句，不执行数据库操作。
     *
     * @param dbType 数据库类型；入参决定标识符引用等方言细节
     * @return SQL翻译结果
     */
    public SqlResult toSql(DbType dbType) {
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.INSERT, query));
        }
        return DialectFactory.getDialect(dbType).insertTranslator().translate(query);
    }

    /**
     * 翻译批处理使用的单行 INSERT 语句。
     *
     * @return 单行 INSERT SQL 翻译结果
     */
    private SqlResult toBatchSql() {
        return toBatchSql(SQL.getSqluxGlobal().getDbType());
    }

    /**
     * 使用指定数据库类型翻译批处理单行 INSERT 语句。
     *
     * @param dbType 数据库类型
     * @return 单行 INSERT SQL 翻译结果
     */
    private SqlResult toBatchSql(DbType dbType) {
        if (query.getSelectQuery() != null) {
            throw new IllegalStateException("INSERT SELECT 不支持批处理提交");
        }
        if (query.getRows().isEmpty()) {
            throw new IllegalStateException("INSERT缺少VALUES数据");
        }
        InsertQuery batchQuery = new InsertQuery(query.getTable().getTableClass());
        for (ColumnRef column : query.getColumns()) {
            batchQuery.addColumn(column);
        }
        batchQuery.addRow(query.getRows().get(0));
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.INSERT, batchQuery));
        }
        return DialectFactory.getDialect(dbType).insertTranslator().translate(batchQuery);
    }

    /**
     * 获取批量参数。
     *
     * @return 批量参数列表
     */
    private List<List<Object>> batchParams() {
        return query.getRows();
    }

    /**
     * 将插入前自动处理的列补充到 INSERT 字段列表。
     *
     * <p>当用户显式调用 {@code into(...)} 且没有包含主键或自动填充列时，框架需要自动补充字段，
     * 否则字段值只会回填到对象而不会写入数据库。</p>
     *
     * @param columnNames 需要补充的列名
     */
    private void appendAutoColumns(Iterable<String> columnNames) {
        if (columnNames == null) {
            return;
        }
        if (query.getColumns().isEmpty()) {
            return;
        }
        for (String columnName : columnNames) {
            if (!containsColumn(columnName)) {
                if (!query.getRows().isEmpty()) {
                    throw new IllegalStateException("已有INSERT VALUES数据后不能自动追加字段");
                }
                query.addColumn(ColumnRef.of(query.getTable().getTableClass(), columnName));
            }
        }
    }

    /**
     * 当调用方未显式指定 into(...) 列时，按实体元数据顺序补全 INSERT 列名，
     * 避免数据库按物理列顺序解释 VALUES 导致字段错位。
     *
     * @param entity 待插入实体
     */
    private void appendAllColumns(Object entity) {
        if (entity == null || !query.getColumns().isEmpty()) {
            return;
        }
        for (EntityFieldMeta field : EntityMetas.get(entity.getClass()).getFields()) {
            query.addColumn(ColumnRef.of(query.getTable().getTableClass(), field.getColumnName()));
        }
    }

    /**
     * 判断当前 INSERT 字段列表是否已包含指定列。
     *
     * @param columnName 数据库列名
     * @return 已包含时返回 {@code true}
     */
    private boolean containsColumn(String columnName) {
        for (ColumnRef column : query.getColumns()) {
            if (column.getColumnName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前实体是否需要等待数据库 generated keys 回填。
     *
     * <p>配置了框架侧主键策略时，主键会在 INSERT 前生成并写回实体，不再请求 JDBC generated keys。</p>
     *
     * @param entity 待插入实体
     * @return 需要数据库 generated keys 时返回 {@code true}
     */
    private boolean needDatabaseGeneratedKey(Object entity) {
        return entity != null && EntityMetas.get(entity.getClass()).getIdType() == IdType.NONE;
    }
}
