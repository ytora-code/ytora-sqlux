package xyz.ytora.sqlux.core.enums;

/**
 * 数据库表的主键策略
 *
 * <p>该枚举用于决定插入数据时，主键的填充规则。</p>
 *
 * @author ytora
 * @since 1.0
 */
public enum IdType {

    /**
     * 无规则
     */
    NONE,

    /**
     * 雪花算法
     */
    SNOWFLAKE,
    /**
     * UUID，通常是36位字符串形式，分布式环境使用较多
     */
    UUID(),

    /**
     * UCID
     *
     * @deprecated 请使用 {@link #ULID}
     */
    @Deprecated
    UCID(),

    /**
     * ULID，按时间排序的字符串ID
     */
    ULID

}
