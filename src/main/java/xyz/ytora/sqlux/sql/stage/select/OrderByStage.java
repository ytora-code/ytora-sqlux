package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.SelectQuery;

/**
 * ORDER阶段
 *
 * @author ytora 
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class OrderByStage extends AbsSelect {

    /**
     * 创建 ORDER BY 阶段。
     *
     * @param query SELECT 查询模型
     */
    public OrderByStage(SelectQuery query) {
        super(query);
    }

    /**
     * ORDER BY 后可能继续追加排序字段
     */
    public <R> OrderByStage orderBy(ColFunction<R, ?> orderColumn, OrderType orderType) {
        return orderBy(true, orderColumn, orderType);
    }

    /**
     * ORDER BY 后可能继续追加排序表达式
     */
    public OrderByStage orderBy(SqlExpression orderColumn, OrderType orderType) {
        return orderBy(true, orderColumn, orderType);
    }

    /**
     * ORDER BY 后可能继续追加排序字段
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderBy(boolean condition, ColFunction<R, ?> orderColumn, OrderType orderType) {
        if (condition) {
            orderByStage(orderColumn, orderType);
        }
        return this;
    }

    /**
     * ORDER BY 后可能继续追加排序表达式
     * 带条件的 ORDER BY
     */
    public OrderByStage orderBy(boolean condition, SqlExpression orderColumn, OrderType orderType) {
        if (condition) {
            orderByStage(orderColumn, orderType);
        }
        return this;
    }

    /**
     * ORDER BY 后可能继续追加 ASC 排序字段
     */
    public <R> OrderByStage orderByAsc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * ORDER BY 后可能继续追加 ASC 排序表达式
     */
    public OrderByStage orderByAsc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.ASC);
    }

    /**
     * ORDER BY 后可能继续追加 ASC 排序字段
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderByAsc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * ORDER BY 后可能继续追加 ASC 排序表达式
     * 带条件的 ORDER BY
     */
    public OrderByStage orderByAsc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.ASC);
    }

    /**
     * ORDER BY 后可能继续追加 DESC 排序字段
     */
    public <R> OrderByStage orderByDesc(ColFunction<R, ?> orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * ORDER BY 后可能继续追加 DESC 排序表达式
     */
    public OrderByStage orderByDesc(SqlExpression orderColumn) {
        return orderBy(orderColumn, OrderType.DESC);
    }

    /**
     * ORDER BY 后可能继续追加 DESC 排序字段
     * 带条件的 ORDER BY
     */
    public <R> OrderByStage orderByDesc(boolean condition, ColFunction<R, ?> orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * ORDER BY 后可能继续追加 DESC 排序表达式
     * 带条件的 ORDER BY
     */
    public OrderByStage orderByDesc(boolean condition, SqlExpression orderColumn) {
        return orderBy(condition, orderColumn, OrderType.DESC);
    }

    /**
     * ORDER BY 后可能是 LIMIT 子句
     */
    public LimitStage limit(Integer limit) {
        return limitStage(limit);
    }

    /**
     * ORDER BY 后可能是 OFFSET 子句
     */
    public OffsetStage offset(Integer offset) {
        return offsetStage(offset);
    }

}
