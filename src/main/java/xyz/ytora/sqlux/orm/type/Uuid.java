package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.util.UUID;

/**
 * 内置类型：UUID。
 *
 * <p>用于实体字段需要明确映射为 UUID 字段的场景。写入数据库时使用标准 36 位 UUID 字符串，
 * 读取时支持数据库返回 {@link UUID} 或字符串。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class Uuid implements SqlReader, SqlWriter {

    private UUID value;

    /**
     * 创建空 UUID。
     */
    public Uuid() {
    }

    /**
     * 根据 UUID 创建对象。
     *
     * @param value UUID值；可以为 {@code null}
     */
    public Uuid(UUID value) {
        this.value = value;
    }

    /**
     * 根据 UUID 字符串创建对象。
     *
     * @param value UUID字符串；可以为 {@code null}
     */
    public Uuid(String value) {
        this.value = value == null ? null : UUID.fromString(value);
    }

    /**
     * 创建 UUID 对象。
     *
     * @param value UUID值；可以为 {@code null}
     * @return UUID对象
     */
    public static Uuid of(UUID value) {
        return new Uuid(value);
    }

    /**
     * 创建 UUID 对象。
     *
     * @param value UUID字符串；可以为 {@code null}
     * @return UUID对象
     */
    public static Uuid of(String value) {
        return new Uuid(value);
    }

    /**
     * 获取 UUID 值。
     *
     * @return UUID值
     */
    public UUID getValue() {
        return value;
    }

    /**
     * 设置 UUID 值。
     *
     * @param value UUID值；可以为 {@code null}
     */
    public void setValue(UUID value) {
        this.value = value;
    }

    @Override
    public Uuid read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID) {
            return new Uuid((UUID) value);
        }
        return new Uuid(String.valueOf(value));
    }

    @Override
    public Object write() {
        return value == null ? null : value.toString();
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
