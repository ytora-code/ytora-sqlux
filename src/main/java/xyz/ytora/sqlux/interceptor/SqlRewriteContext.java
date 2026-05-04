package xyz.ytora.sqlux.interceptor;

import xyz.ytora.sqlux.core.enums.Connector;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;
import xyz.ytora.sqlux.core.enums.SqlType;

import java.util.function.Consumer;

/**
 * SQL翻译前的结构化改写上下文。
 *
 * <p>该上下文暴露的是 SELECT/INSERT/UPDATE/DELETE 的查询模型，而不是已经拼好的 SQL 字符串。
 * 数据权限、租户隔离、字段裁剪等插件可以在这里用类似链式 API 的方式追加条件或限制字段。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class SqlRewriteContext {

    private final SqlType sqlType;

    private final Object statement;

    /**
     * 创建结构化 SQL 改写上下文。
     *
     * <p>示例：SELECT 翻译前会创建 {@code new SqlRewriteContext(SqlType.SELECT, selectQuery)}。</p>
     *
     * @param sqlType SQL语句类型；入参用于区分增删改查
     * @param statement 源查询模型；入参通常是 {@link SelectQuery}、{@link InsertQuery}、
     *                  {@link UpdateQuery} 或 {@link DeleteQuery}
     */
    public SqlRewriteContext(SqlType sqlType, Object statement) {
        if (sqlType == null) {
            throw new IllegalArgumentException("SQL类型不能为空");
        }
        if (statement == null) {
            throw new IllegalArgumentException("SQL结构模型不能为空");
        }
        this.sqlType = sqlType;
        this.statement = statement;
    }

    /**
     * 获取 SQL 语句类型。
     *
     * <p>示例：数据权限插件可以只处理 {@link SqlType#SELECT}。</p>
     *
     * @return SQL语句类型；出参不会为 {@code null}
     */
    public SqlType getSqlType() {
        return sqlType;
    }

    /**
     * 获取源查询模型。
     *
     * <p>示例：当 {@link #getSqlType()} 为 {@link SqlType#SELECT} 时，返回对象可强转为 {@link SelectQuery}。</p>
     *
     * @return 源查询模型；出参不会为 {@code null}
     */
    public Object getStatement() {
        return statement;
    }

    /**
     * 获取 SELECT 查询模型。
     *
     * <p>示例：{@code context.getSelectQuery().getFrom()} 可以读取当前 SELECT 的 FROM 表。</p>
     *
     * @return SELECT 查询模型；当前 SQL 不是 SELECT 时返回 {@code null}
     */
    public SelectQuery getSelectQuery() {
        return statement instanceof SelectQuery ? (SelectQuery) statement : null;
    }

    /**
     * 获取 INSERT 查询模型。
     *
     * <p>示例：INSERT 插件可以通过该方法读取目标表和插入字段。</p>
     *
     * @return INSERT 查询模型；当前 SQL 不是 INSERT 时返回 {@code null}
     */
    public InsertQuery getInsertQuery() {
        return statement instanceof InsertQuery ? (InsertQuery) statement : null;
    }

    /**
     * 获取 UPDATE 查询模型。
     *
     * <p>示例：数据守卫插件可以读取 UPDATE 是否存在 WHERE 条件。</p>
     *
     * @return UPDATE 查询模型；当前 SQL 不是 UPDATE 时返回 {@code null}
     */
    public UpdateQuery getUpdateQuery() {
        return statement instanceof UpdateQuery ? (UpdateQuery) statement : null;
    }

    /**
     * 获取 DELETE 查询模型。
     *
     * <p>示例：数据守卫插件可以读取 DELETE 是否存在 WHERE 条件。</p>
     *
     * @return DELETE 查询模型；当前 SQL 不是 DELETE 时返回 {@code null}
     */
    public DeleteQuery getDeleteQuery() {
        return statement instanceof DeleteQuery ? (DeleteQuery) statement : null;
    }

    /**
     * 替换 SELECT 查询字段。
     *
     * <p>示例：{@code context.select(User::getId, User::getName)} 会把最终 SELECT 字段限制为
     * {@code id} 和 {@code name}，适合字段级数据权限。</p>
     *
     * @param columns 允许查询的字段；入参为空时会变成 SELECT *
     * @return 当前改写上下文；出参用于继续链式调用
     */
    @SafeVarargs
    public final <T> SqlRewriteContext select(ColFunction<T, ?>... columns) {
        SelectQuery query = requireSelect();
        query.clearSelectColumns();
        if (columns != null) {
            for (ColFunction<?, ?> column : columns) {
                if (column != null) {
                    query.addSelectColumn(ColumnRef.from(column));
                }
            }
        }
        return this;
    }

    /**
     * 替换 SELECT 查询字段为通用表达式。
     *
     * @param expressions 查询表达式
     * @return 当前改写上下文
     */
    public SqlRewriteContext select(SqlExpression... expressions) {
        SelectQuery query = requireSelect();
        query.clearSelectColumns();
        if (expressions != null) {
            for (SqlExpression expression : expressions) {
                if (expression != null) {
                    query.addSelectColumn(expression);
                }
            }
        }
        return this;
    }

    /**
     * 向 SELECT、UPDATE 或 DELETE 追加 AND WHERE 条件。
     *
     * <p>示例：{@code context.andWhere(w -> w.eq(User::getTenantId, tenantId))}
     * 会把租户条件追加到已有 WHERE 后面；如果原来没有 WHERE，则直接设置为该条件。</p>
     *
     * @param whereExpr WHERE 条件表达式；入参为 {@code null} 时不做任何修改
     * @return 当前改写上下文；出参用于继续链式调用
     */
    public SqlRewriteContext andWhere(Consumer<ExpressionBuilder> whereExpr) {
        if (whereExpr == null) {
            return this;
        }
        StageContextHolder contextHolder = getContextHolder();
        ExpressionBuilder builder = new ExpressionBuilder(contextHolder);
        whereExpr.accept(builder);
        ExpressionGroup addition = builder.toExpression();
        if (addition.isEmpty()) {
            return this;
        }
        setWhere(mergeAnd(getWhere(), addition));
        return this;
    }

    /**
     * 限制 SELECT 的 LIMIT 值。
     *
     * <p>示例：{@code context.limit(100)} 可以给没有分页的查询加上最大返回行数限制。</p>
     *
     * @param limit 最大返回行数；入参为 {@code null} 时清除 LIMIT
     * @return 当前改写上下文；出参用于继续链式调用
     */
    public SqlRewriteContext limit(Integer limit) {
        requireSelect().setLimit(limit);
        return this;
    }

    /**
     * 判断当前 SQL 是否有 WHERE 条件。
     *
     * <p>示例：DELETE/UPDATE 守卫插件可以在没有 WHERE 时抛出异常阻止执行。</p>
     *
     * @return 存在非空 WHERE 条件时返回 {@code true}
     */
    public boolean hasWhere() {
        ExpressionGroup where = getWhere();
        return where != null && !where.isEmpty();
    }

    /**
     * 获取 SELECT 查询模型，非 SELECT 语句时直接抛出异常。
     *
     * @return SELECT 查询模型
     */
    private SelectQuery requireSelect() {
        SelectQuery query = getSelectQuery();
        if (query == null) {
            throw new IllegalStateException("当前SQL不是SELECT，不能执行SELECT改写");
        }
        return query;
    }

    /**
     * 获取当前语句可用于构造条件的阶段上下文。
     *
     * @return SELECT、UPDATE 或 DELETE 的阶段上下文
     */
    private StageContextHolder getContextHolder() {
        if (statement instanceof SelectQuery) {
            return ((SelectQuery) statement).getContextHolder();
        }
        if (statement instanceof UpdateQuery) {
            return ((UpdateQuery) statement).getContextHolder();
        }
        if (statement instanceof DeleteQuery) {
            return ((DeleteQuery) statement).getContextHolder();
        }
        throw new IllegalStateException("当前SQL类型不支持WHERE改写: " + sqlType);
    }

    /**
     * 读取当前语句已有的 WHERE 条件。
     *
     * @return WHERE 表达式组；没有 WHERE 或不支持 WHERE 时返回 {@code null}
     */
    private ExpressionGroup getWhere() {
        if (statement instanceof SelectQuery) {
            return ((SelectQuery) statement).getWhere();
        }
        if (statement instanceof UpdateQuery) {
            return ((UpdateQuery) statement).getWhere();
        }
        if (statement instanceof DeleteQuery) {
            return ((DeleteQuery) statement).getWhere();
        }
        return null;
    }

    /**
     * 写回当前语句的 WHERE 条件。
     *
     * @param where 新的 WHERE 表达式组
     */
    private void setWhere(ExpressionGroup where) {
        if (statement instanceof SelectQuery) {
            ((SelectQuery) statement).setWhere(where);
            return;
        }
        if (statement instanceof UpdateQuery) {
            ((UpdateQuery) statement).setWhere(where);
            return;
        }
        if (statement instanceof DeleteQuery) {
            ((DeleteQuery) statement).setWhere(where);
            return;
        }
        throw new IllegalStateException("当前SQL类型不支持WHERE改写: " + sqlType);
    }

    /**
     * 使用 AND 把原 WHERE 与新增 WHERE 合并为一个嵌套表达式组。
     *
     * @param original 原 WHERE 条件
     * @param addition 新增 WHERE 条件
     * @return 合并后的 WHERE 条件
     */
    private ExpressionGroup mergeAnd(ExpressionGroup original, ExpressionGroup addition) {
        if (original == null || original.isEmpty()) {
            return addition;
        }
        ExpressionGroup merged = new ExpressionGroup();
        merged.add(Connector.AND, original);
        merged.add(Connector.AND, addition);
        return merged;
    }
}
