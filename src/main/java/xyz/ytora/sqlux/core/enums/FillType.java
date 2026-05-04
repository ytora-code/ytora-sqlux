package xyz.ytora.sqlux.core.enums;

/**
 * 字段自动填充时机。
 *
 * <p>该枚举用于描述字段在 INSERT、UPDATE 或两者场景下是否启用自动填充。</p>
 *
 * @author ytora
 * @since 1.0
 */
public enum FillType {

    /**
     * 不自动填充。
     */
    NONE,

    /**
     * INSERT 时自动填充。
     */
    INSERT,

    /**
     * UPDATE 时自动填充。
     */
    UPDATE,

    /**
     * INSERT 和 UPDATE 时都自动填充。
     */
    INSERT_UPDATE
}
