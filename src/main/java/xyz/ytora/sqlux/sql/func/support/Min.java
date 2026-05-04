package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * MIN 聚合函数。
 */
@SuppressWarnings("overloads")
public final class Min extends AbstractSqlFunction {

    private Min(Object argument) {
        super("min", argument);
    }

    public static <T, R> Min of(ColFunction<T, R> column) {
        return new Min(FunctionArgs.column(column, "MIN"));
    }

    public static Min of(SqlExpression expression) {
        return new Min(FunctionArgs.expression(expression, "MIN"));
    }
}
