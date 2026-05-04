package xyz.ytora.sqlux.orm.type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 对象树工具。
 *
 * @author ytora
 * @since 1.0
 */
final class JsonTree {

    private JsonTree() {
    }

    static void checkJsonObj(Object root) {
        if (!(root instanceof Map<?, ?>)) {
            throw new IllegalStateException("当前不是json对象，无法处理为json对象");
        }
    }

    static void checkJsonArray(Object root) {
        if (!(root instanceof List<?>)) {
            throw new IllegalStateException("当前不是json数组，无法处理为json数组");
        }
    }

    @SuppressWarnings("unchecked")
    static LinkedHashMap<String, Object> getSourceMap(Object root, int index) {
        checkJsonArray(root);
        Object value = sourceList(root).get(index);
        if (!(value instanceof LinkedHashMap<?, ?>)) {
            throw new IllegalStateException("JSON数组下标[" + index + "]的数据不是json对象");
        }
        return (LinkedHashMap<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    static LinkedHashMap<String, Object> sourceMap(Object root) {
        return (LinkedHashMap<String, Object>) root;
    }

    @SuppressWarnings("unchecked")
    static List<Object> sourceList(Object root) {
        return (List<Object>) root;
    }

    static Object normalize(Object value) {
        if (value instanceof Json) {
            return normalize(((Json) value).unwrap());
        }
        if (value instanceof Map<?, ?>) {
            return copyMap((Map<?, ?>) value);
        }
        if (value instanceof List<?>) {
            return copyList((List<?>) value);
        }
        return value;
    }

    static LinkedHashMap<String, Object> copyMap(Map<?, ?> map) {
        if (map == null) {
            throw new IllegalArgumentException("JSON对象不能为空");
        }
        LinkedHashMap<String, Object> target = new LinkedHashMap<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            target.put(String.valueOf(entry.getKey()), normalize(entry.getValue()));
        }
        return target;
    }

    static List<Object> copyList(List<?> list) {
        if (list == null) {
            throw new IllegalArgumentException("JSON数组不能为空");
        }
        List<Object> target = new ArrayList<>(list.size());
        for (Object item : list) {
            target.add(normalize(item));
        }
        return target;
    }
}
