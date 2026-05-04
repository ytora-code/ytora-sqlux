package xyz.ytora.sqlux.sql.model;

/**
 * UPDATE SET 赋值项。
 *
 * <p>左侧始终是结构化字段引用；右侧可以是参数值、字段引用或原始 SQL 片段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class Assignment {

    private final ColumnRef column;

    private final Object value;

    private final boolean raw;

    /**
     * 创建 SET 赋值项。
     *
     * @param column 被赋值字段
     * @param value 赋值内容
     * @param raw 是否将赋值内容按原始 SQL 片段处理
     */
    public Assignment(ColumnRef column, Object value, boolean raw) {
        if (column == null) {
            throw new IllegalArgumentException("赋值字段不能为空");
        }
        this.column = column;
        this.value = value;
        this.raw = raw;
    }

    /**
     * 获取被赋值字段。
     *
     * @return 字段引用
     */
    public ColumnRef getColumn() {
        return column;
    }

    /**
     * 获取赋值内容。
     *
     * @return 赋值内容
     */
    public Object getValue() {
        return value;
    }

    /**
     * 判断赋值内容是否为原始 SQL 片段。
     *
     * @return {@code true} 表示原样拼接右侧表达式
     */
    public boolean isRaw() {
        return raw;
    }
}