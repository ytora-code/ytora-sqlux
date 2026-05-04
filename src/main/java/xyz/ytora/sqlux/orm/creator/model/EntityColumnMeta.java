package xyz.ytora.sqlux.orm.creator.model;

import java.lang.reflect.Field;

/**
 * 实体字段对应的数据库列元数据。
 *
 * @author ytora
 * @since 1.0
 */
public class EntityColumnMeta {

    private final Field field;

    private final String columnName;

    private final Class<?> javaType;

    private final String sqlType;

    private final boolean notNull;

    private final String comment;

    public EntityColumnMeta(Field field, String columnName, Class<?> javaType, String sqlType,
                            boolean notNull, String comment) {
        this.field = field;
        this.columnName = columnName;
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.notNull = notNull;
        this.comment = comment;
    }

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public String getComment() {
        return comment;
    }
}
