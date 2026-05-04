package xyz.ytora.sqlux.rw;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 类型处理器注册表。
 *
 * <p>注册表按注册顺序匹配，先注册的处理器优先级更高。框架默认仍兼容字段类型自身实现
 * {@link SqlReader}、{@link SqlFieldReader} 和 {@link SqlWriter} 的旧写法。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class TypeHandlers {

    private static final List<TypeHandler<?>> HANDLERS = new ArrayList<>();

    /**
     * 工具类不允许实例化。
     */
    private TypeHandlers() {
    }

    /**
     * 注册类型处理器。
     *
     * @param handler 类型处理器；为 {@code null} 时忽略
     */
    public static synchronized void register(TypeHandler<?> handler) {
        if (handler != null) {
            HANDLERS.add(handler);
        }
    }

    /**
     * 移除类型处理器。
     *
     * @param handler 类型处理器；为 {@code null} 时忽略
     */
    public static synchronized void remove(TypeHandler<?> handler) {
        if (handler != null) {
            HANDLERS.remove(handler);
        }
    }

    /**
     * 清空类型处理器。
     */
    public static synchronized void clear() {
        HANDLERS.clear();
    }

    /**
     * 获取类型处理器快照。
     *
     * @return 不可变处理器列表
     */
    public static synchronized List<TypeHandler<?>> list() {
        return Collections.unmodifiableList(new ArrayList<>(HANDLERS));
    }

    /**
     * 将实体字段值转换为数据库写入值。
     *
     * @param value 实体字段值
     * @param field 实体字段元数据
     * @return 数据库写入值
     */
    public static Object write(Object value, Field field) {
        TypeHandler<?> handler = findHandler(field, value);
        if (handler != null) {
            return write(handler, value, field);
        }
        return value;
    }

    /**
     * 将数据库字段值转换为实体字段值。
     *
     * @param value 数据库原始值
     * @param field 实体字段元数据
     * @return 实体字段值
     */
    public static Object read(Object value, Field field) {
        if (field == null) {
            return value;
        }
        TypeHandler<?> handler = findHandler(field, value);
        if (handler == null) {
            return value;
        }
        return read(handler, value, field);
    }

    /**
     * 查找字段对应的类型处理器。
     *
     * @param field 实体字段
     * @param value 当前字段值
     * @return 类型处理器；找不到时返回 {@code null}
     */
    private static TypeHandler<?> findHandler(Field field, Object value) {
        Class<?> type = field == null ? (value == null ? null : value.getClass()) : field.getType();
        if (type == null) {
            return null;
        }
        for (TypeHandler<?> handler : list()) {
            if (handler.supports(type)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * 调用类型处理器写转换。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object write(TypeHandler handler, Object value, Field field) {
        return handler.write(value, field);
    }

    /**
     * 调用类型处理器读转换。
     */
    @SuppressWarnings({"rawtypes"})
    private static Object read(TypeHandler handler, Object value, Field field) {
        return handler.read(value, field);
    }
}
