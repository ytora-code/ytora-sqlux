package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * ROUND 数值四舍五入函数。
 */
@SuppressWarnings("overloads")
public final class Round extends AbstractSqlFunction {

    private Round(Object... arguments) {
        super("round", FunctionArgs.requireAtLeast("ROUND", 1, arguments));
    }

    public static <T, R> Round of(ColFunction<T, R> column) {
        return new Round(FunctionArgs.column(column, "ROUND"));
    }

    public static <T, R> Round of(ColFunction<T, R> column, int scale) {
        return new Round(FunctionArgs.column(column, "ROUND"), scale);
    }

    public static Round of(SqlExpression expression) {
        return new Round(FunctionArgs.expression(expression, "ROUND"));
    }

    public static Round of(SqlExpression expression, int scale) {
        return new Round(FunctionArgs.expression(expression, "ROUND"), scale);
    }

    public static Round of(Object value, int scale) {
        return new Round(value, scale);
    }
}
