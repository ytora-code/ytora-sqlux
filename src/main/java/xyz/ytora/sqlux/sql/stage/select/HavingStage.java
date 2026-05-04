package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.SelectQuery;

/**
 * HAVING阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class HavingStage extends AbsSelect {

    /**
     * 创建 HAVING 阶段。
     *
     * @param query SELECT 查询模型
     */
    public HavingStage(SelectQuery query) {
        super(query);
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
