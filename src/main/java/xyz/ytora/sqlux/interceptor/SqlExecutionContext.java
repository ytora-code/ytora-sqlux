package xyz.ytora.sqlux.interceptor;

import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL执行上下文。
 *
 * <p>该对象会在一次 SQL 执行期间传递给所有拦截器。拦截器可以读取操作类型、
 * 当前 SQL、参数、结果、异常和耗时，也可以在执行前替换 SQL 翻译结果。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlExecutionContext {

    private SqlResult sqlResult;

    private final Class<?> resultType;

    private final List<?> generatedKeyTargets;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private final long startNanos;

    private long endNanos;

    private Object result;

    private Throwable exception;

    /**
     * 创建 SQL 执行上下文。
     *
     * <p>示例：JDBC 查询执行前会创建 {@code new SqlExecutionContext(SqlOperation.QUERY, sqlResult, User.class, null)}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含初始 SQL 文本和参数
     * @param resultType 查询实体结果类型；入参仅在 {@code submit(User.class)} 场景有值，普通查询为 {@code null}
     * @param generatedKeyTargets 需要回填主键的实体对象；入参仅在 ORM INSERT 场景可能有值
     */
    public SqlExecutionContext(SqlResult sqlResult, Class<?> resultType, List<?> generatedKeyTargets) {
        if (sqlResult == null) {
            throw new IllegalArgumentException("SQL翻译结果不能为空");
        }
        this.sqlResult = sqlResult;
        this.resultType = resultType;
        this.generatedKeyTargets = generatedKeyTargets == null ? Collections.emptyList() : generatedKeyTargets;
        this.startNanos = System.nanoTime();
    }

    /**
     * 获取当前 SQL 语句类型。
     *
     * <p>示例：执行 SELECT 时返回 {@link SqlType#SELECT}，
     * 执行 UPDATE 时返回 {@link SqlType#UPDATE}。如果 {@link SqlResult} 是外部手工创建且未指定类型，
     * 则返回 {@link SqlType#UNKNOWN}。</p>
     *
     * @return SQL语句类型；出参不会为 {@code null}
     */
    public SqlType getSqlType() {
        return sqlResult.getSqlType();
    }

    /**
     * 获取源查询模型。
     *
     * <p>示例：SELECT 翻译结果通常返回 {@code SelectQuery}，可用于日志插件输出结构化调试信息。</p>
     *
     * @return 源查询模型；外部手工创建 {@link SqlResult} 时可能为 {@code null}
     */
    public Object getStatement() {
        return sqlResult.getStatement();
    }

    /**
     * 获取当前 SQL 翻译结果。
     *
     * <p>示例：前置拦截器可以读取 {@code context.getSqlResult().getSql()} 做审计或校验。</p>
     *
     * @return 当前 SQL 翻译结果；可能已被前面的拦截器替换
     */
    public SqlResult getSqlResult() {
        return sqlResult;
    }

    /**
     * 替换当前 SQL 翻译结果。
     *
     * <p>示例：拦截器可以把 SQL 从 {@code SELECT * FROM user} 替换为
     * {@code SELECT * FROM user WHERE tenant_id = ?}，并同时追加参数。</p>
     *
     * @param sqlResult 新的 SQL 翻译结果；入参不能为 {@code null}
     */
    public void setSqlResult(SqlResult sqlResult) {
        if (sqlResult == null) {
            throw new IllegalArgumentException("SQL翻译结果不能为空");
        }
        this.sqlResult = sqlResult;
    }

    /**
     * 获取查询实体结果类型。
     *
     * <p>示例：{@code submit(User.class)} 返回 {@code User.class}；
     * {@code submit()} 原始查询返回 {@code null}。</p>
     *
     * @return 查询实体结果类型；非 ORM 查询或更新操作时为 {@code null}
     */
    public Class<?> getResultType() {
        return resultType;
    }

    /**
     * 获取需要回填 generated keys 的实体对象。
     *
     * <p>示例：{@code values(user1).values(user2)} 提交时，该列表包含 {@code user1} 和 {@code user2}。</p>
     *
     * @return 待回填实体对象列表；出参不可变且不会为 {@code null}
     */
    public List<?> getGeneratedKeyTargets() {
        return generatedKeyTargets;
    }

    /**
     * 获取一次执行内的扩展属性。
     *
     * <p>示例：一个拦截器可以放入 {@code traceId}，后续拦截器通过同一个 key 读取。</p>
     *
     * @return 扩展属性 Map；出参可修改，仅在本次 SQL 执行上下文内有效
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 获取执行结果。
     *
     * <p>示例：查询成功后是原始 {@code List<Map<String, Object>>}；
     * 更新成功后是 {@code Integer} 影响行数。</p>
     *
     * @return SQL执行结果；失败或尚未执行完成时可能为 {@code null}
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置执行结果。
     *
     * <p>示例：JDBC 查询成功后会把读取到的原始结果集设置到上下文中。</p>
     *
     * @param result SQL执行结果；入参可以是查询结果集或影响行数
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取执行异常。
     *
     * <p>示例：SQL执行失败时，失败回调可以读取该异常并记录日志。</p>
     *
     * @return 执行异常；成功或尚未失败时为 {@code null}
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 设置执行异常。
     *
     * <p>示例：JDBC 抛出 {@code SQLException} 后，执行器会把包装后的运行时异常写入上下文。</p>
     *
     * @param exception 执行异常；入参可以是 SQL 执行异常或拦截器异常
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * 获取执行开始纳秒时间。
     *
     * <p>示例：可以和 {@link System#nanoTime()} 对比计算耗时。</p>
     *
     * @return 创建上下文时的纳秒时间
     */
    public long getStartNanos() {
        return startNanos;
    }

    /**
     * 获取执行结束纳秒时间。
     *
     * <p>示例：成功或失败回调执行前，执行器会调用 {@link #markEnd()} 写入结束时间。</p>
     *
     * @return 执行结束纳秒时间；尚未结束时为 {@code 0}
     */
    public long getEndNanos() {
        return endNanos;
    }

    /**
     * 标记执行结束时间。
     *
     * <p>示例：数据库返回结果或抛出异常后，执行器调用该方法固定本次执行耗时。</p>
     */
    public void markEnd() {
        this.endNanos = System.nanoTime();
    }

    /**
     * 获取执行耗时毫秒数。
     *
     * <p>示例：日志拦截器可输出 {@code context.getElapsedMillis()} 作为 SQL 执行耗时。</p>
     *
     * @return 耗时毫秒；如果尚未标记结束，则按当前时间计算
     */
    public long getElapsedMillis() {
        long end = endNanos == 0L ? System.nanoTime() : endNanos;
        return (end - startNanos) / 1000000L;
    }
}
