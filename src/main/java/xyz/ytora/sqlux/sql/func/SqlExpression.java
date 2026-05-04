package xyz.ytora.sqlux.sql.func;

import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 可渲染的 SQL 表达式。
 *
 * <p>字段引用、函数调用、别名表达式都应实现该接口，从而在 SELECT、WHERE、GROUP BY、
 * ORDER BY、HAVING 等位置统一使用。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface SqlExpression {

    /**
     * 渲染当前表达式。
     *
     * @param context 翻译上下文
     * @return SQL表达式片段
     */
    String render(TranslateContext context);

    /**
     * 渲染 SELECT 项。
     *
     * <p>默认与普通表达式渲染一致；带别名的表达式会覆写该行为。</p>
     *
     * @param context 翻译上下文
     * @return SELECT项SQL片段
     */
    default String renderSelectItem(TranslateContext context) {
        return render(context);
    }

    /**
     * 为当前表达式追加别名。
     *
     * @param alias 别名
     * @return 带别名的新表达式
     */
    default SqlExpression as(String alias) {
        return new AliasedExpression(this, alias);
    }
}
