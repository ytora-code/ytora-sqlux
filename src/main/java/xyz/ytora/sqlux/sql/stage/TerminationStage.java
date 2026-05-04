package xyz.ytora.sqlux.sql.stage;

/**
 * SQL调用链的终止阶段。
 *
 * <p>不同语句的终止阶段返回值不同：SELECT 返回查询结果，INSERT/UPDATE/DELETE 返回影响行数。</p>
 *
 * @author ytora
 * @since 1.0
 * @param <R> 提交后的返回结果类型
 */
@FunctionalInterface
public interface TerminationStage<R> {

    /**
     * 提交当前 SQL 调用链。
     *
     * <p>实现通常会先翻译 SQL，再通过执行器提交数据库。</p>
     *
     * @return 提交结果
     */
    R submit();
}