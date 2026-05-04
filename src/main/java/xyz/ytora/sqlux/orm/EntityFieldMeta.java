package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.orm.filler.IFiller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 实体字段元数据。
 *
 * <p>缓存字段、列名、getter 和 setter，避免 ORM 映射时反复扫描反射信息。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class EntityFieldMeta {

    private final Field field;

    private final String columnName;

    private final Method getter;

    private final Method setter;

    private final FillType fillType;

    private final Class<? extends IFiller> fillerType;

    /**
     * 创建实体字段元数据。
     *
     * @param field 实体字段
     * @param columnName 数据库列名
     * @param getter 字段 getter；不存在时为 {@code null}
     * @param setter 字段 setter；不存在时为 {@code null}
     * @param fillType 自动填充时机
     * @param fillerType 自动填充器类型
     */
    public EntityFieldMeta(Field field, String columnName, Method getter, Method setter,
                           FillType fillType, Class<? extends IFiller> fillerType) {
        this.field = field;
        this.columnName = columnName;
        this.getter = getter;
        this.setter = setter;
        this.fillType = fillType == null ? FillType.NONE : fillType;
        this.fillerType = fillerType;
    }

    /**
     * 获取实体字段。
     *
     * @return 实体字段
     */
    public Field getField() {
        return field;
    }

    /**
     * 获取数据库列名。
     *
     * @return 数据库列名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 获取字段 getter。
     *
     * @return getter；不存在时返回 {@code null}
     */
    public Method getGetter() {
        return getter;
    }

    /**
     * 获取字段 setter。
     *
     * @return setter；不存在时返回 {@code null}
     */
    public Method getSetter() {
        return setter;
    }

    /**
     * 获取自动填充时机。
     *
     * @return 自动填充时机
     */
    public FillType getFillType() {
        return fillType;
    }

    /**
     * 获取自动填充器类型。
     *
     * @return 自动填充器类型；未配置时可能为 {@code null}
     */
    public Class<? extends IFiller> getFillerType() {
        return fillerType;
    }
}
