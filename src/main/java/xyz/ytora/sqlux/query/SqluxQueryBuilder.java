package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.query.model.QueryColumn;
import xyz.ytora.sqlux.query.model.QueryCondition;
import xyz.ytora.sqlux.query.model.QueryGroup;
import xyz.ytora.sqlux.query.model.QueryOrder;
import xyz.ytora.sqlux.query.rule.QueryRule;
import xyz.ytora.sqlux.query.rule.support.*;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.OrderClause;
import xyz.ytora.sqlux.sql.stage.select.AbsSelect;
import xyz.ytora.sqlux.sql.stage.select.FromStage;
import xyz.ytora.sqlux.sql.stage.select.SelectStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 查询参数 SELECT 构建器。
 *
 * @author ytora
 * @since 1.0
 */
public class SqluxQueryBuilder<T> {

    private final Class<T> entityType;

    private final List<QueryRule> rules = new ArrayList<>();

    private Map<String, ?> params = Collections.emptyMap();

    private QueryRuleCallback callback = QueryRuleCallback.NONE;

    public SqluxQueryBuilder(Class<T> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("查询实体类型不能为空");
        }
        this.entityType = entityType;
        this.rules.add(new DistinctQueryRule());
        this.rules.add(new SelectColumnQueryRule());
        this.rules.add(new WhereQueryRule());
        this.rules.add(new GroupColumnQueryRule());
        this.rules.add(new OrderColumnQueryRule());
    }

    /**
     * 设置前端查询参数。
     *
     * @param params 前端查询参数
     * @return 当前构建器
     */
    public SqluxQueryBuilder<T> params(Map<String, ?> params) {
        this.params = params == null ? Collections.emptyMap() : params;
        return this;
    }

    /**
     * 设置规则解析回调。
     *
     * @param callback 规则解析回调
     * @return 当前构建器
     */
    public SqluxQueryBuilder<T> callback(QueryRuleCallback callback) {
        this.callback = callback == null ? QueryRuleCallback.NONE : callback;
        return this;
    }

    /**
     * 追加自定义解析规则。
     *
     * @param rule 查询参数解析规则
     * @return 当前构建器
     */
    public SqluxQueryBuilder<T> addRule(QueryRule rule) {
        if (rule != null) {
            this.rules.add(rule);
        }
        return this;
    }

    /**
     * 移除指定类型的解析规则。
     *
     * @param ruleType 查询参数解析规则类型
     * @return 当前构建器
     */
    public SqluxQueryBuilder<T> removeRule(Class<? extends QueryRule> ruleType) {
        if (ruleType == null) {
            return this;
        }
        for (int i = rules.size() - 1; i >= 0; i--) {
            if (ruleType.isInstance(rules.get(i))) {
                rules.remove(i);
            }
        }
        return this;
    }

    /**
     * 清空所有解析规则。
     *
     * @return 当前构建器
     */
    public SqluxQueryBuilder<T> clearRules() {
        this.rules.clear();
        return this;
    }

    /**
     * 构建 SELECT 阶段对象。
     *
     * @return SELECT 阶段对象
     */
    public AbsSelect build() {
        QuerySpec spec = parse();
        SelectStage select = spec.getColumns().isEmpty()
                ? SQL.select()
                : SQL.select(toColumnArray(spec.getColumns()));
        FromStage<T> from = spec.isDistinct()
                ? select.distinct().from(entityType)
                : select.from(entityType);
        applyWhere(from, spec);
        applyGroup(from, spec);
        applyOrder(from, spec);
        return from;
    }

    private QuerySpec parse() {
        QuerySpec spec = new QuerySpec();
        QueryBuildContext<T> context = new QueryBuildContext<>(entityType, params, callback, spec);
        for (QueryRule rule : rules) {
            rule.parse(context);
        }
        return spec;
    }

    private SqlExpression[] toColumnArray(List<QueryColumn> columns) {
        SqlExpression[] result =
                new SqlExpression[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            result[i] = columns.get(i).getColumn();
        }
        return result;
    }

    private void applyWhere(AbsSelect select, QuerySpec spec) {
        if (spec.getConditions().isEmpty()) {
            return;
        }
        ExpressionBuilder builder = new ExpressionBuilder(select.getContextHolder());
        for (QueryCondition condition : spec.getConditions()) {
            applyCondition(builder, condition);
        }
        select.getQuery().setWhere(builder.toExpression());
    }

    private void applyCondition(ExpressionBuilder builder, QueryCondition condition) {
        List<Object> values = condition.getValues();
        switch (condition.getOp()) {
            case NE:
                builder.ne(condition.getColumn(), values.get(0));
                return;
            case GT:
                builder.gt(condition.getColumn(), values.get(0));
                return;
            case GE:
                builder.ge(condition.getColumn(), values.get(0));
                return;
            case LT:
                builder.lt(condition.getColumn(), values.get(0));
                return;
            case LE:
                builder.le(condition.getColumn(), values.get(0));
                return;
            case IN:
                builder.in(condition.getColumn(), values);
                return;
            case BETWEEN:
                if (values.size() != 2) {
                    throw new IllegalArgumentException("BETWEEN 查询参数必须包含两个值: " + condition.getFieldName());
                }
                builder.between(condition.getColumn(), values.get(0), values.get(1));
                return;
            case LIKE:
                builder.like(condition.getColumn(), values.get(0));
                return;
            case EQ:
            default:
                builder.eq(condition.getColumn(), values.get(0));
        }
    }

    private void applyGroup(AbsSelect select, QuerySpec spec) {
        for (QueryGroup group : spec.getGroups()) {
            select.getQuery().addGroupByColumn(group.getColumn());
        }
    }

    private void applyOrder(AbsSelect select, QuerySpec spec) {
        for (QueryOrder order : spec.getOrders()) {
            select.getQuery().addOrderByColumn(new OrderClause(order.getColumn(), order.getOrderType()));
        }
    }
}
