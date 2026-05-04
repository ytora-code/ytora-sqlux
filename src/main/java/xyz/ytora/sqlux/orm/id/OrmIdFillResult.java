package xyz.ytora.sqlux.orm.id;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ORM 主键填充结果。
 *
 * <p>用于告诉 INSERT 阶段本次插入前需要写入哪些框架侧主键列。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OrmIdFillResult {

    private static final OrmIdFillResult EMPTY = new OrmIdFillResult(Collections.emptySet());

    private final Set<String> filledColumns;

    /**
     * 创建主键填充结果。
     *
     * @param filledColumns 已处理的主键列
     */
    public OrmIdFillResult(Set<String> filledColumns) {
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
    public static OrmIdFillResult empty() {
        return EMPTY;
    }

    /**
     * 判断是否存在框架侧主键列。
     *
     * @return 发生填充时返回 {@code true}
     */
    public boolean hasFilled() {
        return !filledColumns.isEmpty();
    }

    /**
     * 获取已处理主键列。
     *
     * @return 不可变主键列集合
     */
    public Set<String> getFilledColumns() {
        return filledColumns;
    }
}
