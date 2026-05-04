package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.translate.SqlResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 未配置执行器时的默认实现。
 *
 * @author ytora
 * @since 1.0
 */
public class MissingSqlExecutor implements SqlExecutor {

    /**
     * 拒绝执行查询并提示调用方先注册执行器。
     *
     * @param sqlResult SQL 翻译结果
     * @return 不会返回结果，始终抛出异常
     */
    @Override
    public List<Map<String, Object>> query(SqlResult sqlResult) {
        throw missing(sqlResult);
    }

    @Override
    public List<Map<String, Object>> queryWithoutInterceptors(SqlResult sqlResult) {
        throw missing(sqlResult);
    }

    /**
     * 拒绝执行实体查询并提示调用方先注册执行器。
     *
     * @param sqlResult SQL 翻译结果
     * @param resultType 实体结果类型
     * @return 不会返回结果，始终抛出异常
     * @param <T> 实体结果类型
     */
    @Override
    public <T> List<T> query(SqlResult sqlResult, Class<T> resultType) {
        throw missing(sqlResult);
    }

    @Override
    public <T> List<T> queryWithoutInterceptors(SqlResult sqlResult, Class<T> resultType) {
        throw missing(sqlResult);
    }

    /**
     * 拒绝执行更新并提示调用方先注册执行器。
     *
     * @param sqlResult SQL 翻译结果
     * @return 不会返回结果，始终抛出异常
     */
    @Override
    public int update(SqlResult sqlResult) {
        throw missing(sqlResult);
    }

    /**
     * 创建缺少执行器时的异常。
     *
     * @param sqlResult SQL 翻译结果，用于帮助定位是哪条语句触发执行
     * @return 非法状态异常
     */
    private IllegalStateException missing(SqlResult sqlResult) {
        return new IllegalStateException("尚未配置SqlExecutor，无法执行SQL。可先调用toSql()查看翻译结果。SQL: " + sqlResult.getSql());
    }
}
