package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.SelectQuery;

import java.util.function.Consumer;

/**
 * FROM阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings({"overloads", "varargs"})
public class FromStage<T> extends AbsSelect {

    /**
     * FROM主表
     */
    private final Class<T> tableClazz;

    /**
     * 创建 FROM 阶段。
     *
     * @param query SELECT 查询模型
     * @param tableClazz FROM 主表实体类型；子查询 FROM 时可以为 {@code null}
     */
    public FromStage(SelectQuery query, Class<T> tableClazz) {
        super(query);
        this.tableClazz = tableClazz;
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子句。
     */
    public JoinStage join(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return innerJoin(joinTable, on);
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子句。
     */
    public JoinStage join(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return innerJoin(joinTable, alias, on);
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子查询。
     */
    public JoinStage join(AbsSelect subQuery, String alias, Consumer<ExpressionBuilder> on) {
        return innerJoin(subQuery, alias, on);
    }

    /**
     * 当前阶段后可能是 LEFT JOIN 子句。
     */
    public JoinStage leftJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.LEFT_JOIN, joinTable, null, on);
    }

    /**
     * 当前阶段后可能是 LEFT JOIN 子句。
     */
    public JoinStage leftJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.LEFT_JOIN, joinTable, alias, on);
    }

    /**
     * 当前阶段后可能是 LEFT JOIN 子查询。
     */
    public JoinStage leftJoin(AbsSelect subQuery, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.LEFT_JOIN, subQuery, alias, on);
    }

    /**
     * 当前阶段后可能是 RIGHT JOIN 子句。
     */
    public JoinStage rightJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.RIGHT_JOIN, joinTable, null, on);
    }

    /**
     * 当前阶段后可能是 RIGHT JOIN 子句。
     */
    public JoinStage rightJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.RIGHT_JOIN, joinTable, alias, on);
    }

    /**
     * 当前阶段后可能是 RIGHT JOIN 子查询。
     */
    public JoinStage rightJoin(AbsSelect subQuery, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.RIGHT_JOIN, subQuery, alias, on);
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子句。
     */
    public JoinStage innerJoin(Class<?> joinTable, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.INNER_JOIN, joinTable, null, on);
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子句。
     */
    public JoinStage innerJoin(Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.INNER_JOIN, joinTable, alias, on);
    }

    /**
     * 当前阶段后可能是 INNER JOIN 子查询。
     */
    public JoinStage innerJoin(AbsSelect subQuery, String alias, Consumer<ExpressionBuilder> on) {
        return joinStage(JoinType.INNER_JOIN, subQuery, alias, on);
    }

    /**
     * 当前阶段后可能是 WHERE 子句。
     */
    public SelectWhereStage where(Consumer<ExpressionBuilder> whereExpr) {
        return whereStage(whereExpr);
    }

    /**
     * 当前阶段后可能是 WHERE 子句。
     */
    @SafeVarargs
    public final <E extends AbsEntity> SelectWhereStage where(E... whereObjs) {
        return whereStage(whereObjs);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <R> GroupByStage groupBy(ColFunction<R, ?> groupColumn) {
        return groupByStage(groupColumn);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return groupByStage(first, second);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B, C> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                          ColFunction<C, ?> third) {
        return groupByStage(first, second, third);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B, C, D> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                             ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return groupByStage(first, second, third, fourth);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B, C, D, E> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                ColFunction<E, ?> fifth) {
        return groupByStage(first, second, third, fourth, fifth);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B, C, D, E, F> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                   ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                   ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return groupByStage(first, second, third, fourth, fifth, sixth);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public <A, B, C, D, E, F, G> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                      ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                      ColFunction<E, ?> fifth, ColFunction<F, ?> sixth,
                                                      ColFunction<G, ?> seventh) {
        return groupByStage(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    @SafeVarargs
    public final <R> GroupByStage groupBy(ColFunction<R, ?>... groupColumns) {
        return groupByStage(groupColumns);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public GroupByStage groupBy(SqlExpression groupColumn) {
        return groupByStage(groupColumn);
    }

    /**
     * 当前阶段后可能是 GROUP BY 子句。
     */
    public GroupByStage groupBy(SqlExpression... groupColumns) {
        return groupByStage(groupColumns);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句。
     */
    public <R> OrderByStage orderBy(ColFunction<R, ?> orderColumn, OrderType orderType) {
        return orderByStage(orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句。
     */
    public OrderByStage orderBy(SqlExpression orderColumn, OrderType orderType) {
        return orderByStage(orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句。
     * 带条件的 ORDER BY。
     */
    public <R> OrderByStage orderBy(boolean condition, ColFunction<R, ?> orderColumn, OrderType orderType) {
        return orderByStage(condition, orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句。
     * 带条件的 ORDER BY。
     */
    public OrderByStage orderBy(boolean condition, SqlExpression orderColumn, OrderType orderType) {
        return orderByStage(condition, orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句。
     */
    public <R> OrderByStage orderByAsc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句。
     */
    public OrderByStage orderByAsc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句。
     * 带条件的 ORDER BY。
     */
    public <R> OrderByStage orderByAsc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句。
     * 带条件的 ORDER BY。
     */
    public OrderByStage orderByAsc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句。
     */
    public <R> OrderByStage orderByDesc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句。
     */
    public OrderByStage orderByDesc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句。
     * 带条件的 ORDER BY。
     */
    public <R> OrderByStage orderByDesc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句。
     * 带条件的 ORDER BY。
     */
    public OrderByStage orderByDesc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 LIMIT 子句。
     */
    public LimitStage limit(Integer limit) {
        return limitStage(limit);
    }

    /**
     * 当前阶段后可能是 OFFSET 子句。
     */
    public OffsetStage offset(Integer offset) {
        return offsetStage(offset);
    }

    /**
     * 获取 FROM 主表实体类型。
     *
     * @return 主表实体类型；子查询 FROM 时返回 {@code null}
     */
    public Class<T> getTableClazz() {
        return tableClazz;
    }
}
