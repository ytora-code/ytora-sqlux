package xyz.ytora.sqlux.core.enums;

/**
 * SQL语句类型。
 *
 * <p>该枚举用于标识翻译结果来自 SELECT、INSERT、UPDATE 还是 DELETE，
 * 便于执行器、拦截器和日志插件做差异化处理。</p>
 *
 * @author ytora
 * @since 1.0
 */
public enum SqlType {

    /**
     * SELECT 查询语句。
     */
    SELECT,

    /**
     * INSERT 插入语句。
     */
    INSERT,

    /**
     * UPDATE 更新语句。
     */
    UPDATE,

    /**
     * DELETE 删除语句。
     */
    DELETE,

    /**
     * 未知 SQL 类型。
     */
    UNKNOWN
}
