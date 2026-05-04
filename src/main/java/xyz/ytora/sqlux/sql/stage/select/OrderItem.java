package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.func.ColFunction;

/**
 * 排序项
 *
 * @author ytora
 * @since 1.0
 */
public class OrderItem {

    /**
     * 排序字段
     */
    private ColFunction<?, ?> orderColumn;

    /**
     * 排序类型
     */
    private OrderType orderType;

    /**
     * 创建排序项。
     *
     * @param orderColumn 排序字段方法引用
     * @param orderType 排序方向
     * @param <T> 排序字段所属实体类型
     */
    public <T> OrderItem(ColFunction<T, ?> orderColumn, OrderType orderType) {
        this.orderColumn = orderColumn;
        this.orderType = orderType;
    }

    /**
     * 设置排序字段。
     *
     * @param orderColumn 排序字段方法引用
     * @param <T> 排序字段所属实体类型
     */
    public <T> void setOrderColumn(ColFunction<T, ?> orderColumn) {
        this.orderColumn = orderColumn;
    }

    /**
     * 获取排序方向。
     *
     * @return 排序方向
     */
    public OrderType getOrderType() {
        return orderType;
    }

    /**
     * 设置排序方向。
     *
     * @param orderType 排序方向
     */
    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    /**
     * 获取排序字段。
     *
     * @return 排序字段方法引用
     */
    public ColFunction<?, ?> getOrderColumn() {
        return orderColumn;
    }
}
