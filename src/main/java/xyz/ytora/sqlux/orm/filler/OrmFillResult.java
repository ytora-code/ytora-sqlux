package xyz.ytora.sqlux.orm.filler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ORM 字段填充结果。
 *
 * <p>用于告诉 INSERT 阶段哪些自动填充字段需要写入数据库。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OrmFillResult {

    private static final OrmFillResult EMPTY = new OrmFillResult(Collections.emptySet());

    private final Set<String> filledColumns;

    /**
     * 创建字段填充结果。
     *
     * @param filledColumns 需要写入的字段列名
     */
    public OrmFillResult(Set<String> filledColumns) {
        if (filledColumns == null || filledColumns.isEmpty()) {
            this.filledColumns = Collections.emptySet();
        } else {
            this.filledColumns = Collections.unmodifiableSet(new LinkedHashSet<>(filledColumns));
        }
    }

    /**
     * 获取空填充结果。
     *
     * @return 空填充结果
     */
    public static OrmFillResult empty() {
        return EMPTY;
    }

    /**
     * 判断是否存在需要写入的填充列。
     *
     * @return 存在时返回 {@code true}
     */
    public boolean hasFilled() {
        return !filledColumns.isEmpty();
    }

    /**
     * 获取需要写入的填充列。
     *
     * @return 不可变列名集合
     */
    public Set<String> getFilledColumns() {
        return filledColumns;
    }
}
