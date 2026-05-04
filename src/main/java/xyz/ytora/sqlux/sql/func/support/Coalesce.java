package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * COALESCE 函数。
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class Coalesce extends AbstractSqlFunction {

    /**
     * 创建 COALESCE 函数表达式。
     *
     * @param arguments 候选值列表，至少需要两个参数
     */
    private Coalesce(Object... arguments) {
        super("coalesce", validate(arguments));
    }

    /**
     * 使用普通值或 SQL 表达式创建 COALESCE 函数。
     *
     * @param arguments 候选值列表；普通对象会作为绑定参数
     * @return COALESCE 函数表达式
     */
    public static Coalesce of(Object... arguments) {
        return new Coalesce(arguments);
    }

    /**
     * 使用实体字段作为第一个候选值创建 COALESCE 函数。
     *
     * @param first 第一个候选字段
     * @param remaining 其余候选值或表达式
     * @return COALESCE 函数表达式
     * @param <T> 字段所属实体类型
     * @param <R> 字段值类型
     */
    public static <T, R> Coalesce of(ColFunction<T, R> first, Object... remaining) {
        Object[] arguments = new Object[remaining == null ? 1 : remaining.length + 1];
        arguments[0] = ColumnRef.from(first);
        if (remaining != null) {
            System.arraycopy(remaining, 0, arguments, 1, remaining.length);
        }
        return new Coalesce(arguments);
    }

    /**
     * 使用 SQL 表达式作为第一个候选值创建 COALESCE 函数。
     *
     * @param first 第一个候选表达式
     * @param remaining 其余候选值或表达式
     * @return COALESCE 函数表达式
     */
    public static Coalesce of(SqlExpression first, Object... remaining) {
        if (first == null) {
            throw new IllegalArgumentException("COALESCE 第一个参数不能为空");
        }
        Object[] arguments = FunctionArgs.prepend(first, remaining);
        return new Coalesce(arguments);
    }

    /**
     * 校验 COALESCE 参数数量。
     *
     * @param arguments 候选值列表
     * @return 原始参数列表
     */
    private static Object[] validate(Object... arguments) {
        if (arguments == null || arguments.length < 2) {
            throw new IllegalArgumentException("COALESCE 至少需要两个参数");
        }
        return arguments;
    }
}
