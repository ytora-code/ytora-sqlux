package xyz.ytora.sqlux.query.rule.support;

import xyz.ytora.sqlux.query.QueryBuildContext;
import xyz.ytora.sqlux.query.model.QueryColumn;
import xyz.ytora.sqlux.query.model.QueryField;
import xyz.ytora.sqlux.query.QueryParamUtil;
import xyz.ytora.sqlux.query.rule.QueryRule;

/**
 * 解析查询字段参数。
 *
 * @author ytora
 * @since 1.0
 */
public class SelectColumnQueryRule implements QueryRule {

    public static final String PARAM_NAME = "sql_query_col";

    @Override
    public void parse(QueryBuildContext<?> context) {
        if (context.getParams() == null) {
            return;
        }
        for (String name : QueryParamUtil.splitCsv(context.getParams().get(PARAM_NAME))) {
            QueryField field = context.requireField(name);
            QueryColumn column = new QueryColumn(field.getFieldName(), field.getColumnName(), field.toColumnRef());
            context.getSpec().addColumn(context.getCallback().onQueryColumn(column));
        }
    }
}
