package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.SelectQuery;

import java.util.function.Consumer;

/**
 * GROUP BY阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings({"overloads", "varargs"})
public class GroupByStage extends AbsSelect {

    /**
     * 创建 GROUP BY 阶段。
     *
     * @param query SELECT 查询模型
     */
    public GroupByStage(SelectQuery query) {
        super(query);
    }

    /**
     * GROUP BY 后可能继续追加分组字段
     */
    public <R> GroupByStage groupBy(ColFunction<R, ?> groupColumn) {
        return groupBy(true, groupColumn);
    }

    /**
     * GROUP BY 后可能继续追加两个分组字段
     */
    public <A, B> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return groupBy(true, first, second);
    }

    /**
     * GROUP BY 后可能继续追加三个分组字段
     */
    public <A, B, C> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                          ColFunction<C, ?> third) {
        return groupBy(true, first, second, third);
    }

    /**
     * GROUP BY 后可能继续追加四个分组字段
     */
    public <A, B, C, D> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                             ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return groupBy(true, first, second, third, fourth);
    }

    /**
     * GROUP BY 后可能继续追加五个分组字段
     */
    public <A, B, C, D, E> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                ColFunction<E, ?> fifth) {
        return groupBy(true, first, second, third, fourth, fifth);
    }

    /**
     * GROUP BY 后可能继续追加六个分组字段
     */
    public <A, B, C, D, E, F> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                   ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                   ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return groupBy(true, first, second, third, fourth, fifth, sixth);
    }

    /**
     * GROUP BY 后可能继续追加七个分组字段
     */
    public <A, B, C, D, E, F, G> GroupByStage groupBy(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                      ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                      ColFunction<E, ?> fifth, ColFunction<F, ?> sixth,
                                                      ColFunction<G, ?> seventh) {
        return groupBy(true, first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * GROUP BY 后可能继续追加多个分组字段
     */
    @SafeVarargs
    public final <R> GroupByStage groupBy(ColFunction<R, ?>... groupColumns) {
        return groupBy(true, groupColumns);
    }

    /**
     * GROUP BY 后可能继续追加分组表达式
     */
    public GroupByStage groupBy(SqlExpression groupColumn) {
        return groupBy(true, groupColumn);
    }

    /**
     * GROUP BY 后可能继续追加多个分组表达式
     */
    public GroupByStage groupBy(SqlExpression... groupColumns) {
        return groupBy(true, groupColumns);
    }

    /**
     * GROUP BY 后可能继续追加分组字段
     * 带条件的 GROUP BY
     */
    public <R> GroupByStage groupBy(boolean condition, ColFunction<R, ?> groupColumn) {
        if (condition && groupColumn != null) {
            getQuery().addGroupByColumn(ColumnRef.from(groupColumn));
        }
        return this;
    }

    /**
     * GROUP BY 后可能继续追加两个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return addGroupByColumns(condition, first, second);
    }

    /**
     * GROUP BY 后可能继续追加三个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B, C> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first, ColFunction<B, ?> second,
                                          ColFunction<C, ?> third) {
        return addGroupByColumns(condition, first, second, third);
    }

    /**
     * GROUP BY 后可能继续追加四个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B, C, D> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first,
                                             ColFunction<B, ?> second, ColFunction<C, ?> third,
                                             ColFunction<D, ?> fourth) {
        return addGroupByColumns(condition, first, second, third, fourth);
    }

    /**
     * GROUP BY 后可能继续追加五个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B, C, D, E> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first,
                                                ColFunction<B, ?> second, ColFunction<C, ?> third,
                                                ColFunction<D, ?> fourth, ColFunction<E, ?> fifth) {
        return addGroupByColumns(condition, first, second, third, fourth, fifth);
    }

    /**
     * GROUP BY 后可能继续追加六个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B, C, D, E, F> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first,
                                                   ColFunction<B, ?> second, ColFunction<C, ?> third,
                                                   ColFunction<D, ?> fourth, ColFunction<E, ?> fifth,
                                                   ColFunction<F, ?> sixth) {
        return addGroupByColumns(condition, first, second, third, fourth, fifth, sixth);
    }

    /**
     * GROUP BY 后可能继续追加七个分组字段
     * 带条件的 GROUP BY
     */
    public <A, B, C, D, E, F, G> GroupByStage groupBy(boolean condition, ColFunction<A, ?> first,
                                                      ColFunction<B, ?> second, ColFunction<C, ?> third,
                                                      ColFunction<D, ?> fourth, ColFunction<E, ?> fifth,
                                                      ColFunction<F, ?> sixth, ColFunction<G, ?> seventh) {
        return addGroupByColumns(condition, first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * GROUP BY 后可能继续追加多个分组字段
     * 带条件的 GROUP BY
     */
    @SafeVarargs
    public final <R> GroupByStage groupBy(boolean condition, ColFunction<R, ?>... groupColumns) {
        return addGroupByColumns(condition, groupColumns);
    }

    /**
     * 追加方法引用形式的 GROUP BY 字段。
     */
    private GroupByStage addGroupByColumns(boolean condition, ColFunction<?, ?>... groupColumns) {
        if (condition && groupColumns != null) {
            for (ColFunction<?, ?> groupColumn : groupColumns) {
                if (groupColumn != null) {
                    getQuery().addGroupByColumn(ColumnRef.from(groupColumn));
                }
            }
        }
        return this;
    }

    /**
     * GROUP BY 后可能继续追加分组表达式
     * 带条件的 GROUP BY
     */
    public GroupByStage groupBy(boolean condition, SqlExpression groupColumn) {
        if (condition && groupColumn != null) {
            getQuery().addGroupByColumn(groupColumn);
        }
        return this;
    }

    /**
     * GROUP BY 后可能继续追加多个分组表达式
     * 带条件的 GROUP BY
     */
    public GroupByStage groupBy(boolean condition, SqlExpression... groupColumns) {
        if (condition && groupColumns != null) {
            for (SqlExpression groupColumn : groupColumns) {
                if (groupColumn != null) {
                    getQuery().addGroupByColumn(groupColumn);
                }
            }
        }
        return this;
    }

    /**
     * GROUP BY 后可能是 HAVING 子句
     */
    public HavingStage having(Consumer<ExpressionBuilder> havingExpr) {
        return havingStage(havingExpr);
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
