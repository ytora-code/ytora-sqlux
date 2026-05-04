package xyz.ytora.sqlux.sql.func;

import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * SQL 函数参数工具。
 */
public final class FunctionArgs {

    private FunctionArgs() {
    }

    public static ColumnRef column(ColFunction<?, ?> column, String name) {
        if (column == null) {
            throw new IllegalArgumentException(name + " 字段不能为空");
        }
        return ColumnRef.from(column);
    }

    public static SqlExpression expression(SqlExpression expression, String name) {
        if (expression == null) {
            throw new IllegalArgumentException(name + " 表达式不能为空");
        }
        return expression;
    }

    public static Object[] prepend(Object first, Object... remaining) {
        Object[] arguments = new Object[remaining == null ? 1 : remaining.length + 1];
        arguments[0] = first;
        if (remaining != null) {
            System.arraycopy(remaining, 0, arguments, 1, remaining.length);
        }
        return arguments;
    }

    public static Object[] requireAtLeast(String name, int min, Object... arguments) {
        if (arguments == null || arguments.length < min) {
            throw new IllegalArgumentException(name + " 至少需要" + min + "个参数");
        }
        return arguments;
    }
}
