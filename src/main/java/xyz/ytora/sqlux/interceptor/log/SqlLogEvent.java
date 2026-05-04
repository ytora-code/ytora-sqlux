package xyz.ytora.sqlux.interceptor.log;

import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.SqlExecutionContext;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL日志事件。
 *
 * <p>该对象是 {@link SqlExecutionContext} 在某个日志回调时刻的只读快照，
 * 避免后续拦截器继续修改上下文导致日志记录器读到不一致的数据。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class SqlLogEvent {

    private final SqlType sqlType;
    private final Object statement;
    private final String sql;
    private final List<Object> params;
    private final Class<?> resultType;
    private final List<?> generatedKeyTargets;
    private final Map<String, Object> attributes;
    private final long startNanos;
    private final long endNanos;
    private final long elapsedMillis;
    private final Object result;
    private final Throwable exception;

    private SqlLogEvent(SqlExecutionContext context) {
        SqlResult sqlResult = context.getSqlResult();
        this.sqlType = context.getSqlType();
        this.statement = context.getStatement();
        this.sql = sqlResult.getSql();
        this.params = Collections.unmodifiableList(new ArrayList<>(sqlResult.getParams()));
        this.resultType = context.getResultType();
        this.generatedKeyTargets = Collections.unmodifiableList(new ArrayList<Object>(context.getGeneratedKeyTargets()));
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(context.getAttributes()));
        this.startNanos = context.getStartNanos();
        this.endNanos = context.getEndNanos();
        this.elapsedMillis = context.getElapsedMillis();
        this.result = context.getResult();
        this.exception = context.getException();
    }

    public static SqlLogEvent from(SqlExecutionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("SqlExecutionContext不能为空");
        }
        return new SqlLogEvent(context);
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public Object getStatement() {
        return statement;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public List<?> getGeneratedKeyTargets() {
        return generatedKeyTargets;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public long getStartNanos() {
        return startNanos;
    }

    public long getEndNanos() {
        return endNanos;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public Object getResult() {
        return result;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isSuccess() {
        return exception == null && endNanos > 0L;
    }
}
