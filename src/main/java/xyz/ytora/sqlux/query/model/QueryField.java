package xyz.ytora.sqlux.query.model;

import xyz.ytora.sqlux.orm.EntityFieldMeta;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * 查询参数字段元信息。
 *
 * @author ytora
 * @since 1.0
 */
public class QueryField {

    private final Class<?> entityType;

    private final EntityFieldMeta fieldMeta;

    public QueryField(Class<?> entityType, EntityFieldMeta fieldMeta) {
        this.entityType = entityType;
        this.fieldMeta = fieldMeta;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public EntityFieldMeta getFieldMeta() {
        return fieldMeta;
    }

    public String getFieldName() {
        return fieldMeta.getField().getName();
    }

    public String getColumnName() {
        return fieldMeta.getColumnName();
    }

    public Class<?> getFieldType() {
        return fieldMeta.getField().getType();
    }

    public ColumnRef toColumnRef() {
        return ColumnRef.of(entityType, getColumnName());
    }
}
