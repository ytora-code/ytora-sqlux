package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.orm.filler.IFiller;
import xyz.ytora.sqlux.util.NamedUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实体元数据缓存。
 *
 * <p>该类集中解析实体字段映射，ORM 映射、INSERT/UPDATE 取值和主键回填都复用同一份元数据。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class EntityMetas {

    private static final ConcurrentMap<Class<?>, EntityMeta> CACHE = new ConcurrentHashMap<>();

    /**
     * 工具类不允许实例化。
     */
    private EntityMetas() {
    }

    /**
     * 获取实体元数据。
     *
     * @param entityType 实体类型
     * @return 实体元数据
     */
    public static EntityMeta get(Class<?> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("实体类型不能为空");
        }
        EntityMeta cached = CACHE.get(entityType);
        if (cached != null) {
            return cached;
        }
        EntityMeta created = parse(entityType);
        EntityMeta previous = CACHE.putIfAbsent(entityType, created);
        return previous == null ? created : previous;
    }

    /**
     * 清空实体元数据缓存。
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * 解析实体元数据。
     *
     * @param type 实体类型
     * @return 实体元数据
     */
    private static EntityMeta parse(Class<?> type) {
        Map<String, EntityFieldMeta> fields = new LinkedHashMap<>();
        for (Field field : listFields(type)) {
            if (!NamedUtil.isColumnExists(field)) {
                continue;
            }
            Method getter = findGetter(type, field);
            Method setter = findSetter(type, field);
            Column column = field.getAnnotation(Column.class);
            FillType fillType = column == null ? FillType.NONE : column.fillOn();
            Class<? extends IFiller> fillerType = column == null ? null : column.filler();
            String columnName = NamedUtil.parseColumnName(field);
            fields.put(columnName, new EntityFieldMeta(field, columnName, getter, setter,
                    fillType, fillerType));
        }
        return new EntityMeta(type, new ArrayList<>(fields.values()));
    }

    /**
     * 列出实体类型及其父类中的非静态字段。
     *
     * @param type 实体类型
     * @return 字段列表
     */
    private static List<Field> listFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);
        for (Class<?> item : hierarchy) {
            Field[] declaredFields = item.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /**
     * 查找字段 getter。
     *
     * @param type 实体类型
     * @param field 实体字段
     * @return getter；不存在时返回 {@code null}
     */
    private static Method findGetter(Class<?> type, Field field) {
        String upperName = upperFirst(field.getName());
        String[] names = new String[]{"get" + upperName, "is" + upperName, field.getName()};
        for (String name : names) {
            Method method = findMethod(type, name);
            if (method != null
                    && method.getParameterTypes().length == 0
                    && method.getReturnType().equals(field.getType())) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    /**
     * 查找字段 setter。
     *
     * @param type 实体类型
     * @param field 实体字段
     * @return setter；不存在时返回 {@code null}
     */
    private static Method findSetter(Class<?> type, Field field) {
        Method method = findMethod(type, "set" + upperFirst(field.getName()), field.getType());
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        return method;
    }

    /**
     * 在当前类及父类中查找指定签名的方法。
     *
     * @param type 起始查找类型
     * @param name 方法名
     * @param parameterTypes 参数类型
     * @return 方法；找不到时返回 {@code null}
     */
    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 将字符串首字母转为大写。
     *
     * @param name 原始字符串
     * @return 首字母大写后的字符串
     */
    private static String upperFirst(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
