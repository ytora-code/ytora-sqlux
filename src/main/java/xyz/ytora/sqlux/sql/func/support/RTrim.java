package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * RTRIM 去除右侧空白函数。
 */
@SuppressWarnings("overloads")
public final class RTrim extends AbstractSqlFunction {

    private RTrim(Object argument) {
        super("rtrim", argument);
    }

    public static <T, R> RTrim of(ColFunction<T, R> column) {
        return new RTrim(FunctionArgs.column(column, "RTRIM"));
    }

    public static RTrim of(SqlExpression expression) {
        return new RTrim(FunctionArgs.expression(expression, "RTRIM"));
    }
}
