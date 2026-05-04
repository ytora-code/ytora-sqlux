package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * ABS 数值绝对值函数。
 */
@SuppressWarnings("overloads")
public final class Abs extends AbstractSqlFunction {

    private Abs(Object argument) {
        super("abs", argument);
    }

    public static <T, R> Abs of(ColFunction<T, R> column) {
        return new Abs(FunctionArgs.column(column, "ABS"));
    }

    public static Abs of(SqlExpression expression) {
        return new Abs(FunctionArgs.expression(expression, "ABS"));
    }

    public static Abs of(Object value) {
        return new Abs(value);
    }
}
