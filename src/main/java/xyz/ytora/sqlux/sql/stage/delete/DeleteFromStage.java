package xyz.ytora.sqlux.sql.stage.delete;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlRewriteContext;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.TableRef;
import xyz.ytora.sqlux.sql.stage.EntityWhereAppender;
import xyz.ytora.sqlux.sql.stage.TerminationStage;
import xyz.ytora.sqlux.translate.DialectFactory;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.function.Consumer;

/**
 * DELETE FROM 阶段。
 *
 * <p>用于追加 JOIN 和 WHERE 子句；该阶段也是 DELETE 的终止阶段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DeleteFromStage implements TerminationStage<Integer> {

    private final DeleteQuery query;

    /**
     * 创建 DELETE FROM 阶段。
     *
     * @param query DELETE 查询模型
     */
    public DeleteFromStage(DeleteQuery query) {
        this.query = query;
    }

    /**
     * 追加 INNER JOIN 子句。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage join(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.INNER_JOIN, joinTable, null, on);
    }

    /**
     * 追加 INNER JOIN 子句并设置别名。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param alias 表别名
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage join(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.INNER_JOIN, joinTable, alias, on);
    }

    /**
     * 追加 LEFT JOIN 子句。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage leftJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.LEFT_JOIN, joinTable, null, on);
    }

    /**
     * 追加 LEFT JOIN 子句并设置别名。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param alias 表别名
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage leftJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.LEFT_JOIN, joinTable, alias, on);
    }

    /**
     * 追加 RIGHT JOIN 子句。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage rightJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return join(JoinType.RIGHT_JOIN, joinTable, null, on);
    }

    /**
     * 追加 RIGHT JOIN 子句并设置别名。
     *
     * @param joinTable 被连接表对应的实体类型
     * @param alias 表别名
     * @param on ON 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage rightJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return join(JoinType.RIGHT_JOIN, joinTable, alias, on);
    }

    /**
     * 添加 WHERE 条件。
     *
     * @param whereExpr WHERE 条件表达式
     * @return 当前 DELETE FROM 阶段对象
     */
    public DeleteFromStage where(Consumer<ExpressionBuilder> whereExpr) {
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
    public final <E extends AbsEntity> DeleteFromStage where(E... whereObjs) {
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
     * 执行 DELETE，并返回影响行数。
     *
     * @return 影响行数
     */
    @Override
    public Integer submit() {
        return SQL.getSqluxGlobal().getExecutor().update(toSql());
    }

    /**
     * 翻译 DELETE 语句，不执行数据库操作。
     *
     * @return SQL翻译结果
     */
    public SqlResult toSql() {
        return toSql(SQL.getSqluxGlobal().getDbType());
    }

    /**
     * 使用指定数据库类型翻译 DELETE 语句，不执行数据库操作。
     *
     * @param dbType 数据库类型；入参决定标识符引用等方言细节
     * @return SQL翻译结果
     */
    public SqlResult toSql(DbType dbType) {
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.DELETE, query));
        }
        return DialectFactory.getDialect(dbType).deleteTranslator().translate(query);
    }

    /**
     * 写入 DELETE JOIN 子句。
     *
     * @param joinType JOIN 类型
     * @param joinTable 被关联的实体表
     * @param alias 被关联表别名；为空时自动分配
     * @param on ON 条件构造回调
     * @return 当前 DELETE FROM 阶段
     */
    private DeleteFromStage join(JoinType joinType, Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
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
}
