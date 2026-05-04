package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 可作为 FROM/JOIN 数据源的 SQL 节点。
 *
 * @author ytora
 * @since 1.0
 */
public interface QuerySource {

    /**
     * 渲染当前数据源。
     *
     * @param context 翻译上下文
     * @return SQL数据源片段
     */
    String renderSource(TranslateContext context);

    /**
     * 获取当前数据源别名。
     *
     * @return 数据源别名；没有时返回 {@code null}
     */
    String getAlias();
}
