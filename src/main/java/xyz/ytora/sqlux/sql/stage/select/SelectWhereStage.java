package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.SelectQuery;

/**
 * WHERE阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings({"overloads", "varargs"})
public class SelectWhereStage extends AbsSelect {

    /**
     * 创建 SELECT WHERE 阶段。
     *
     * @param query SELECT 查询模型
     */
    public SelectWhereStage(SelectQuery query) {
        super(query);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <R> GroupByStage groupBy(ColFunction<R, ?> groupColumn) {
        return groupByStage(groupColumn);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return groupByStage(first, second);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B, C> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                          ColFunction<C, ?> third) {
        return groupByStage(first, second, third);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B, C, D> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                             ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return groupByStage(first, second, third, fourth);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B, C, D, E> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                ColFunction<E, ?> fifth) {
        return groupByStage(first, second, third, fourth, fifth);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B, C, D, E, F> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                   ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                   ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return groupByStage(first, second, third, fourth, fifth, sixth);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public <A, B, C, D, E, F, G> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                      ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                      ColFunction<E, ?> fifth, ColFunction<F, ?> sixth,
                                                      ColFunction<G, ?> seventh) {
        return groupByStage(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    @SafeVarargs
    public final <R> GroupByStage groupBy(ColFunction<R, ?>... groupColumns) {
        return groupByStage(groupColumns);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public GroupByStage groupBy(SqlExpression groupColumn) {
        return groupByStage(groupColumn);
    }

    /**
     * WHERE 后可能是 GROUP BY 子句
     */
    public GroupByStage groupBy(SqlExpression... groupColumns) {
        return groupByStage(groupColumns);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句
     */
    public <R> OrderByStage orderBy(ColFunction<R, ?> orderColumn, OrderType orderType) {
        return orderByStage(orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句
     */
    public OrderByStage orderBy(SqlExpression orderColumn, OrderType orderType) {
        return orderByStage(orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderBy(boolean condition, ColFunction<R, ?> orderColumn, OrderType orderType) {
        return orderByStage(condition, orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY 子句
     * 带条件的 ORDER BY
     */
    public OrderByStage orderBy(boolean condition, SqlExpression orderColumn, OrderType orderType) {
        return orderByStage(condition, orderColumn, orderType);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句
     */
    public <R> OrderByStage orderByAsc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句
     */
    public OrderByStage orderByAsc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderByAsc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY ASC 子句
     * 带条件的 ORDER BY
     */
    public OrderByStage orderByAsc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句
     */
    public <R> OrderByStage orderByDesc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句
     */
    public OrderByStage orderByDesc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderByDesc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 ORDER BY DESC 子句
     * 带条件的 ORDER BY
     */
    public OrderByStage orderByDesc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * 当前阶段后可能是 LIMIT 子句
     */
    public LimitStage limit(Integer limit) {
        return limitStage(limit);
    }

    /**
     * 当前阶段后可能是 OFFSET 子句
     */
    public OffsetStage offset(Integer offset) {
        return offsetStage(offset);
    }
}
