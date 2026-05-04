package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * FLOOR 向下取整函数。
 */
@SuppressWarnings("overloads")
public final class Floor extends AbstractSqlFunction {

    private Floor(Object argument) {
        super("floor", argument);
    }

    public static <T, R> Floor of(ColFunction<T, R> column) {
        return new Floor(FunctionArgs.column(column, "FLOOR"));
    }

    public static Floor of(SqlExpression expression) {
        return new Floor(FunctionArgs.expression(expression, "FLOOR"));
    }

    public static Floor of(Object value) {
        return new Floor(value);
    }
}
