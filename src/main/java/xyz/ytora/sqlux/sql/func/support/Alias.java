package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 别名占位表达式。
 *
 * <p>该表达式本身不生成任何函数调用，只负责把字段方法引用或已有表达式
 * 包装为可继续调用 {@link #as(String)} 的 {@link SqlExpression}。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class Alias implements SqlExpression {

    private final SqlExpression delegate;

    private Alias(SqlExpression delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("别名表达式不能为空");
        }
        this.delegate = delegate;
    }

    /**
     * 将字段方法引用包装为可别名的表达式。
     *
     * @param column 实体字段方法引用
     * @return 可继续调用 {@code as(...)} 的表达式
     * @param <T> 实体类型
     * @param <R> 字段类型
     */
    public static <T, R> Alias of(ColFunction<T, R> column) {
        return new Alias(ColumnRef.from(column));
    }

    /**
     * 将已有 SQL 表达式包装为可别名的表达式。
     *
     * @param expression SQL 表达式
     * @return 可继续调用 {@code as(...)} 的表达式
     */
    public static Alias of(SqlExpression expression) {
        return new Alias(expression);
    }

    @Override
    public String render(TranslateContext context) {
        return delegate.render(context);
    }
}
