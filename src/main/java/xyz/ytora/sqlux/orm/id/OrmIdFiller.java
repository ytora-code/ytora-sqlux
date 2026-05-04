package xyz.ytora.sqlux.orm.id;

import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.sqlux.orm.EntityFieldMeta;
import xyz.ytora.sqlux.orm.EntityMeta;
import xyz.ytora.sqlux.orm.EntityMetas;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ORM 主键填充器。
 *
 * <p>在 INSERT 实体参数读取前，根据 {@code @Table(idType = ...)} 为主键字段生成值，
 * 并通过 setter 回填到实体对象。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class OrmIdFiller {

    /**
     * 工具类不允许实例化。
     */
    private OrmIdFiller() {
    }

    /**
     * 插入前填充实体主键。
     *
     * <p>仅当实体配置了非 {@link IdType#NONE} 主键策略，且主键字段当前值为空时才会生成 ID。</p>
     *
     * @param entity 待插入实体
     * @return 主键填充结果
     */
    public static OrmIdFillResult fillBeforeInsert(Object entity) {
        if (entity == null) {
            return OrmIdFillResult.empty();
        }
        EntityMeta meta = EntityMetas.get(entity.getClass());
        IdType idType = meta.getIdType();
        if (idType == IdType.NONE) {
            return OrmIdFillResult.empty();
        }
        Set<String> filledColumns = new LinkedHashSet<>();
        for (String keyColumn : meta.getKeyColumns()) {
            EntityFieldMeta field = meta.getFieldByColumn(keyColumn);
            if (field == null) {
                continue;
            }
            if (field.getSetter() == null) {
                throw new IllegalArgumentException("主键字段缺少setter: " + field.getField().getName());
            }
            Object current = readValue(entity, field);
            if (current != null) {
                filledColumns.add(field.getColumnName());
                continue;
            }
            Object generated = OrmIdGenerator.nextId(idType);
            if (generated == null) {
                continue;
            }
            writeValue(entity, field, convertValue(generated, field.getField().getType()));
            filledColumns.add(field.getColumnName());
        }
        if (filledColumns.isEmpty()) {
            return OrmIdFillResult.empty();
        }
        return new OrmIdFillResult(filledColumns);
    }

    /**
     * 读取实体字段当前值。
     *
     * @param entity 实体对象
     * @param field 字段元数据
     * @return 字段值；缺少 getter 时返回 {@code null}
     */
    private static Object readValue(Object entity, EntityFieldMeta field) {
        Method getter = field.getGetter();
        if (getter == null) {
            return null;
        }
        return invoke(getter, entity);
    }

    /**
     * 写入实体字段值。
     *
     * @param entity 实体对象
     * @param field 字段元数据
     * @param value 字段值
     */
    private static void writeValue(Object entity, EntityFieldMeta field, Object value) {
        invoke(field.getSetter(), entity, value);
    }

    /**
     * 调用反射方法。
     *
     * @param method 方法
     * @param target 调用目标
     * @param args 参数
     * @return 返回值
     */
    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问方法: " + method.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("方法执行失败: " + method.getName(), e.getTargetException());
        }
    }

    /**
     * 将生成的 ID 转换为主键字段类型。
     *
     * @param value 原始 ID
     * @param targetType 目标字段类型
     * @return 转换后的 ID
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        Class<?> boxedType = box(targetType);
        if (boxedType.isInstance(value)) {
            return value;
        }
        if (CharSequence.class.isAssignableFrom(boxedType)) {
            return String.valueOf(value);
        }
        if (Number.class.isAssignableFrom(boxedType) && value instanceof Number) {
            Number number = (Number) value;
            if (boxedType.equals(Integer.class)) {
                return number.intValue();
            }
            if (boxedType.equals(Long.class)) {
                return number.longValue();
            }
            if (boxedType.equals(Short.class)) {
                return number.shortValue();
            }
            if (boxedType.equals(Byte.class)) {
                return number.byteValue();
            }
            if (boxedType.equals(Float.class)) {
                return number.floatValue();
            }
            if (boxedType.equals(Double.class)) {
                return number.doubleValue();
            }
            if (boxedType.equals(BigDecimal.class)) {
                return new BigDecimal(number.toString());
            }
            if (boxedType.equals(BigInteger.class)) {
                return BigInteger.valueOf(number.longValue());
            }
        }
        return value;
    }

    /**
     * 将 Java 基本类型转为包装类型。
     *
     * @param type 原始类型
     * @return 包装类型或原类型
     */
    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type.equals(int.class)) {
            return Integer.class;
        }
        if (type.equals(long.class)) {
            return Long.class;
        }
        if (type.equals(short.class)) {
            return Short.class;
        }
        if (type.equals(byte.class)) {
            return Byte.class;
        }
        if (type.equals(float.class)) {
            return Float.class;
        }
        if (type.equals(double.class)) {
            return Double.class;
        }
        if (type.equals(boolean.class)) {
            return Boolean.class;
        }
        if (type.equals(char.class)) {
            return Character.class;
        }
        return type;
    }
}
