package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 内置类型：二进制数据。
 *
 * <p>用于实体字段需要明确映射为 {@code blob}/{@code bytea} 一类二进制字段的场景。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class Binary implements SqlReader, SqlWriter {

    private byte[] value;

    /**
     * 创建空二进制对象。
     */
    public Binary() {
    }

    /**
     * 根据字节数组创建二进制对象。
     *
     * @param value 字节数组；可以为 {@code null}
     */
    public Binary(byte[] value) {
        this.value = copy(value);
    }

    /**
     * 创建二进制对象。
     *
     * @param value 字节数组；可以为 {@code null}
     * @return 二进制对象
     */
    public static Binary of(byte[] value) {
        return new Binary(value);
    }

    /**
     * 获取字节数组副本。
     *
     * @return 字节数组副本；值为空时返回 {@code null}
     */
    public byte[] getValue() {
        return copy(value);
    }

    /**
     * 设置字节数组。
     *
     * @param value 字节数组；可以为 {@code null}
     */
    public void setValue(byte[] value) {
        this.value = copy(value);
    }

    @Override
    public Binary read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return new Binary((byte[]) value);
        }
        if (value instanceof Byte[]) {
            Byte[] source = (Byte[]) value;
            byte[] bytes = new byte[source.length];
            for (int i = 0; i < source.length; i++) {
                bytes[i] = source[i] == null ? 0 : source[i];
            }
            return new Binary(bytes);
        }
        if (value instanceof Blob) {
            return new Binary(readBlob((Blob) value));
        }
        throw new IllegalArgumentException("无法将[" + value.getClass().getName() + "]类型转为Binary");
    }

    @Override
    public Object write() {
        return copy(value);
    }

    @Override
    public String toString() {
        return value == null ? null : Arrays.toString(value);
    }

    private static byte[] copy(byte[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    private static byte[] readBlob(Blob blob) {
        try {
            long length = blob.length();
            if (length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Blob长度超过Java数组上限: " + length);
            }
            return blob.getBytes(1, (int) length);
        } catch (SQLException e) {
            throw new IllegalArgumentException("读取Blob失败", e);
        }
    }
}
