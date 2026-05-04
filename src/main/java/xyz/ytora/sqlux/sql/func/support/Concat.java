package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * CONCAT 函数。
 *
 * @author ytora
 * @since 1.0
 */
public final class Concat extends AbstractSqlFunction {

    /**
     * 创建 CONCAT 函数表达式。
     *
     * @param arguments 要拼接的参数列表
     */
    private Concat(Object... arguments) {
        super("concat", validate(arguments));
    }

    /**
     * 使用普通值或 SQL 表达式创建 CONCAT 函数。
     *
     * @param arguments 拼接参数；普通对象会作为绑定参数
     * @return CONCAT 函数表达式
     */
    public static Concat of(Object... arguments) {
        return new Concat(arguments);
    }

    /**
     * 使用实体字段作为第一个拼接参数创建 CONCAT 函数。
     *
     * @param first 第一个拼接字段
     * @param remaining 其余拼接参数
     * @return CONCAT 函数表达式
     * @param <T> 字段所属实体类型
     * @param <R> 字段值类型
     */
    public static <T, R> Concat of(ColFunction<T, R> first, Object... remaining) {
        Object[] arguments = new Object[remaining == null ? 1 : remaining.length + 1];
        arguments[0] = ColumnRef.from(first);
        if (remaining != null) {
            System.arraycopy(remaining, 0, arguments, 1, remaining.length);
        }
        return new Concat(arguments);
    }

    /**
     * 校验 CONCAT 参数数量。
     *
     * @param arguments 拼接参数列表
     * @return 原始参数列表
     */
    private static Object[] validate(Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            throw new IllegalArgumentException("CONCAT 至少需要一个参数");
        }
        return arguments;
    }
}
