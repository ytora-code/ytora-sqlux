package xyz.ytora.sqlux.orm.id;

import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.toolkit.id.Ids;

/**
 * ORM 主键生成器。
 *
 * <p>该类只负责根据 {@link IdType} 生成原始 ID 值，不处理实体字段回填和类型转换。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class OrmIdGenerator {

    /**
     * 工具类不允许实例化。
     */
    private OrmIdGenerator() {
    }

    /**
     * 根据主键策略生成 ID。
     *
     * @param idType 主键策略
     * @return 生成的 ID；无策略时返回 {@code null}
     */
    public static Object nextId(IdType idType) {
        if (idType == null || idType == IdType.NONE) {
            return null;
        }
        if (idType == IdType.SNOWFLAKE) {
            return Ids.nextSnowflakeId();
        }
        if (idType == IdType.UUID) {
            return Ids.nextUuid();
        }
        if ("ULID".equals(idType.name()) || "UCID".equals(idType.name())) {
            return Ids.nextUlid();
        }
        throw new IllegalArgumentException("不支持的主键策略: " + idType);
    }
}
