package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * AVG 聚合函数。
 */
@SuppressWarnings("overloads")
public final class Avg extends AbstractSqlFunction {

    private Avg(Object argument) {
        super("avg", argument);
    }

    public static <T, R> Avg of(ColFunction<T, R> column) {
        return new Avg(FunctionArgs.column(column, "AVG"));
    }

    public static Avg of(SqlExpression expression) {
        return new Avg(FunctionArgs.expression(expression, "AVG"));
    }
}
