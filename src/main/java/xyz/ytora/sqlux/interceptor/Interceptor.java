package xyz.ytora.sqlux.interceptor;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.translate.SqlResult;

/**
 * SQL执行拦截器。
 *
 * <p>拦截器运行在 SQL 真正提交给数据库的前后，适合实现日志、审计、限流、
 * SQL改写、参数改写、执行守卫等扩展。使用者可以在 {@link #beforeExecute(SqlExecutionContext)}
 * 中修改 {@link SqlExecutionContext#getSqlResult()}，也可以直接抛出异常阻止 SQL 提交。</p>
 *
 * <p>示例：注册一个禁止无 WHERE 删除的拦截器。</p>
 *
 * <pre><code>
 * Interceptors.add(new Interceptor() {
 *     &#64;Override
 *     public void beforeExecute(SqlExecutionContext context) {
 *         String sql = context.getSqlResult().getSql().toLowerCase();
 *         if (sql.startsWith("delete") && !sql.contains(" where ")) {
 *             throw new IllegalStateException("禁止无WHERE删除");
 *         }
 *     }
 * });
 * </code></pre>
 *
 * @author ytora 
 * @since 1.0
 */
public interface Interceptor {

    /**
     * SQL翻译前的结构化改写回调。
     *
     * <p>该方法在查询模型翻译为 SQL 字符串之前执行。实现者可以通过
     * {@link SqlRewriteContext#andWhere(java.util.function.Consumer)} 追加条件，
     * 或通过 {@link SqlRewriteContext#select(ColFunction[])}
     * 限制 SELECT 字段，避免直接拼接 SQL 字符串。</p>
     *
     * <p>如果beforeTranslate执行抛出异常，后面拦截器的beforeTranslate会被打断</p>
     *
     * @param context SQL结构化改写上下文；入参包含 SQL 类型和源查询模型
     */
    default void beforeTranslate(SqlRewriteContext context) {
    }

    /**
     * SQL执行前回调。
     *
     * <p>该方法在 JDBC 创建 {@code PreparedStatement} 之前执行。实现者可以通过
     * {@link SqlExecutionContext#setSqlResult(SqlResult)}
     * 替换 SQL 和参数，也可以抛出异常阻止执行。</p>
     *
     * <p>如果beforeExecute执行抛出异常，后面拦截器的beforeExecute会被打断</p>
     *
     * @param context SQL执行上下文；入参包含当前 SQL、参数、操作类型和扩展属性
     */
    default void beforeExecute(SqlExecutionContext context) {
    }

    /**
     * SQL执行成功后回调。
     *
     * <p>该方法只在数据库执行成功后触发。查询场景中 {@link SqlExecutionContext#getResult()}
     * 是原始 {@code List<Map<String, Object>>}；更新场景中结果是受影响行数 {@code Integer}。</p>
     *
     * <p>
     *     如果afterSuccess执行抛出异常，后面拦截器的afterSuccess会被打断.
     *     转而执行{@link #afterFailure}和{@link #afterFinally}
     * </p>
     *
     * @param context SQL执行上下文；入参包含成功结果和耗时信息
     */
    default void afterSuccess(SqlExecutionContext context) {
    }

    /**
     * SQL执行失败后回调。
     *
     * <p>该方法在 SQL 执行或前置拦截阶段抛出异常时触发。实现者可以读取
     * {@link SqlExecutionContext#getException()} 做日志或告警，原异常会继续向调用方抛出。</p>
     *
     * <p>afterFailure执行状态，不会影响其他拦截器的afterFailure执行</p>
     *
     * @param context SQL执行上下文；入参包含失败异常
     */
    default void afterFailure(SqlExecutionContext context) {
    }

    /**
     * SQL执行结束回调。
     *
     * <p>无论成功还是失败都会触发，适合清理 ThreadLocal、释放拦截器内部资源等。
     * 该方法会在 {@link #afterSuccess(SqlExecutionContext)} 或
     * {@link #afterFailure(SqlExecutionContext)} 之后执行。</p>
     *
     * <p>afterFinally执行状态，不会影响其他拦截器的afterFinally执行</p>
     *
     * @param context SQL执行上下文；入参可用于判断本次执行是否成功
     */
    default void afterFinally(SqlExecutionContext context) {
    }
}
