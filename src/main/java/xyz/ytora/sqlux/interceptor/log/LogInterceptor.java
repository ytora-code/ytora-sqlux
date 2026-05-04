package xyz.ytora.sqlux.interceptor.log;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlExecutionContext;

/**
 * SQL日志拦截器。
 *
 * <p>该类只负责在 SQL 真正执行前后采集稳定的日志事件，并转交给
 * {@link SqlLogger} 输出。用户需要定制日志格式、脱敏、采样或写入外部系统时，
 * 只需要替换 {@link SqlLogger}，不需要重新实现执行前后调用时机。</p>
 *
 * <p>日志记录器异常不会影响 SQL 本身的执行结果，避免日志系统故障扩大为业务故障。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class LogInterceptor implements Interceptor {

    private SqlLogger logger;

    /**
     * 获取当前使用的 SQL 日志记录器。
     *
     * @return SQL日志记录器；出参不会为 {@code null}
     */
    public SqlLogger getLogger() {
        if (logger == null) {
            logger = SQL.getSqluxGlobal().getSqlLogger();
        }
        return logger;
    }

    @Override
    public void beforeExecute(final SqlExecutionContext context) {
        if (getLogger() == null) {
            return;
        }
        safeLog(() -> getLogger().beforeExecute(SqlLogEvent.from(context)));
    }

    @Override
    public void afterSuccess(final SqlExecutionContext context) {
        if (getLogger() == null) {
            return;
        }
        safeLog(() -> getLogger().afterSuccess(SqlLogEvent.from(context)));
    }

    @Override
    public void afterFailure(final SqlExecutionContext context) {
        if (getLogger() == null) {
            return;
        }
        safeLog(() -> getLogger().afterFailure(SqlLogEvent.from(context)));
    }

    private void safeLog(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            System.err.println("记录日志失败:" + e.getMessage());
        }
    }

}
