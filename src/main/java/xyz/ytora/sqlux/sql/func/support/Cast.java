package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.FunctionArgs;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * CAST 类型转换表达式。
 */
@SuppressWarnings("overloads")
public final class Cast extends AbstractSqlFunction {

    private final Object expression;

    private final String targetType;

    private Cast(Object expression, String targetType) {
        super("cast", expression);
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new IllegalArgumentException("CAST 目标类型不能为空");
        }
        this.expression = expression;
        this.targetType = targetType.trim();
    }

    public static <T, R> Cast of(ColFunction<T, R> column, String targetType) {
        return new Cast(FunctionArgs.column(column, "CAST"), targetType);
    }

    public static Cast of(SqlExpression expression, String targetType) {
        return new Cast(FunctionArgs.expression(expression, "CAST"), targetType);
    }

    public static Cast of(Object value, String targetType) {
        return new Cast(value, targetType);
    }

    @Override
    public String render(TranslateContext context) {
        return "cast(" + renderArgument(expression, context) + " AS " + targetType + ")";
    }
}
