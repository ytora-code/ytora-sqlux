package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SELECT 查询模型。
 *
 * <p>Stage 层只负责提供链式 API，真实 SQL 信息统一沉淀到该模型中。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SelectQuery {

    private final StageContextHolder contextHolder = new StageContextHolder();

    private final List<SqlExpression> selectColumns = new ArrayList<>();

    private final List<JoinClause> joins = new ArrayList<>();

    private final List<SqlExpression> groupByColumns = new ArrayList<>();

    private final List<OrderClause> orderByColumns = new ArrayList<>();

    private boolean distinct;

    private QuerySource from;

    private ExpressionGroup where;

    private ExpressionGroup having;

    private Integer limit;

    private Integer offset;

    /**
     * 获取 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * 添加 SELECT 查询字段。
     *
     * @param column 字段引用
     */
    public void addSelectColumn(SqlExpression column) {
        selectColumns.add(column);
    }

    /**
     * 清空 SELECT 查询字段。
     *
     * <p>示例：数据权限拦截器可以先清空原始查询字段，再通过
     * {@code addSelectColumn(...)} 限制最终允许查询的字段。</p>
     */
    public void clearSelectColumns() {
        selectColumns.clear();
    }

    /**
     * 获取 SELECT 查询字段。
     *
     * @return 不可变字段列表
     */
    public List<SqlExpression> getSelectColumns() {
        return Collections.unmodifiableList(selectColumns);
    }

    /**
     * 判断是否使用 DISTINCT。
     *
     * @return {@code true} 表示使用 DISTINCT
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * 设置是否使用 DISTINCT。
     *
     * @param distinct 是否使用 DISTINCT
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * 获取 FROM 表。
     *
     * @return FROM 表引用
     */
    public QuerySource getFrom() {
        return from;
    }

    /**
     * 设置 FROM 表。
     *
     * @param from FROM 表引用
     */
    public void setFrom(QuerySource from) {
        this.from = from;
    }

    /**
     * 添加 JOIN 子句。
     *
     * @param join JOIN 子句
     */
    public void addJoin(JoinClause join) {
        joins.add(join);
    }

    /**
     * 获取 JOIN 子句列表。
     *
     * @return 不可变 JOIN 列表
     */
    public List<JoinClause> getJoins() {
        return Collections.unmodifiableList(joins);
    }

    /**
     * 获取 WHERE 条件。
     *
     * @return WHERE 条件表达式
     */
    public ExpressionGroup getWhere() {
        return where;
    }

    /**
     * 设置 WHERE 条件。
     *
     * @param where WHERE 条件表达式
     */
    public void setWhere(ExpressionGroup where) {
        this.where = where;
    }

    /**
     * 添加 GROUP BY 字段。
     *
     * @param column 字段引用
     */
    public void addGroupByColumn(SqlExpression column) {
        groupByColumns.add(column);
    }

    /**
     * 获取 GROUP BY 字段列表。
     *
     * @return 不可变字段列表
     */
    public List<SqlExpression> getGroupByColumns() {
        return Collections.unmodifiableList(groupByColumns);
    }

    /**
     * 获取 HAVING 条件。
     *
     * @return HAVING 条件表达式
     */
    public ExpressionGroup getHaving() {
        return having;
    }

    /**
     * 设置 HAVING 条件。
     *
     * @param having HAVING 条件表达式
     */
    public void setHaving(ExpressionGroup having) {
        this.having = having;
    }

    /**
     * 添加 ORDER BY 排序项。
     *
     * @param order 排序项
     */
    public void addOrderByColumn(OrderClause order) {
        orderByColumns.add(order);
    }

    /**
     * 获取 ORDER BY 排序项列表。
     *
     * @return 不可变排序项列表
     */
    public List<OrderClause> getOrderByColumns() {
        return Collections.unmodifiableList(orderByColumns);
    }

    /**
     * 清空 ORDER BY 排序项。
     */
    public void clearOrderByColumns() {
        orderByColumns.clear();
    }

    /**
     * 获取 LIMIT 值。
     *
     * @return LIMIT 值
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * 设置 LIMIT 值。
     *
     * @param limit LIMIT 值
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * 获取 OFFSET 值。
     *
     * @return OFFSET 值
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * 设置 OFFSET 值。
     *
     * @param offset OFFSET 值
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * 复制当前 SELECT 查询模型。
     *
     * <p>分页查询需要在原始查询基础上分别生成 count 查询和数据查询，复制模型可以避免改写用户持有的阶段对象。</p>
     *
     * @return 新的 SELECT 查询模型
     */
    public SelectQuery copy() {
        SelectQuery copy = new SelectQuery();
        copy.selectColumns.addAll(selectColumns);
        copy.joins.addAll(joins);
        copy.groupByColumns.addAll(groupByColumns);
        copy.orderByColumns.addAll(orderByColumns);
        copy.distinct = distinct;
        copy.from = from;
        copy.where = where;
        copy.having = having;
        copy.limit = limit;
        copy.offset = offset;
        copy.contextHolder.copyFrom(contextHolder);
        return copy;
    }
}
