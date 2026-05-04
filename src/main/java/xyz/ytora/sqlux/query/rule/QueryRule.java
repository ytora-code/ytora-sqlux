package xyz.ytora.sqlux.query.rule;

import xyz.ytora.sqlux.query.QueryBuildContext;

/**
 * 查询参数解析规则。
 *
 * @author ytora
 * @since 1.0
 */
public interface QueryRule {

    /**
     * 解析查询参数并写入查询规格。
     *
     * @param context 查询参数上下文
     */
    void parse(QueryBuildContext<?> context);
}
