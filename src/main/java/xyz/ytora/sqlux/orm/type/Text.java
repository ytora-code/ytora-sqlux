package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

/**
 * 内置类型：大文本。
 *
 * <p>用于实体字段需要明确映射为数据库 {@code text} 一类大文本字段的场景。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class Text implements SqlReader, SqlWriter {

    private String value;

    /**
     * 创建空文本。
     */
    public Text() {
    }

    /**
     * 根据字符串创建文本。
     *
     * @param value 文本内容；可以为 {@code null}
     */
    public Text(String value) {
        this.value = value;
    }

    /**
     * 创建文本对象。
     *
     * @param value 文本内容；可以为 {@code null}
     * @return 文本对象
     */
    public static Text of(String value) {
        return new Text(value);
    }

    /**
     * 获取文本内容。
     *
     * @return 文本内容
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置文本内容。
     *
     * @param value 文本内容；可以为 {@code null}
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Text read(Object value) {
        return value == null ? null : new Text(String.valueOf(value));
    }

    @Override
    public Object write() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
