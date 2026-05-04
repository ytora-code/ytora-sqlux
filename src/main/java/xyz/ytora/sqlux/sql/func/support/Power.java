package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * POWER 幂函数。
 */
@SuppressWarnings("overloads")
public final class Power extends AbstractSqlFunction {

    private Power(Object value, Object exponent) {
        super("power", value, exponent);
    }

    public static <T, R> Power of(ColFunction<T, R> column, Object exponent) {
        return new Power(FunctionArgs.column(column, "POWER"), exponent);
    }

    public static Power of(SqlExpression expression, Object exponent) {
        return new Power(FunctionArgs.expression(expression, "POWER"), exponent);
    }

    public static Power of(Object value, Object exponent) {
        return new Power(value, exponent);
    }
}
