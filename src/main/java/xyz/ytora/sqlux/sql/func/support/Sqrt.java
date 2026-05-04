package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * SQRT 平方根函数。
 */
@SuppressWarnings("overloads")
public final class Sqrt extends AbstractSqlFunction {

    private Sqrt(Object argument) {
        super("sqrt", argument);
    }

    public static <T, R> Sqrt of(ColFunction<T, R> column) {
        return new Sqrt(FunctionArgs.column(column, "SQRT"));
    }

    public static Sqrt of(SqlExpression expression) {
        return new Sqrt(FunctionArgs.expression(expression, "SQRT"));
    }

    public static Sqrt of(Object value) {
        return new Sqrt(value);
    }
}
