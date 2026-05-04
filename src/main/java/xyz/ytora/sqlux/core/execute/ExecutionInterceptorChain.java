package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlExecutionContext;

import java.util.List;

/**
 * SQL 执行拦截器链。
 *
 * <p>负责按约定顺序触发拦截器回调，让执行器本身只关注 SQL 的实际提交。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class ExecutionInterceptorChain {

    private final List<Interceptor> interceptors;

    private ExecutionInterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    static ExecutionInterceptorChain snapshot() {
        return new ExecutionInterceptorChain(SQL.getSqluxGlobal().snapshotInterceptors());
    }

    void before(SqlExecutionContext context) {
        for (Interceptor interceptor : interceptors) {
            interceptor.beforeExecute(context);
        }
    }

    void success(SqlExecutionContext context) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).afterSuccess(context);
        }
    }

    void failure(SqlExecutionContext context, Throwable failure) {
        context.setException(failure);
        if (context.getEndNanos() == 0L) {
            context.markEnd();
        }
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            try {
                interceptors.get(i).afterFailure(context);
            } catch (RuntimeException | Error e) {
                failure.addSuppressed(e);
            }
        }
    }

    void finish(SqlExecutionContext context) {
        Throwable failure = context.getException();
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            try {
                interceptors.get(i).afterFinally(context);
            } catch (RuntimeException | Error e) {
                if (failure == null) {
                    throw e;
                }
                failure.addSuppressed(e);
            }
        }
    }
}
