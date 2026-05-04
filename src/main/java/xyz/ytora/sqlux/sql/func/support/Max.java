package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * MAX 聚合函数。
 */
@SuppressWarnings("overloads")
public final class Max extends AbstractSqlFunction {

    private Max(Object argument) {
        super("max", argument);
    }

    public static <T, R> Max of(ColFunction<T, R> column) {
        return new Max(FunctionArgs.column(column, "MAX"));
    }

    public static Max of(SqlExpression expression) {
        return new Max(FunctionArgs.expression(expression, "MAX"));
    }
}
