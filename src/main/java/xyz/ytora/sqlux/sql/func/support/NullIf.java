package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;

/**
 * NULLIF 空值转换函数。
 */
@SuppressWarnings("overloads")
public final class NullIf extends AbstractSqlFunction {

    private NullIf(Object first, Object second) {
        super("nullif", first, second);
    }

    public static NullIf of(Object first, Object second) {
        return new NullIf(first, second);
    }

    public static <T, R> NullIf of(ColFunction<T, R> first, Object second) {
        return new NullIf(FunctionArgs.column(first, "NULLIF"), second);
    }

    public static NullIf of(SqlExpression first, Object second) {
        return new NullIf(FunctionArgs.expression(first, "NULLIF"), second);
    }
}
