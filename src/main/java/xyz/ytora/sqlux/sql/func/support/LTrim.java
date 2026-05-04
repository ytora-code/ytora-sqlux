package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * LTRIM 去除左侧空白函数。
 */
@SuppressWarnings("overloads")
public final class LTrim extends AbstractSqlFunction {

    private LTrim(Object argument) {
        super("ltrim", argument);
    }

    public static <T, R> LTrim of(ColFunction<T, R> column) {
        return new LTrim(FunctionArgs.column(column, "LTRIM"));
    }

    public static LTrim of(SqlExpression expression) {
        return new LTrim(FunctionArgs.expression(expression, "LTRIM"));
    }
}
