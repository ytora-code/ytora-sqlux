package xyz.ytora.sqlux.sql.func;

import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 带别名的 SQL 表达式。
 *
 * <p>在 SELECT 子句中会输出 {@code AS alias}，在其他位置仍按原表达式渲染，
 * 以便别名表达式可以继续参与排序、分组或条件构造。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class AliasedExpression implements SqlExpression {

    private final SqlExpression delegate;

    private final String alias;

    /**
     * 创建带别名的表达式包装器。
     *
     * @param delegate 被包装的原始表达式
     * @param alias SELECT 子句中输出的字段别名
     */
    AliasedExpression(SqlExpression delegate, String alias) {
        if (delegate == null) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("别名不能为空");
        }
        this.delegate = delegate;
        this.alias = alias.trim();
    }

    /**
     * 在非 SELECT 项位置渲染原始表达式。
     *
     * @param context 翻译上下文
     * @return 不带别名的 SQL 表达式
     */
    @Override
    public String render(TranslateContext context) {
        return delegate.render(context);
    }

    /**
     * 在 SELECT 项位置渲染表达式及别名。
     *
     * @param context 翻译上下文
     * @return 带 {@code AS alias} 的 SELECT 项
     */
    @Override
    public String renderSelectItem(TranslateContext context) {
        return delegate.render(context) + " AS " + alias;
    }

    /**
     * 基于同一个原始表达式创建新的别名包装。
     *
     * @param alias 新别名
     * @return 带新别名的表达式
     */
    @Override
    public SqlExpression as(String alias) {
        return new AliasedExpression(delegate, alias);
    }
}
