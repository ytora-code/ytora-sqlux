package xyz.ytora.sqlux.query.rule.support;

import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.query.QueryBuildContext;
import xyz.ytora.sqlux.query.model.QueryField;
import xyz.ytora.sqlux.query.model.QueryOrder;
import xyz.ytora.sqlux.query.QueryParamUtil;
import xyz.ytora.sqlux.query.rule.QueryRule;

/**
 * 解析排序字段参数。
 *
 * @author ytora
 * @since 1.0
 */
public class OrderColumnQueryRule implements QueryRule {

    public static final String PARAM_NAME = "sql_order_col";

    @Override
    public void parse(QueryBuildContext<?> context) {
        if (context.getParams() == null) {
            return;
        }
        for (String item : QueryParamUtil.splitCsv(context.getParams().get(PARAM_NAME))) {
            OrderItem orderItem = parseOrderItem(item);
            QueryField field = context.requireField(orderItem.fieldName);
            QueryOrder order = new QueryOrder(field.getFieldName(), field.getColumnName(),
                    field.toColumnRef(), orderItem.orderType);
            context.getSpec().addOrder(context.getCallback().onOrder(order));
        }
    }

    private OrderItem parseOrderItem(String item) {
        String value = item == null ? "" : item.trim();
        if (value.endsWith("↑")) {
            return new OrderItem(value.substring(0, value.length() - 1), OrderType.ASC);
        }
        if (value.endsWith("↓")) {
            return new OrderItem(value.substring(0, value.length() - 1), OrderType.DESC);
        }
        int index = value.lastIndexOf(":");
        if (index > 0) {
            String direction = value.substring(index + 1).trim();
            OrderType orderType = "desc".equalsIgnoreCase(direction) ? OrderType.DESC : OrderType.ASC;
            return new OrderItem(value.substring(0, index), orderType);
        }
        return new OrderItem(value, OrderType.ASC);
    }

    private static class OrderItem {

        private final String fieldName;

        private final OrderType orderType;

        private OrderItem(String fieldName, OrderType orderType) {
            this.fieldName = fieldName;
            this.orderType = orderType;
        }
    }
}
