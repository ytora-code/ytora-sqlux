package xyz.ytora.sqlux.query.model;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * ORDER BY 字段规则。
 *
 * @author ytora
 * @since 1.0
 */
public class QueryOrder {

    private final String fieldName;

    private final String columnName;

    private final ColumnRef column;

    private final OrderType orderType;

    public QueryOrder(String fieldName, String columnName, ColumnRef column, OrderType orderType) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.column = column;
        this.orderType = orderType == null ? OrderType.ASC : orderType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColumnRef getColumn() {
        return column;
    }

    public OrderType getOrderType() {
        return orderType;
    }
}
