package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.creator.model.EntityColumnMeta;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.TypeMapper;
import xyz.ytora.sqlux.util.NamedUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将实体类解析为建表元数据。
 *
 * @author ytora
 * @since 1.0
 */
public final class EntityTableParser {

    private EntityTableParser() {
    }

    /**
     * 解析实体类的表结构元数据。
     *
     * @param entityClass 实体类型；必须继承 {@link AbsEntity}
     * @param typeMapper 类型映射器；用于把 Java 字段类型转换为数据库字段类型
     * @return 表结构元数据
     */
    public static EntityTableMeta parse(Class<?> entityClass, TypeMapper typeMapper) {
        if (entityClass == null) {
            throw new IllegalArgumentException("实体类型不能为空");
        }
        if (!AbsEntity.class.isAssignableFrom(entityClass)) {
            throw new IllegalArgumentException("实体类型必须继承AbsEntity: " + entityClass.getName());
        }
        if (typeMapper == null) {
            throw new IllegalArgumentException("TypeMapper不能为空");
        }
        Table table = entityClass.getAnnotation(Table.class);
        List<EntityColumnMeta> columns = parseColumns(entityClass, typeMapper);
        return new EntityTableMeta(entityClass, parseTableName(entityClass, table), parseKeys(table),
                table == null ? "" : table.comment(), columns);
    }

    private static List<EntityColumnMeta> parseColumns(Class<?> entityClass, TypeMapper typeMapper) {
        List<OrderedField> fields = listFields(entityClass);
        fields.sort(Comparator.comparingInt((OrderedField left) -> columnIndex(left.field)).thenComparingInt(left -> left.order));

        Map<String, OrderedField> effectiveFields = new LinkedHashMap<>();
        for (OrderedField orderedField : fields) {
            Field field = orderedField.field;
            String columnName = NamedUtil.parseColumnName(field);
            effectiveFields.put(columnName, orderedField);
        }

        List<EntityColumnMeta> columns = new ArrayList<>();
        for (Map.Entry<String, OrderedField> entry : effectiveFields.entrySet()) {
            Field field = entry.getValue().field;
            String columnName = entry.getKey();
            if (!NamedUtil.isColumnExists(field)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String sqlType = column != null && column.type() != ColumnType.AUTO
                    ? typeMapper.sqlType(column.type())
                    : typeMapper.sqlType(field.getType());
            columns.add(new EntityColumnMeta(field, columnName, field.getType(), sqlType,
                    column != null && column.notNull(), column == null ? "" : column.comment()));
        }
        return columns;
    }

    private static String parseTableName(Class<?> entityClass, Table table) {
        if (table != null && table.value() != null && !table.value().trim().isEmpty()) {
            return table.value();
        }
        return NamedUtil.parseTableName(entityClass);
    }

    private static List<String> parseKeys(Table table) {
        if (table == null || table.key() == null || table.key().length == 0) {
            return Collections.singletonList("id");
        }
        return Arrays.asList(table.key());
    }

    private static int columnIndex(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null ? Integer.MAX_VALUE : column.index();
    }

    private static List<OrderedField> listFields(Class<?> type) {
        List<OrderedField> fields = new ArrayList<>();
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);

        int order = 0;
        for (Class<?> item : hierarchy) {
            Field[] declaredFields = item.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(new OrderedField(field, order++));
                }
            }
        }
        return fields;
    }

    private static final class OrderedField {

        private final Field field;

        private final int order;

        private OrderedField(Field field, int order) {
            this.field = field;
            this.order = order;
        }
    }
}
