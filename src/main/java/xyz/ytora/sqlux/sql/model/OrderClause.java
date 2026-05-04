package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * ORDER BY 排序项模型。
 *
 * @author ytora
 * @since 1.0
 */
public class OrderClause {

    private final SqlExpression column;

    private final OrderType orderType;

    /**
     * 创建排序项。
     *
     * @param column 排序字段
     * @param orderType 排序方向
     */
    public OrderClause(SqlExpression column, OrderType orderType) {
        if (column == null) {
            throw new IllegalArgumentException("排序字段不能为空");
        }
        if (orderType == null) {
            throw new IllegalArgumentException("排序类型不能为空");
        }
        this.column = column;
        this.orderType = orderType;
    }

    /**
     * 获取排序字段。
     *
     * @return 字段引用
     */
    public SqlExpression getColumn() {
        return column;
    }

    /**
     * 获取排序方向。
     *
     * @return 排序方向
     */
    public OrderType getOrderType() {
        return orderType;
    }
}
