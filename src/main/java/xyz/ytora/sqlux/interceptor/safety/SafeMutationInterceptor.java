package xyz.ytora.sqlux.interceptor.safety;

import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlExecutionContext;
import xyz.ytora.sqlux.sql.condition.*;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UPDATE/DELETE 安全拦截器。
 *
 * <p>默认阻止执行没有 WHERE 条件的 UPDATE 和 DELETE。该拦截器只在执行前检查，
 * 不影响 {@code toSql(...)} 查看 SQL，便于测试、调试和 SQL 预览。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SafeMutationInterceptor implements Interceptor {

    private static final int UNKNOWN_COMPARE = Integer.MIN_VALUE;

    private static final Pattern SIMPLE_RAW_COMPARISON = Pattern.compile(
            "^([+-]?\\d+(?:\\.\\d+)?)\\s*(=|!=|<>|>=|<=|>|<)\\s*([+-]?\\d+(?:\\.\\d+)?)$"
    );

    /**
     * SQL 执行前检查 UPDATE/DELETE 是否带 WHERE。
     *
     * @param context SQL执行上下文
     */
    @Override
    public void beforeExecute(SqlExecutionContext context) {
        if (context == null || context.getSqlResult() == null) {
            return;
        }
        SqlType sqlType = context.getSqlType();
        if (sqlType == SqlType.UPDATE) {
            assertSafeMutation(context.getStatement(), "UPDATE");
        }
        if (sqlType == SqlType.DELETE) {
            assertSafeMutation(context.getStatement(), "DELETE");
        }
    }

    /**
     * 统一检查 UPDATE/DELETE 是否缺失 WHERE 或 WHERE 为显而易见的永真条件。
     *
     * @param statement 源查询模型
     * @param sqlName SQL 名称
     */
    private void assertSafeMutation(Object statement, String sqlName) {
        if (!(statement instanceof UpdateQuery) && !(statement instanceof DeleteQuery)) {
            return;
        }
        ExpressionGroup where = extractWhere(statement);
        if (where == null || where.isEmpty()) {
            throw new IllegalStateException("禁止执行无WHERE的" + sqlName);
        }
        if (evaluate(where) == TruthValue.TRUE) {
            throw new IllegalStateException("禁止执行WHERE恒为真的" + sqlName);
        }
    }

    /**
     * 读取 UPDATE/DELETE 的 WHERE 条件。
     *
     * @param statement 源查询模型
     * @return WHERE 条件
     */
    private ExpressionGroup extractWhere(Object statement) {
        if (statement instanceof UpdateQuery) {
            return ((UpdateQuery) statement).getWhere();
        }
        if (statement instanceof DeleteQuery) {
            return ((DeleteQuery) statement).getWhere();
        }
        return null;
    }

    /**
     * 递归评估表达式组是否可以明确判断为永真、永假或未知。
     *
     * @param group 表达式组
     * @return 真值状态
     */
    private TruthValue evaluate(ExpressionGroup group) {
        TruthValue result = TruthValue.UNKNOWN;
        boolean first = true;
        for (ExpressionPart part : group.getParts()) {
            TruthValue current = evaluate(part.getExpression());
            if (first) {
                result = current;
                first = false;
                continue;
            }
            result = part.getConnector() == xyz.ytora.sqlux.core.enums.Connector.OR
                    ? result.or(current)
                    : result.and(current);
        }
        return result;
    }

    /**
     * 评估单个表达式的真值状态。
     *
     * @param expression 表达式
     * @return 真值状态
     */
    private TruthValue evaluate(Expression expression) {
        if (expression instanceof ExpressionGroup) {
            return evaluate((ExpressionGroup) expression);
        }
        if (expression instanceof ConditionExpression) {
            return evaluateCondition((ConditionExpression) expression);
        }
        if (expression instanceof RawExpression) {
            return evaluateRaw((RawExpression) expression);
        }
        return TruthValue.UNKNOWN;
    }

    /**
     * 评估结构化条件表达式。
     *
     * @param expression 条件表达式
     * @return 真值状态
     */
    private TruthValue evaluateCondition(ConditionExpression expression) {
        String operator = expression.getOperator();
        Object left = expression.getLeft();
        Object right = expression.getRight();
        if (isSameColumn(left, right)) {
            if ("=".equals(operator) || ">=".equals(operator) || "<=".equals(operator)) {
                return TruthValue.TRUE;
            }
            if ("!=".equals(operator) || "<>".equals(operator) || ">".equals(operator) || "<".equals(operator)) {
                return TruthValue.FALSE;
            }
        }
        if (left instanceof ColumnRef || right instanceof ColumnRef) {
            return TruthValue.UNKNOWN;
        }
        int compare = compareConstants(left, right);
        if (compare == UNKNOWN_COMPARE) {
            return TruthValue.UNKNOWN;
        }
        if ("=".equals(operator)) {
            return bool(compare == 0);
        }
        if ("!=".equals(operator) || "<>".equals(operator)) {
            return bool(compare != 0);
        }
        if (">".equals(operator)) {
            return bool(compare > 0);
        }
        if (">=".equals(operator)) {
            return bool(compare >= 0);
        }
        if ("<".equals(operator)) {
            return bool(compare < 0);
        }
        if ("<=".equals(operator)) {
            return bool(compare <= 0);
        }
        return TruthValue.UNKNOWN;
    }

    /**
     * 评估原生 SQL 条件片段中的明显永真/永假形式。
     *
     * @param expression 原生表达式
     * @return 真值状态
     */
    private TruthValue evaluateRaw(RawExpression expression) {
        if (!expression.getParams().isEmpty()) {
            return TruthValue.UNKNOWN;
        }
        String normalized = normalizeRawSql(expression.getSql());
        if ("true".equals(normalized) || "1=1".equals(normalized)) {
            return TruthValue.TRUE;
        }
        if ("false".equals(normalized) || "1=0".equals(normalized) || "1<>1".equals(normalized)
                || "1!=1".equals(normalized)) {
            return TruthValue.FALSE;
        }
        Matcher matcher = SIMPLE_RAW_COMPARISON.matcher(normalized);
        if (!matcher.matches()) {
            return TruthValue.UNKNOWN;
        }
        BigDecimal left = new BigDecimal(matcher.group(1));
        BigDecimal right = new BigDecimal(matcher.group(3));
        int compare = left.compareTo(right);
        String operator = matcher.group(2);
        if ("=".equals(operator)) {
            return bool(compare == 0);
        }
        if ("!=".equals(operator) || "<>".equals(operator)) {
            return bool(compare != 0);
        }
        if (">".equals(operator)) {
            return bool(compare > 0);
        }
        if (">=".equals(operator)) {
            return bool(compare >= 0);
        }
        if ("<".equals(operator)) {
            return bool(compare < 0);
        }
        if ("<=".equals(operator)) {
            return bool(compare <= 0);
        }
        return TruthValue.UNKNOWN;
    }

    /**
     * 判断两个值是否引用同一列。
     *
     * @param left 左值
     * @param right 右值
     * @return 相同列时返回 {@code true}
     */
    private boolean isSameColumn(Object left, Object right) {
        if (!(left instanceof ColumnRef) || !(right instanceof ColumnRef)) {
            return false;
        }
        ColumnRef first = (ColumnRef) left;
        ColumnRef second = (ColumnRef) right;
        return Objects.equals(first.getTableClass(), second.getTableClass())
                && Objects.equals(first.getColumnName(), second.getColumnName())
                && Objects.equals(first.getExplicitAlias(), second.getExplicitAlias())
                && first.isRawExpression() == second.isRawExpression();
    }

    /**
     * 比较两个常量值。
     *
     * @param left 左值
     * @param right 右值
     * @return 比较结果；无法比较时返回 {@link #UNKNOWN_COMPARE}
     */
    private int compareConstants(Object left, Object right) {
        if (left == null || right == null) {
            return UNKNOWN_COMPARE;
        }
        if (left instanceof Number && right instanceof Number) {
            return new BigDecimal(left.toString()).compareTo(new BigDecimal(right.toString()));
        }
        if (left instanceof CharSequence || right instanceof CharSequence) {
            return String.valueOf(left).compareTo(String.valueOf(right));
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            return Boolean.compare((Boolean) left, (Boolean) right);
        }
        if (left.getClass().equals(right.getClass()) && left instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> comparable = (Comparable<Object>) left;
            return comparable.compareTo(right);
        }
        if (Objects.equals(left, right)) {
            return 0;
        }
        return UNKNOWN_COMPARE;
    }

    /**
     * 将原生 SQL 条件片段规范化为便于识别的形式。
     *
     * @param sql 原始 SQL 条件片段
     * @return 规范化文本
     */
    private String normalizeRawSql(String sql) {
        if (sql == null) {
            return "";
        }
        String normalized = sql.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        while (normalized.startsWith("(") && normalized.endsWith(")") && normalized.length() > 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 布尔值转真值状态。
     *
     * @param value 布尔值
     * @return 真值状态
     */
    private TruthValue bool(boolean value) {
        return value ? TruthValue.TRUE : TruthValue.FALSE;
    }

    /**
     * 三值逻辑枚举。
     */
    private enum TruthValue {
        TRUE,
        FALSE,
        UNKNOWN;

        private TruthValue and(TruthValue other) {
            if (this == FALSE || other == FALSE) {
                return FALSE;
            }
            if (this == TRUE && other == TRUE) {
                return TRUE;
            }
            return UNKNOWN;
        }

        private TruthValue or(TruthValue other) {
            if (this == TRUE || other == TRUE) {
                return TRUE;
            }
            if (this == FALSE && other == FALSE) {
                return FALSE;
            }
            return UNKNOWN;
        }
    }
}
