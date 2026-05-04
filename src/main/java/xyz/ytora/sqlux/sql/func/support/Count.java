package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * COUNT 函数。
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class Count extends AbstractSqlFunction {

    private final boolean countAll;

    /**
     * 创建 COUNT 函数表达式。
     *
     * @param countAll 是否渲染为 {@code count(*)}
     * @param arguments 非全量计数时使用的计数表达式参数
     */
    private Count(boolean countAll, Object... arguments) {
        super("count", arguments);
        this.countAll = countAll;
    }

    /**
     * 创建 {@code count(*)} 表达式。
     *
     * @return COUNT 全行计数表达式
     */
    public static Count of() {
        return new Count(true);
    }

    /**
     * 创建针对实体字段的 COUNT 表达式。
     *
     * @param column 要计数的实体字段
     * @return COUNT 字段表达式
     * @param <T> 字段所属实体类型
     * @param <R> 字段值类型
     */
    public static <T, R> Count of(ColFunction<T, R> column) {
        return new Count(false, ColumnRef.from(column));
    }

    /**
     * 创建针对 SQL 表达式的 COUNT 表达式。
     *
     * @param expression 要计数的 SQL 表达式
     * @return COUNT 表达式
     */
    public static Count of(SqlExpression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("COUNT 表达式不能为空");
        }
        return new Count(false, expression);
    }

    /**
     * 渲染 COUNT 函数。
     *
     * @param context 翻译上下文
     * @return {@code count(*)} 或带参数的 {@code count(expr)}
     */
    @Override
    public String render(TranslateContext context) {
        if (countAll) {
            return "count(*)";
        }
        return super.render(context);
    }
}
