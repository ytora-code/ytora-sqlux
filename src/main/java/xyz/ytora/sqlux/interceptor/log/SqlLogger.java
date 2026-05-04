package xyz.ytora.sqlux.interceptor.log;

import java.util.logging.Level;

/**
 * SQL日志记录器。
 *
 * <p>实现类只负责决定如何输出日志，例如写入应用日志、审计系统、链路追踪系统，
 * 或按采样规则跳过大结果集。SQL执行前后调用时机由 {@link LogInterceptor} 保证。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface SqlLogger {

    /**
     * SQL提交给数据库前的日志回调。
     *
     * @param event SQL日志事件；入参包含 SQL、参数、SQL 类型等信息
     */
    void beforeExecute(SqlLogEvent event);

    /**
     * SQL执行成功后的日志回调。
     *
     * @param event SQL日志事件；入参包含成功结果和耗时信息
     */
    void afterSuccess(SqlLogEvent event);

    /**
     * SQL执行失败后的日志回调。
     *
     * @param event SQL日志事件；入参包含异常和耗时信息
     */
    void afterFailure(SqlLogEvent event);

    /**
     * 自由记录日志
     * @param level 日志级别
     * @param msg 日志消息
     */
    default void log(Level level, String msg) {

    }
}
