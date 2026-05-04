package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * REPLACE 字符串替换函数。
 */
@SuppressWarnings("overloads")
public final class Replace extends AbstractSqlFunction {

    private Replace(Object source, Object search, Object replacement) {
        super("replace", source, search, replacement);
    }

    public static Replace of(Object source, Object search, Object replacement) {
        return new Replace(source, search, replacement);
    }

    public static <T, R> Replace of(ColFunction<T, R> source, Object search, Object replacement) {
        return new Replace(FunctionArgs.column(source, "REPLACE"), search, replacement);
    }

    public static Replace of(SqlExpression source, Object search, Object replacement) {
        return new Replace(FunctionArgs.expression(source, "REPLACE"), search, replacement);
    }
}
