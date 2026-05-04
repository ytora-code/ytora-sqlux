package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.query.model.QueryColumn;
import xyz.ytora.sqlux.query.model.QueryCondition;
import xyz.ytora.sqlux.query.model.QueryGroup;
import xyz.ytora.sqlux.query.model.QueryOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查询参数解析后的结构化规格。
 *
 * @author ytora
 * @since 1.0
 */
public class QuerySpec {

    private boolean distinct;

    private final List<QueryColumn> columns = new ArrayList<>();

    private final List<QueryCondition> conditions = new ArrayList<>();

    private final List<QueryGroup> groups = new ArrayList<>();

    private final List<QueryOrder> orders = new ArrayList<>();

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void addColumn(QueryColumn column) {
        if (column != null) {
            columns.add(column);
        }
    }

    public void addCondition(QueryCondition condition) {
        if (condition != null) {
            conditions.add(condition);
        }
    }

    public void addGroup(QueryGroup group) {
        if (group != null) {
            groups.add(group);
        }
    }

    public void addOrder(QueryOrder order) {
        if (order != null) {
            orders.add(order);
        }
    }

    public List<QueryColumn> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<QueryCondition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public List<QueryGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public List<QueryOrder> getOrders() {
        return Collections.unmodifiableList(orders);
    }
}
