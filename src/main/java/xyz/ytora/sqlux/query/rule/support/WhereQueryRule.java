package xyz.ytora.sqlux.query.rule.support;

import xyz.ytora.sqlux.query.*;
import xyz.ytora.sqlux.query.model.QueryCondition;
import xyz.ytora.sqlux.query.model.QueryField;
import xyz.ytora.sqlux.query.rule.QueryRule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 解析 WHERE 条件参数。
 *
 * @author ytora
 * @since 1.0
 */
public class WhereQueryRule implements QueryRule {

    private final List<OpSuffix> suffixes = Arrays.asList(
            new OpSuffix("_between", QueryOp.BETWEEN),
            new OpSuffix("_like", QueryOp.LIKE),
            new OpSuffix("_in", QueryOp.IN),
            new OpSuffix("_ne", QueryOp.NE),
            new OpSuffix("_gt", QueryOp.GT),
            new OpSuffix("_ge", QueryOp.GE),
            new OpSuffix("_lt", QueryOp.LT),
            new OpSuffix("_le", QueryOp.LE)
    );

    @Override
    public void parse(QueryBuildContext<?> context) {
        if (context.getParams() == null) {
            return;
        }
        for (Map.Entry<String, ?> entry : context.getParams().entrySet()) {
            String key = entry.getKey();
            if (isReserved(key)) {
                continue;
            }
            FieldOp fieldOp = parseFieldOp(key);
            QueryField field = context.findField(fieldOp.fieldName);
            if (field == null) {
                continue;
            }
            List<Object> values = parseValues(field, fieldOp.op, entry.getValue());
            if (values.isEmpty()) {
                continue;
            }
            QueryCondition condition = new QueryCondition(field.getFieldName(), field.getColumnName(),
                    field.toColumnRef(), fieldOp.op, values);
            context.getSpec().addCondition(context.getCallback().onWhere(condition));
        }
    }

    private boolean isReserved(String key) {
        return SelectColumnQueryRule.PARAM_NAME.equals(key)
                || DistinctQueryRule.PARAM_NAME.equals(key)
                || GroupColumnQueryRule.PARAM_NAME.equals(key)
                || OrderColumnQueryRule.PARAM_NAME.equals(key);
    }

    private FieldOp parseFieldOp(String key) {
        String fieldName = key == null ? "" : key.trim();
        for (OpSuffix suffix : suffixes) {
            if (fieldName.endsWith(suffix.suffix)) {
                return new FieldOp(fieldName.substring(0, fieldName.length() - suffix.suffix.length()), suffix.op);
            }
        }
        return new FieldOp(fieldName, QueryOp.EQ);
    }

    private List<Object> parseValues(QueryField field, QueryOp op, Object value) {
        if (op == QueryOp.IN || op == QueryOp.BETWEEN) {
            return QueryParamUtil.convertValues(field, value);
        }
        Object converted = QueryParamUtil.convertValue(field, QueryParamUtil.firstString(value));
        return converted == null ? java.util.Collections.emptyList() : java.util.Collections.singletonList(converted);
    }

    private static class OpSuffix {

        private final String suffix;

        private final QueryOp op;

        private OpSuffix(String suffix, QueryOp op) {
            this.suffix = suffix;
            this.op = op;
        }
    }

    private static class FieldOp {

        private final String fieldName;

        private final QueryOp op;

        private FieldOp(String fieldName, QueryOp op) {
            this.fieldName = fieldName;
            this.op = op;
        }
    }
}
