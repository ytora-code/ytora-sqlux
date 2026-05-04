package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * TRIM 去除首尾空白函数。
 */
@SuppressWarnings("overloads")
public final class Trim extends AbstractSqlFunction {

    private Trim(Object argument) {
        super("trim", argument);
    }

    public static <T, R> Trim of(ColFunction<T, R> column) {
        return new Trim(FunctionArgs.column(column, "TRIM"));
    }

    public static Trim of(SqlExpression expression) {
        return new Trim(FunctionArgs.expression(expression, "TRIM"));
    }
}
