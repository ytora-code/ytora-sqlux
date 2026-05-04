package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.sql.condition.ConditionExpression;
import xyz.ytora.sqlux.sql.condition.Expression;
import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.condition.ExpressionPart;
import xyz.ytora.sqlux.sql.condition.RawExpression;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 条件表达式翻译器。
 *
 * @author ytora
 * @since 1.0
 */
public class ExpressionTranslator {

    /**
     * 翻译表达式组。
     *
     * @param group 条件表达式组；为空时表示没有条件
     * @param context 翻译上下文，用于渲染字段并收集参数
     * @return 可直接拼接到 WHERE、ON 或 HAVING 后面的 SQL 片段
     */
    public String translate(ExpressionGroup group, TranslateContext context) {
        if (group == null || group.isEmpty()) {
            return "";
        }
        return translateGroup(group, context, false);
    }

    /**
     * 翻译表达式组内部的所有表达式片段。
     *
     * @param group 条件表达式组
     * @param context 翻译上下文
     * @param nested 是否作为嵌套分组输出括号
     * @return 表达式组 SQL 片段
     */
    private String translateGroup(ExpressionGroup group, TranslateContext context, boolean nested) {
        List<String> items = new ArrayList<>();
        int index = 0;
        for (ExpressionPart part : group.getParts()) {
            String expressionSql = translateExpression(part.getExpression(), context);
            if (expressionSql.isEmpty()) {
                continue;
            }
            if (index == 0) {
                items.add(expressionSql);
            } else {
                items.add(part.getConnector().name() + " " + expressionSql);
            }
            index++;
        }
        String sql = SqlRenderUtil.join(items, " ");
        if (nested && !sql.isEmpty()) {
            return "(" + sql + ")";
        }
        return sql;
    }

    /**
     * 按表达式实际类型分派翻译逻辑。
     *
     * @param expression 条件表达式
     * @param context 翻译上下文
     * @return 条件 SQL 片段
     */
    private String translateExpression(Expression expression, TranslateContext context) {
        if (expression instanceof ConditionExpression) {
            return translateCondition((ConditionExpression) expression, context);
        }
        if (expression instanceof RawExpression) {
            RawExpression rawExpression = (RawExpression) expression;
            return SqlRenderUtil.raw(rawExpression.getSql(), rawExpression.getParams(), context);
        }
        if (expression instanceof ExpressionGroup) {
            return translateGroup((ExpressionGroup) expression, context, true);
        }
        return "";
    }

    /**
     * 翻译原子条件表达式。
     *
     * <p>这里集中处理 {@code IN}、{@code BETWEEN}、{@code IS NULL}、{@code EXISTS} 等特殊运算符。</p>
     *
     * @param expression 原子条件表达式
     * @param context 翻译上下文
     * @return 条件 SQL 片段
     */
    private String translateCondition(ConditionExpression expression, TranslateContext context) {
        String left = translateValue(expression.getLeft(), context);
        String operator = expression.getOperator();
        if ("EXISTS".equals(operator) || "NOT EXISTS".equals(operator)) {
            return operator + " " + left;
        }
        if ("IS NULL".equals(operator) || "IS NOT NULL".equals(operator)) {
            return left + " " + operator;
        }
        if ("IN".equals(operator) || "NOT IN".equals(operator)) {
            if ("NOT IN".equals(operator) && toList(expression.getRight()).isEmpty()) {
                throw new IllegalArgumentException("NOT IN 条件不能为空集合");
            }
            return left + " " + operator + " " + translateInValues(expression.getRight(), context);
        }
        if ("BETWEEN".equals(operator) || "NOT BETWEEN".equals(operator)) {
            List<Object> values = toList(expression.getRight());
            if (values.size() != 2) {
                throw new IllegalArgumentException("BETWEEN 条件必须且只能包含两个值");
            }
            return left + " " + operator + " " + SqlRenderUtil.value(values.get(0), context)
                    + " AND " + SqlRenderUtil.value(values.get(1), context);
        }
        String right = translateValue(expression.getRight(), context);
        return left + " " + operator + " " + right;
    }

    /**
     * 翻译 IN/NOT IN 右侧值列表。
     *
     * @param value 集合、数组、单值或子查询表达式
     * @param context 翻译上下文
     * @return 带括号的 IN 值列表或子查询 SQL
     */
    private String translateInValues(Object value, TranslateContext context) {
        List<Object> values = toList(value);
        if (values.isEmpty()) {
            return "(NULL)";
        }
        if (values.size() == 1 && values.get(0) instanceof SqlExpression) {
            return translateValue(values.get(0), context);
        }
        List<String> placeholders = new ArrayList<>();
        for (Object item : values) {
            placeholders.add(SqlRenderUtil.value(item, context));
        }
        return "(" + SqlRenderUtil.join(placeholders, ", ") + ")";
    }

    /**
     * 将数组、Iterable 或单个值统一转换为列表。
     *
     * @param value 原始值
     * @return 规范化后的值列表
     */
    private List<Object> toList(Object value) {
        List<Object> values = new ArrayList<>();
        if (value == null) {
            return values;
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                values.add(item);
            }
            return values;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                values.add(Array.get(value, i));
            }
            return values;
        }
        values.add(value);
        return values;
    }

    /**
     * 翻译条件中的单个值。
     *
     * @param value 字段、函数、子查询等 SQL 表达式或普通参数值
     * @param context 翻译上下文
     * @return SQL 表达式片段或参数占位符
     */
    private String translateValue(Object value, TranslateContext context) {
        if (value instanceof SqlExpression) {
            return SqlRenderUtil.expression((SqlExpression) value, context);
        }
        return context.addParam(value);
    }
}
