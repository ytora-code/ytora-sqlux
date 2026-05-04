package xyz.ytora.sqlux.orm;

import java.util.Locale;
import java.util.Map;

/**
 * ORM 原始行读取工具。
 *
 * <p>统一处理 JDBC 列名大小写差异，让实体映射流程只关注字段绑定。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class OrmRowAccess {

    private OrmRowAccess() {
    }

    static boolean containsKey(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return true;
        }
        String lowerKey = key.toLowerCase(Locale.ENGLISH);
        for (String candidate : row.keySet()) {
            if (candidate != null && candidate.toLowerCase(Locale.ENGLISH).equals(lowerKey)) {
                return true;
            }
        }
        return false;
    }

    static Object getValue(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lowerKey = key.toLowerCase(Locale.ENGLISH);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().toLowerCase(Locale.ENGLISH).equals(lowerKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
