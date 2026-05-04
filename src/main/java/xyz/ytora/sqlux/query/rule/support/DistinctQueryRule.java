package xyz.ytora.sqlux.query.rule.support;

import xyz.ytora.sqlux.query.QueryBuildContext;
import xyz.ytora.sqlux.query.QueryParamUtil;
import xyz.ytora.sqlux.query.rule.QueryRule;

/**
 * 解析去重参数。
 *
 * @author ytora
 * @since 1.0
 */
public class DistinctQueryRule implements QueryRule {

    public static final String PARAM_NAME = "sql_distinct_enable";

    @Override
    public void parse(QueryBuildContext<?> context) {
        if (context.getParams() == null || !context.getParams().containsKey(PARAM_NAME)) {
            return;
        }
        context.getSpec().setDistinct(QueryParamUtil.isTrue(context.getParams().get(PARAM_NAME)));
    }
}
