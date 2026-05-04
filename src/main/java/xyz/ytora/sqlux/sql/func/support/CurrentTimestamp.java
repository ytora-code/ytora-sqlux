package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * CURRENT_TIMESTAMP 当前时间戳表达式。
 */
public final class CurrentTimestamp implements SqlExpression {

    private static final CurrentTimestamp INSTANCE = new CurrentTimestamp();

    private CurrentTimestamp() {
    }

    public static CurrentTimestamp of() {
        return INSTANCE;
    }

    @Override
    public String render(TranslateContext context) {
        return "current_timestamp";
    }
}
