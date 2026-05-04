package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.util.SqlRenderUtil;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * SELECT 子查询。
 *
 * <p>既可以作为表达式参与条件构造，也可以作为 FROM/JOIN 数据源。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class SelectSubQuery implements SqlExpression, QuerySource {

    private final SelectQuery query;

    private final String alias;

    /**
     * 创建没有别名的 SELECT 子查询。
     *
     * <p>该形式适合用于 {@code IN (...)}、比较条件等表达式位置；作为 FROM/JOIN 数据源时必须再绑定别名。</p>
     *
     * @param query SELECT 查询模型
     */
    public SelectSubQuery(SelectQuery query) {
        this(query, null);
    }

    /**
     * 创建 SELECT 子查询。
     *
     * @param query SELECT 查询模型
     * @param alias 子查询作为数据源时使用的别名
     */
    public SelectSubQuery(SelectQuery query, String alias) {
        if (query == null) {
            throw new IllegalArgumentException("子查询不能为空");
        }
        this.query = query;
        this.alias = normalizeAlias(alias);
    }

    /**
     * 获取子查询内部的 SELECT 查询模型。
     *
     * @return SELECT 查询模型
     */
    public SelectQuery getQuery() {
        return query;
    }

    /**
     * 将子查询渲染为表达式片段。
     *
     * @param context 翻译上下文，用于继承外层方言并合并子查询参数
     * @return 带括号的子查询 SQL
     */
    @Override
    public String render(TranslateContext context) {
        return "(" + SqlRenderUtil.subQuery(query, context) + ")";
    }

    /**
     * 将子查询渲染为 FROM/JOIN 数据源片段。
     *
     * @param context 翻译上下文
     * @return 带括号和别名的子查询 SQL
     */
    @Override
    public String renderSource(TranslateContext context) {
        if (alias == null) {
            throw new IllegalStateException("子查询作为数据源时必须指定别名");
        }
        return render(context) + " " + alias;
    }

    /**
     * 获取子查询别名。
     *
     * @return 子查询别名；未指定时返回 {@code null}
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * 创建绑定新别名的子查询。
     *
     * @param alias 子查询别名
     * @return 带别名的新子查询对象
     */
    @Override
    public SelectSubQuery as(String alias) {
        return new SelectSubQuery(query, alias);
    }

    /**
     * 规范化子查询别名。
     *
     * @param alias 原始别名
     * @return 去除空白后的别名；空白别名返回 {@code null}
     */
    private static String normalizeAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return null;
        }
        return alias.trim();
    }
}
