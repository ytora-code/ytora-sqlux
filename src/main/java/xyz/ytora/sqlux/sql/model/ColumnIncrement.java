package xyz.ytora.sqlux.sql.model;

/**
 * UPDATE SET 中的字段自增表达式。
 *
 * <p>该类型只由框架内部生成，用于安全表达 {@code column = column + step}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class ColumnIncrement {

    private final Number step;

    /**
     * 创建字段自增表达式。
     *
     * @param step 自增步长
     */
    public ColumnIncrement(Number step) {
        if (step == null) {
            throw new IllegalArgumentException("自增步长不能为空");
        }
        this.step = step;
    }

    /**
     * 获取自增步长。
     *
     * @return 自增步长
     */
    public Number getStep() {
        return step;
    }
}
