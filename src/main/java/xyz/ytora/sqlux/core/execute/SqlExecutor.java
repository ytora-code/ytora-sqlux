package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.SqluxGlobal;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL执行器。
 *
 * <p>翻译器只负责生成 SQL 和参数；执行器负责提交数据库并映射结果。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface SqlExecutor {

    /**
     * 执行查询并返回原始结果集。
     *
     * <p>示例：{@code SQL.select(User::getName).from(User.class).submit()} 最终会调用该方法，
     * 返回值形如 {@code [{name=ytora}]}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @return 以字段名和值表示的查询结果集；出参通常是 {@code List<Map<String, Object>>}
     */
    List<Map<String, Object>> query(SqlResult sqlResult);

    /**
     * 执行查询并返回原始结果集，但不触发 SQL 拦截器。
     *
     * <p>适用于审计、删除前备份等非常规业务场景。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @return 原始查询结果集
     */
    List<Map<String, Object>> queryWithoutInterceptors(SqlResult sqlResult);

    /**
     * 执行查询并映射为指定实体类型。
     *
     * <p>示例：{@code SQL.select(User::getName).from(User.class).submit(User.class)}
     * 最终会调用该方法，并返回 {@code List<User>}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @param resultType 结果实体类型；入参例如 {@code User.class}
     * @return 映射后的实体集合；出参元素类型由 {@code resultType} 决定
     * @param <T> 结果实体类型泛型，例如 {@code User}
     */
    <T> List<T> query(SqlResult sqlResult, Class<T> resultType);

    /**
     * 执行查询并映射为指定实体类型，但不触发 SQL 拦截器。
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @param resultType 结果实体类型；入参例如 {@code User.class}
     * @param <T> 结果实体类型泛型，例如 {@code User}
     * @return 映射后的实体集合
     */
    <T> List<T> queryWithoutInterceptors(SqlResult sqlResult, Class<T> resultType);

    /**
     * 执行 INSERT、UPDATE 或 DELETE。
     *
     * <p>示例：{@code SQL.update(User.class).set(User::getName, "ytora").submit()}
     * 最终会调用该方法，返回数据库报告的受影响行数。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @return 受影响行数；出参通常来自 JDBC {@code executeUpdate}
     */
    int update(SqlResult sqlResult);

    /**
     * 执行 INSERT、UPDATE 或 DELETE，并在需要时回填数据库生成的主键。
     *
     * <p>默认实现保持向后兼容，只执行普通更新。支持 JDBC generated keys 的执行器可以覆盖该方法。</p>
     *
     * <p>示例：{@code SQL.insert(User.class).into(User::getName).values(user).submit()}
     * 可通过该方法把 JDBC generated keys 回填到 {@code user}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @param generatedKeyTargets 需要回填主键的实体对象；为空时等价于 {@link #update(SqlResult)}
     * @return 受影响行数；默认实现直接调用 {@link #update(SqlResult)}
     */
    default int update(SqlResult sqlResult, List<?> generatedKeyTargets) {
        return update(sqlResult);
    }

    /**
     * 批量执行 INSERT、UPDATE 或 DELETE。
     *
     * <p>默认实现逐条执行，保证旧执行器无需改造即可工作。JDBC 执行器会覆盖该方法，
     * 使用 {@code PreparedStatement.addBatch()} 提交批处理。</p>
     *
     * @param sqlResult SQL翻译结果；入参 SQL 通常只包含一组占位符
     * @param batchParams 批量参数；每个元素是一条 SQL 的参数列表
     * @return 每条 SQL 的影响行数
     */
    default int[] updateBatch(SqlResult sqlResult, List<List<Object>> batchParams) {
        return updateBatch(sqlResult, batchParams, null);
    }

    /**
     * 批量执行 INSERT、UPDATE 或 DELETE，并在需要时回填数据库生成的主键。
     *
     * @param sqlResult SQL翻译结果；入参 SQL 通常只包含一组占位符
     * @param batchParams 批量参数；每个元素是一条 SQL 的参数列表
     * @param generatedKeyTargets 需要回填主键的实体对象；为空时不请求 generated keys
     * @return 每条 SQL 的影响行数
     */
    default int[] updateBatch(SqlResult sqlResult, List<List<Object>> batchParams, List<?> generatedKeyTargets) {
        if (batchParams == null || batchParams.isEmpty()) {
            return new int[0];
        }
        int[] results = new int[batchParams.size()];
        for (int i = 0; i < batchParams.size(); i++) {
            List<Object> params = batchParams.get(i) == null
                    ? new ArrayList<>()
                    : batchParams.get(i);
            results[i] = update(new SqlResult(sqlResult.getSql(), params,
                    sqlResult.getSqlType(), sqlResult.getStatement()));
        }
        return results;
    }

    /**
     * 获取当前执行器默认使用的数据库类型。
     *
     * <p>默认实现返回 {@link SqluxGlobal#getDefaultDbType()}。JDBC 执行器可以覆盖该方法，
     * 使用构造参数、连接提供器或 JDBC metadata 自动探测数据库类型。</p>
     *
     * @return 数据库类型；出参不会为 {@code null}
     */
    default DbType getDbType() {
        return SQL.getSqluxGlobal().getDefaultDbType();
    }
}
