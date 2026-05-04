package xyz.ytora.sqlux.orm.creator.model;

import java.util.Collections;
import java.util.List;

/**
 * 实体类对应的数据库表元数据。
 *
 * @author ytora
 * @since 1.0
 */
public class EntityTableMeta {

    private final Class<?> entityClass;

    private final String tableName;

    private final List<String> keyColumns;

    private final String comment;

    private final List<EntityColumnMeta> columns;

    public EntityTableMeta(Class<?> entityClass, String tableName, List<String> keyColumns,
                           String comment, List<EntityColumnMeta> columns) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.keyColumns = Collections.unmodifiableList(keyColumns);
        this.comment = comment;
        this.columns = Collections.unmodifiableList(columns);
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public String getComment() {
        return comment;
    }

    public List<EntityColumnMeta> getColumns() {
        return columns;
    }
}
