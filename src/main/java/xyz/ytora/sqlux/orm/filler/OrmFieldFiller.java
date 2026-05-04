package xyz.ytora.sqlux.orm.filler;

import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.orm.EntityFieldMeta;
import xyz.ytora.sqlux.orm.EntityMeta;
import xyz.ytora.sqlux.orm.EntityMetas;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ORM 字段自动填充器。
 *
 * <p>在 INSERT 或 UPDATE 实体参数读取前，根据字段上的 {@code @Column(fill = ..., filler = ...)}
 * 调用对应填充器，并通过 setter 写回实体对象。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class OrmFieldFiller {

    /**
     * 工具类不允许实例化。
     */
    private OrmFieldFiller() {
    }

    /**
     * INSERT 前填充实体字段。
     *
     * @param entity 待插入实体
     * @return 字段填充结果
     */
    public static OrmFillResult fillBeforeInsert(Object entity) {
        return fill(entity, true);
    }

    /**
     * UPDATE 前填充实体字段。
     *
     * @param entity 待更新实体
     * @return 字段填充结果
     */
    public static OrmFillResult fillBeforeUpdate(Object entity) {
        return fill(entity, false);
    }

    /**
     * 清空填充器实例缓存。
     */
    public static void clearCache() {
        OrmFillerFactory.clearCache();
    }

    /**
     * 执行字段填充。
     *
     * @param entity 实体对象
     * @param insert 是否为 INSERT 场景
     * @return 字段填充结果
     */
    private static OrmFillResult fill(Object entity, boolean insert) {
        if (entity == null) {
            return OrmFillResult.empty();
        }
        EntityMeta meta = EntityMetas.get(entity.getClass());
        Set<String> filledColumns = new LinkedHashSet<>();
        for (EntityFieldMeta field : meta.getFields()) {
            if (!supportFill(field, insert)) {
                continue;
            }
            fillField(entity, field, insert);
            filledColumns.add(field.getColumnName());
        }
        if (filledColumns.isEmpty()) {
            return OrmFillResult.empty();
        }
        return new OrmFillResult(filledColumns);
    }

    /**
     * 判断字段是否支持当前场景填充。
     *
     * @param field 字段元数据
     * @param insert 是否为 INSERT 场景
     * @return 支持时返回 {@code true}
     */
    private static boolean supportFill(EntityFieldMeta field, boolean insert) {
        if (field.getSetter() == null || field.getFillerType() == null
                || field.getFillerType() == FillerAdapter.class) {
            return false;
        }
        FillType fillType = field.getFillType();
        if (fillType == FillType.NONE) {
            return false;
        }
        if (insert) {
            return fillType == FillType.INSERT || fillType == FillType.INSERT_UPDATE;
        }
        return fillType == FillType.UPDATE || fillType == FillType.INSERT_UPDATE;
    }

    /**
     * 填充单个字段。
     *
     * @param entity 实体对象
     * @param field 字段元数据
     * @param insert 是否为 INSERT 场景
     */
    private static void fillField(Object entity, EntityFieldMeta field, boolean insert) {
        IFiller filler = OrmFillerFactory.getFiller(field.getFillerType());
        Object current = OrmFillAccess.readValue(entity, field);
        boolean overwrite = insert ? filler.overwriteOnInsert() : filler.overwriteOnUpdate();
        if (current != null && !overwrite) {
            return;
        }
        Object value = insert ? filler.onInsert() : filler.onUpdate();
        if (value == null) {
            return;
        }
        OrmFillAccess.writeValue(entity, field,
                OrmFillValueConverter.convertValue(value, field.getField().getType()));
    }

    /**
     * 获取填充器实例。
     *
     * @param fillerType 填充器类型
     * @return 填充器实例
     */
    private static IFiller getFiller(Class<? extends IFiller> fillerType) {
        return OrmFillerFactory.getFiller(fillerType);
    }

    /**
     * 创建填充器实例。
     *
     * @param fillerType 填充器类型
     * @return 填充器实例
     */
    private static IFiller newInstance(Class<? extends IFiller> fillerType) {
        return OrmFillerFactory.newInstance(fillerType);
    }

    /**
     * 读取实体字段当前值。
     *
     * @param entity 实体对象
     * @param field 字段元数据
     * @return 字段值；缺少 getter 时返回 {@code null}
     */
    private static Object readValue(Object entity, EntityFieldMeta field) {
        return OrmFillAccess.readValue(entity, field);
    }

    /**
     * 写入实体字段值。
     *
     * @param entity 实体对象
     * @param field 字段元数据
     * @param value 字段值
     */
    private static void writeValue(Object entity, EntityFieldMeta field, Object value) {
        OrmFillAccess.writeValue(entity, field, value);
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
        return OrmFillAccess.invoke(method, target, args);
    }

    /**
     * 将填充值转换为字段类型。
     *
     * @param value 填充值
     * @param targetType 字段类型
     * @return 转换后的字段值
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        return OrmFillValueConverter.convertValue(value, targetType);
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
