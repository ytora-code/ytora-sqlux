package xyz.ytora.sqlux.orm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ORM 实体 UPDATE 值规划结果。
 *
 * <p>普通字段值会生成 {@code column = ?}，版本锁字段会由 UPDATE 翻译阶段生成
 * {@code version = version + 1} 和 {@code WHERE version = oldVersion}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class OrmUpdateValues {

    private final Map<String, Object> values = new LinkedHashMap<>();

    private String versionColumn;

    private Object versionValue;

    /**
     * 添加普通 UPDATE 字段值。
     *
     * @param column 数据库列名
     * @param value 写入值
     */
    public void putValue(String column, Object value) {
        values.put(column, value);
    }

    /**
     * 获取普通 UPDATE 字段值。
     *
     * @return 不可变字段值映射
     */
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * 设置乐观锁版本字段。
     *
     * @param column 数据库列名
     * @param value 旧版本值
     */
    public void setVersionLock(String column, Object value) {
        this.versionColumn = column;
        this.versionValue = value;
    }

    /**
     * 判断是否包含乐观锁版本字段。
     *
     * @return 包含时返回 {@code true}
     */
    public boolean hasVersionLock() {
        return versionColumn != null;
    }

    /**
     * 获取版本字段列名。
     *
     * @return 版本字段列名
     */
    public String getVersionColumn() {
        return versionColumn;
    }

    /**
     * 获取旧版本值。
     *
     * @return 旧版本值
     */
    public Object getVersionValue() {
        return versionValue;
    }
}
