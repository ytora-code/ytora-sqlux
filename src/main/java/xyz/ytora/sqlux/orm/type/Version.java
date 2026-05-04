package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

/**
 * 内置类型：乐观锁版本。
 *
 * <p>实体 UPDATE 时，框架会根据该字段生成 {@code version = version + 1} 和
 * {@code WHERE version = oldVersion}，由单条 SQL 完成并发版本校验。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class Version implements SqlReader, SqlWriter {

    private Long value;

    /**
     * 创建空版本对象。
     */
    public Version() {
    }

    private Version(Long value) {
        this.value = value;
    }

    /**
     * 创建版本对象。
     *
     * @param value 版本值
     * @return 版本对象
     */
    public static Version of(long value) {
        return new Version(value);
    }

    /**
     * 创建初始版本对象。
     *
     * @return 初始版本对象
     */
    public static Version initial() {
        return of(1L);
    }

    /**
     * 获取版本值。
     *
     * @return 版本值
     */
    public Long getValue() {
        return value;
    }

    @Override
    public Version read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Version) {
            return (Version) value;
        }
        if (value instanceof Number) {
            return Version.of(((Number) value).longValue());
        }
        return Version.of(Long.parseLong(String.valueOf(value)));
    }

    @Override
    public Object write() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
