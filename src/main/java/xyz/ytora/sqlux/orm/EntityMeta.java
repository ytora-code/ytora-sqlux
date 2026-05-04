package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Version;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 实体类元数据。
 *
 * <p>该对象是不可变缓存，保存实体字段映射和常用特殊字段，供 ORM 读写复用。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class EntityMeta {

    private final Class<?> entityType;

    private final List<EntityFieldMeta> fields;

    private final Map<String, EntityFieldMeta> fieldsByColumn;

    private final String[] keyColumns;

    private final IdType idType;

    private final EntityFieldMeta versionField;

    private final EntityFieldMeta logicDeleteField;

    /**
     * 创建实体类元数据。
     *
     * @param entityType 实体类型
     * @param fields 字段元数据
     */
    public EntityMeta(Class<?> entityType, List<EntityFieldMeta> fields) {
        this.entityType = entityType;
        this.fields = Collections.unmodifiableList(fields);
        this.fieldsByColumn = indexFields(fields);
        this.keyColumns = parseKeyColumns(entityType);
        this.idType = parseIdType(entityType);
        this.versionField = findByType(fields, Version.class);
        this.logicDeleteField = findByType(fields, LogicDelete.class);
    }

    /**
     * 获取实体类型。
     *
     * @return 实体类型
     */
    public Class<?> getEntityType() {
        return entityType;
    }

    /**
     * 获取字段元数据。
     *
     * @return 不可变字段列表
     */
    public List<EntityFieldMeta> getFields() {
        return fields;
    }

    /**
     * 按数据库列名查找字段元数据。
     *
     * @param columnName 数据库列名
     * @return 字段元数据；找不到时返回 {@code null}
     */
    public EntityFieldMeta getFieldByColumn(String columnName) {
        if (columnName == null) {
            return null;
        }
        EntityFieldMeta direct = fieldsByColumn.get(columnName);
        if (direct != null) {
            return direct;
        }
        return fieldsByColumn.get(columnName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * 获取主键列名。
     *
     * @return 主键列名数组
     */
    public String[] getKeyColumns() {
        return keyColumns.clone();
    }

    /**
     * 获取主键生成策略。
     *
     * @return 主键生成策略；未配置时返回 {@link IdType#NONE}
     */
    public IdType getIdType() {
        return idType;
    }

    /**
     * 获取乐观锁字段。
     *
     * @return 乐观锁字段；不存在时返回 {@code null}
     */
    public EntityFieldMeta getVersionField() {
        return versionField;
    }

    /**
     * 获取逻辑删除字段。
     *
     * @return 逻辑删除字段；不存在时返回 {@code null}
     */
    public EntityFieldMeta getLogicDeleteField() {
        return logicDeleteField;
    }

    /**
     * 构建列名索引。
     *
     * @param fields 字段元数据
     * @return 列名索引
     */
    private Map<String, EntityFieldMeta> indexFields(List<EntityFieldMeta> fields) {
        Map<String, EntityFieldMeta> index = new LinkedHashMap<>();
        for (EntityFieldMeta field : fields) {
            index.put(field.getColumnName(), field);
            index.put(field.getColumnName().toLowerCase(Locale.ENGLISH), field);
        }
        return Collections.unmodifiableMap(index);
    }

    /**
     * 解析实体主键列。
     *
     * @param type 实体类型
     * @return 主键列名
     */
    private String[] parseKeyColumns(Class<?> type) {
        Table table = type.getAnnotation(Table.class);
        if (table == null || table.key() == null || table.key().length == 0) {
            return new String[]{"id"};
        }
        return table.key();
    }

    /**
     * 解析实体主键生成策略。
     *
     * @param type 实体类型
     * @return 主键生成策略
     */
    private IdType parseIdType(Class<?> type) {
        Table table = type.getAnnotation(Table.class);
        if (table == null || table.idType() == null) {
            return IdType.NONE;
        }
        return table.idType();
    }

    /**
     * 查找指定字段类型。
     *
     * @param fields 字段元数据
     * @param fieldType 字段类型
     * @return 字段元数据
     */
    private EntityFieldMeta findByType(List<EntityFieldMeta> fields, Class<?> fieldType) {
        for (EntityFieldMeta field : fields) {
            if (field.getField().getType() == fieldType) {
                return field;
            }
        }
        return null;
    }
}
