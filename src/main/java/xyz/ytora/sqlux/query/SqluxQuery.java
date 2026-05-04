package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.sql.stage.select.AbsSelect;

import java.util.Map;

/**
 * 根据查询参数生成 SELECT 的统一入口。
 *
 * @author ytora
 * @since 1.0
 */
public final class SqluxQuery {

    private SqluxQuery() {
    }

    /**
     * 根据查询参数生成 SELECT 阶段对象。
     *
     * @param entityType 查询实体类型
     * @param params 前端查询参数
     * @return SELECT 阶段对象
     * @param <T> 实体类型
     */
    public static <T> AbsSelect select(Class<T> entityType, Map<String, ?> params) {
        return select(entityType).params(params).build();
    }

    /**
     * 创建查询参数构建器。
     *
     * @param entityType 查询实体类型
     * @return 查询参数构建器
     * @param <T> 实体类型
     */
    public static <T> SqluxQueryBuilder<T> select(Class<T> entityType) {
        return new SqluxQueryBuilder<>(entityType);
    }
}
