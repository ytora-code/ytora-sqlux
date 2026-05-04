package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * SUM 聚合函数。
 */
@SuppressWarnings("overloads")
public final class Sum extends AbstractSqlFunction {

    private Sum(Object argument) {
        super("sum", argument);
    }

    public static <T, R> Sum of(ColFunction<T, R> column) {
        return new Sum(FunctionArgs.column(column, "SUM"));
    }

    public static Sum of(SqlExpression expression) {
        return new Sum(FunctionArgs.expression(expression, "SUM"));
    }
}
