package xyz.ytora.sqlux.core;

import xyz.ytora.sqlux.core.enums.DbType;

/**
 * 数据库类型提供组件。
 *
 * <p>连接提供器如果同时实现该接口，Sqlux 可以在不访问 JDBC metadata 的情况下获取当前数据库类型，
 * 适合动态数据源、多租户切库等场景。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface IDbTypeProvider {

    /**
     * 获取当前上下文使用的数据库类型。
     *
     * @return 当前数据库类型；返回 {@code null} 时表示不提供，由后续策略兜底
     */
    DbType getDbType();
}
