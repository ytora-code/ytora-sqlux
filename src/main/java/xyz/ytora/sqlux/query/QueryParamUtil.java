package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.query.model.QueryField;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 查询参数解析工具。
 *
 * @author ytora
 * @since 1.0
 */
public final class QueryParamUtil {

    private QueryParamUtil() {
    }

    public static String firstString(Object value) {
        Object first = firstValue(value);
        return first == null ? null : String.valueOf(first);
    }

    public static boolean isTrue(Object value) {
        String text = firstString(value);
        return "true".equalsIgnoreCase(text) || "1".equals(text) || "yes".equalsIgnoreCase(text);
    }

    public static List<String> splitCsv(Object value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                addCsv(result, item);
            }
            return result;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addCsv(result, Array.get(value, i));
            }
            return result;
        }
        addCsv(result, value);
        return result;
    }

    public static List<Object> convertValues(QueryField field, Object value) {
        List<String> items = splitCsv(value);
        List<Object> result = new ArrayList<>();
        for (String item : items) {
            result.add(convertValue(field, item));
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convertValue(QueryField field, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof SqlWriter) {
            return ((SqlWriter) value).write();
        }
        Class<?> type = field.getFieldType();
        if (type.isInstance(value)) {
            return value;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (type == String.class) {
            return text;
        }
        if (type == Integer.class || type == Integer.TYPE) {
            return Integer.valueOf(text);
        }
        if (type == Long.class || type == Long.TYPE) {
            return Long.valueOf(text);
        }
        if (type == Short.class || type == Short.TYPE) {
            return Short.valueOf(text);
        }
        if (type == Byte.class || type == Byte.TYPE) {
            return Byte.valueOf(text);
        }
        if (type == Double.class || type == Double.TYPE) {
            return Double.valueOf(text);
        }
        if (type == Float.class || type == Float.TYPE) {
            return Float.valueOf(text);
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            return Boolean.valueOf(text);
        }
        if (type == LocalDate.class) {
            return LocalDate.parse(text);
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(text);
        }
        if (type == LocalTime.class) {
            return LocalTime.parse(text);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), text);
        }
        return value;
    }

    private static Object firstValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return collection.isEmpty() ? null : collection.iterator().next();
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                return item;
            }
            return null;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0 ? null : Array.get(value, 0);
        }
        return value;
    }

    private static void addCsv(List<String> result, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value);
        String[] items = text.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
    }
}
