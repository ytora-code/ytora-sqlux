package xyz.ytora.sqlux.sql.stage.raw;

import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.SqlResult;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 原生 SQL 阶段的共享工具。
 */
final class RawSqlSupport {

    private RawSqlSupport() {
    }

    static SqlResult toSqlResult(String sql, SqlType sqlType, Object... params) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("原生SQL不能为空");
        }
        List<Object> flatParams = flattenParams(params);
        int placeholderCount = countPositionalPlaceholders(sql);
        if (placeholderCount == flatParams.size()) {
            return new SqlResult(sql, flatParams, sqlType);
        }
        ResolvedRawSql resolved = resolvePositional(sql, params);
        return new SqlResult(resolved.sql, resolved.params, sqlType);
    }

    static SqlResult toSqlResult(String sql, SqlType sqlType, Map<String, ?> params) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("原生SQL不能为空");
        }
        ResolvedRawSql resolved = resolveNamed(sql, params);
        return new SqlResult(resolved.sql, resolved.params, sqlType);
    }

    static SqlType detectMutationType(String sql) {
        if (sql == null) {
            return SqlType.UNKNOWN;
        }
        String normalized = sql.trim();
        if (normalized.isEmpty()) {
            return SqlType.UNKNOWN;
        }
        String upper = normalized.toUpperCase();
        if (upper.startsWith("INSERT")) {
            return SqlType.INSERT;
        }
        if (upper.startsWith("UPDATE")) {
            return SqlType.UPDATE;
        }
        if (upper.startsWith("DELETE")) {
            return SqlType.DELETE;
        }
        return SqlType.UNKNOWN;
    }

    private static ResolvedRawSql resolvePositional(String sql, Object[] params) {
        if (params == null || params.length == 0) {
            return new ResolvedRawSql(sql, Collections.emptyList());
        }
        StringBuilder out = new StringBuilder(sql.length() + 16);
        List<Object> actualParams = new ArrayList<>();
        int paramIndex = 0;
        ScanState state = ScanState.NORMAL;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            switch (state) {
                case NORMAL:
                    if (current == '\'') {
                        state = ScanState.SINGLE_QUOTE;
                        out.append(current);
                        continue;
                    }
                    if (current == '"') {
                        state = ScanState.DOUBLE_QUOTE;
                        out.append(current);
                        continue;
                    }
                    if (current == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                        state = ScanState.LINE_COMMENT;
                        out.append(current).append(sql.charAt(i + 1));
                        i++;
                        continue;
                    }
                    if (current == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                        state = ScanState.BLOCK_COMMENT;
                        out.append(current).append(sql.charAt(i + 1));
                        i++;
                        continue;
                    }
                    if (current == '?') {
                        if (paramIndex >= params.length) {
                            out.append(current);
                            continue;
                        }
                        appendExpandedParam(sql, i, i + 1, params[paramIndex], out, actualParams);
                        paramIndex++;
                        continue;
                    }
                    out.append(current);
                    continue;
                case SINGLE_QUOTE:
                    out.append(current);
                    if (current == '\'' && !isEscapedSingleQuote(sql, i)) {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case DOUBLE_QUOTE:
                    out.append(current);
                    if (current == '"') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case LINE_COMMENT:
                    out.append(current);
                    if (current == '\n' || current == '\r') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case BLOCK_COMMENT:
                    out.append(current);
                    if (current == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
                        out.append(sql.charAt(i + 1));
                        i++;
                        state = ScanState.NORMAL;
                    }
                    continue;
                default:
                    throw new IllegalStateException("未知的 SQL 扫描状态: " + state);
            }
        }
        return new ResolvedRawSql(out.toString(), actualParams);
    }

    private static List<Object> flattenParams(Object[] params) {
        if (params == null || params.length == 0) {
            return Collections.emptyList();
        }
        List<Object> actualParams = new ArrayList<>();
        for (Object param : params) {
            ParameterValue parameterValue = ParameterValue.of(param);
            if (!parameterValue.expandable) {
                actualParams.add(parameterValue.singleValue);
                continue;
            }
            actualParams.addAll(parameterValue.values);
        }
        return actualParams;
    }

    private static int countPositionalPlaceholders(String sql) {
        int count = 0;
        ScanState state = ScanState.NORMAL;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            switch (state) {
                case NORMAL:
                    if (current == '\'') {
                        state = ScanState.SINGLE_QUOTE;
                        continue;
                    }
                    if (current == '"') {
                        state = ScanState.DOUBLE_QUOTE;
                        continue;
                    }
                    if (current == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                        state = ScanState.LINE_COMMENT;
                        i++;
                        continue;
                    }
                    if (current == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                        state = ScanState.BLOCK_COMMENT;
                        i++;
                        continue;
                    }
                    if (current == '?') {
                        count++;
                    }
                    continue;
                case SINGLE_QUOTE:
                    if (current == '\'' && !isEscapedSingleQuote(sql, i)) {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case DOUBLE_QUOTE:
                    if (current == '"') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case LINE_COMMENT:
                    if (current == '\n' || current == '\r') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case BLOCK_COMMENT:
                    if (current == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
                        i++;
                        state = ScanState.NORMAL;
                    }
                    continue;
                default:
                    throw new IllegalStateException("未知的 SQL 扫描状态: " + state);
            }
        }
        return count;
    }

    private static ResolvedRawSql resolveNamed(String sql, Map<String, ?> params) {
        Map<String, ?> actualNamedParams = params == null ? Collections.emptyMap() : params;
        StringBuilder out = new StringBuilder(sql.length() + 16);
        List<Object> actualParams = new ArrayList<>();
        ScanState state = ScanState.NORMAL;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            switch (state) {
                case NORMAL:
                    if (current == '\'') {
                        state = ScanState.SINGLE_QUOTE;
                        out.append(current);
                        continue;
                    }
                    if (current == '"') {
                        state = ScanState.DOUBLE_QUOTE;
                        out.append(current);
                        continue;
                    }
                    if (current == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                        state = ScanState.LINE_COMMENT;
                        out.append(current).append(sql.charAt(i + 1));
                        i++;
                        continue;
                    }
                    if (current == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                        state = ScanState.BLOCK_COMMENT;
                        out.append(current).append(sql.charAt(i + 1));
                        i++;
                        continue;
                    }
                    if (current == ':' && i + 1 < sql.length() && sql.charAt(i + 1) == ':') {
                        out.append("::");
                        i++;
                        continue;
                    }
                    if (current == ':' && i + 1 < sql.length() && isNamedParamStart(sql.charAt(i + 1))) {
                        int end = i + 2;
                        while (end < sql.length() && isNamedParamPart(sql.charAt(end))) {
                            end++;
                        }
                        String name = sql.substring(i + 1, end);
                        if (!actualNamedParams.containsKey(name)) {
                            throw new IllegalArgumentException("命名参数不存在: " + name);
                        }
                        appendExpandedParam(sql, i, end, actualNamedParams.get(name), out, actualParams);
                        i = end - 1;
                        continue;
                    }
                    out.append(current);
                    continue;
                case SINGLE_QUOTE:
                    out.append(current);
                    if (current == '\'' && !isEscapedSingleQuote(sql, i)) {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case DOUBLE_QUOTE:
                    out.append(current);
                    if (current == '"') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case LINE_COMMENT:
                    out.append(current);
                    if (current == '\n' || current == '\r') {
                        state = ScanState.NORMAL;
                    }
                    continue;
                case BLOCK_COMMENT:
                    out.append(current);
                    if (current == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
                        out.append(sql.charAt(i + 1));
                        i++;
                        state = ScanState.NORMAL;
                    }
                    continue;
                default:
                    throw new IllegalStateException("未知的 SQL 扫描状态: " + state);
            }
        }
        return new ResolvedRawSql(out.toString(), actualParams);
    }

    private static void appendExpandedParam(String sql, int start, int end, Object value,
                                            StringBuilder out, List<Object> actualParams) {
        ParameterValue parameterValue = ParameterValue.of(value);
        if (!parameterValue.expandable) {
            out.append('?');
            actualParams.add(parameterValue.singleValue);
            return;
        }
        boolean wrapped = isWrappedInParentheses(sql, start, end);
        if (parameterValue.values.isEmpty()) {
            if (isNotInOperator(sql, start)) {
                throw new IllegalArgumentException("NOT IN 条件不能为空集合");
            }
            out.append(wrapped ? "NULL" : "(NULL)");
            return;
        }
        if (!wrapped) {
            out.append('(');
        }
        for (int i = 0; i < parameterValue.values.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append('?');
            actualParams.add(parameterValue.values.get(i));
        }
        if (!wrapped) {
            out.append(')');
        }
    }

    private static boolean isWrappedInParentheses(String sql, int start, int end) {
        int left = start - 1;
        while (left >= 0 && Character.isWhitespace(sql.charAt(left))) {
            left--;
        }
        int right = end;
        while (right < sql.length() && Character.isWhitespace(sql.charAt(right))) {
            right++;
        }
        return left >= 0 && right < sql.length() && sql.charAt(left) == '(' && sql.charAt(right) == ')';
    }

    private static boolean isNotInOperator(String sql, int paramStart) {
        int index = paramStart - 1;
        while (index >= 0 && Character.isWhitespace(sql.charAt(index))) {
            index--;
        }
        if (index < 1) {
            return false;
        }
        int inEnd = index + 1;
        while (index >= 0 && Character.isLetter(sql.charAt(index))) {
            index--;
        }
        String inToken = sql.substring(index + 1, inEnd);
        if (!"IN".equalsIgnoreCase(inToken)) {
            return false;
        }
        while (index >= 0 && Character.isWhitespace(sql.charAt(index))) {
            index--;
        }
        if (index < 2) {
            return false;
        }
        int notEnd = index + 1;
        while (index >= 0 && Character.isLetter(sql.charAt(index))) {
            index--;
        }
        String notToken = sql.substring(index + 1, notEnd);
        return "NOT".equalsIgnoreCase(notToken);
    }

    private static boolean isEscapedSingleQuote(String sql, int index) {
        return index + 1 < sql.length() && sql.charAt(index + 1) == '\'';
    }

    private static boolean isNamedParamStart(char value) {
        return Character.isLetter(value) || value == '_';
    }

    private static boolean isNamedParamPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    private enum ScanState {
        NORMAL,
        SINGLE_QUOTE,
        DOUBLE_QUOTE,
        LINE_COMMENT,
        BLOCK_COMMENT
    }

    private static final class ResolvedRawSql {

        private final String sql;

        private final List<Object> params;

        private ResolvedRawSql(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    private static final class ParameterValue {

        private final boolean expandable;

        private final Object singleValue;

        private final List<Object> values;

        private ParameterValue(boolean expandable, Object singleValue, List<Object> values) {
            this.expandable = expandable;
            this.singleValue = singleValue;
            this.values = values;
        }

        private static ParameterValue of(Object value) {
            if (value == null) {
                return new ParameterValue(false, null, Collections.emptyList());
            }
            if (value instanceof Collection) {
                return new ParameterValue(true, null, new ArrayList<>((Collection<?>) value));
            }
            Class<?> type = value.getClass();
            if (type.isArray()) {
                int length = Array.getLength(value);
                List<Object> values = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    values.add(Array.get(value, i));
                }
                return new ParameterValue(true, null, values);
            }
            return new ParameterValue(false, value, Collections.emptyList());
        }
    }
}
