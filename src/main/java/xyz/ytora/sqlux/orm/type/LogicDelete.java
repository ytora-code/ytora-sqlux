package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

/**
 * 内置类型：逻辑删除标记。
 *
 * <p>实体包含该字段时，普通单表 DELETE 会被翻译为 UPDATE，将该字段设置为 {@code 1}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class LogicDelete implements SqlReader, SqlWriter {

    private final Integer value;

    /**
     * 创建未删除标记。
     */
    public LogicDelete() {
        this(0);
    }

    private LogicDelete(Integer value) {
        this.value = value;
    }

    /**
     * 创建未删除标记。
     *
     * @return 未删除标记
     */
    public static LogicDelete normal() {
        return new LogicDelete(0);
    }

    /**
     * 创建已删除标记。
     *
     * @return 已删除标记
     */
    public static LogicDelete deleted() {
        return new LogicDelete(1);
    }

    /**
     * 获取数据库标记值。
     *
     * @return {@code 0} 表示未删除，{@code 1} 表示已删除
     */
    public Integer getValue() {
        return value;
    }

    /**
     * 是否已删除。
     *
     * @return 已删除时返回 {@code true}
     */
    public boolean isDeleted() {
        return Integer.valueOf(1).equals(value);
    }

    @Override
    public LogicDelete read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LogicDelete) {
            return (LogicDelete) value;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? deleted() : normal();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1 ? deleted() : normal();
        }
        String text = String.valueOf(value);
        return "true".equalsIgnoreCase(text) || "1".equals(text) ? deleted() : normal();
    }

    @Override
    public Object write() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
