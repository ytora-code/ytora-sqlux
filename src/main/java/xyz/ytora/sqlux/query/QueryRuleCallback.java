package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.query.model.QueryColumn;
import xyz.ytora.sqlux.query.model.QueryCondition;
import xyz.ytora.sqlux.query.model.QueryGroup;
import xyz.ytora.sqlux.query.model.QueryOrder;

/**
 * 查询参数规则回调。
 *
 * <p>回调返回 {@code null} 表示忽略当前规则；返回新的规则对象表示替换当前规则。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface QueryRuleCallback {

    QueryRuleCallback NONE = new QueryRuleCallback() {
    };

    default QueryColumn onQueryColumn(QueryColumn column) {
        return column;
    }

    default QueryCondition onWhere(QueryCondition condition) {
        return condition;
    }

    default QueryOrder onOrder(QueryOrder order) {
        return order;
    }

    default QueryGroup onGroup(QueryGroup group) {
        return group;
    }
}
