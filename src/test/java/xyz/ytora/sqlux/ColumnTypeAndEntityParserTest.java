package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.creator.EntityTableParser;
import xyz.ytora.sqlux.orm.creator.model.EntityColumnMeta;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.DefaultTypeMapper;
import xyz.ytora.sqlux.translate.MysqlTypeMapper;
import xyz.ytora.sqlux.translate.PostgreSqlTypeMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ColumnTypeAndEntityParserTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.resetGlobal();
        SqluxTestSupport.registerCommonTypeHandlers();
    }

    // 验证框架内置标准字段类型在不同数据库方言下会被翻译成各自正确的物理字段类型。
    @Test
    void standardColumnTypesMapToDifferentDialects() {
        MysqlTypeMapper mysql = new MysqlTypeMapper();
        PostgreSqlTypeMapper pg = new PostgreSqlTypeMapper();
        DefaultTypeMapper defaults = new DefaultTypeMapper();

        assertEquals("bigint", mysql.sqlType(ColumnType.INT8));
        assertEquals("tinyint(1)", mysql.sqlType(ColumnType.BOOLEAN));
        assertEquals("char(36)", mysql.sqlType(ColumnType.UUID));
        assertEquals("jsonb", pg.sqlType(ColumnType.JSON));
        assertEquals("bytea", pg.sqlType(ColumnType.BLOB));
        assertEquals("timestamp", pg.sqlType(ColumnType.DATETIME));
        assertEquals("varchar(64)", defaults.sqlType(ColumnType.VARCHAR64));
        assertEquals("decimal(19, 4)", defaults.sqlType(ColumnType.DECIMAL_19_4));
    }

    // 验证建表元数据解析时会优先采用 @Column 指定的标准类型，并忽略实体中声明为不存在字段的属性。
    @Test
    void entityTableParserUsesDeclaredTypesAndSkipsNonExistingFields() {
        EntityTableMeta table = EntityTableParser.parse(SqluxTestUser.class, new PostgreSqlTypeMapper());
        Map<String, EntityColumnMeta> columns = byName(table);

        assertEquals("sqlux_test_user", table.getTableName());
        assertEquals("bigint", columns.get("id").getSqlType());
        assertEquals("varchar(64)", columns.get("login_name").getSqlType());
        assertEquals("jsonb", columns.get("profile").getSqlType());
        assertEquals("uuid", columns.get("external_id").getSqlType());
        assertEquals("text", columns.get("remark").getSqlType());
        assertEquals("timestamp", columns.get("created_at").getSqlType());
        assertEquals("smallint", columns.get("deleted").getSqlType());
        assertFalse(columns.containsKey("transient_note"));
    }

    // 验证字段未显式指定类型，或者指定为 AUTO 时，会回退到根据 Java 字段类型自动推断数据库类型。
    @Test
    void columnAnnotationWithoutTypeAndAutoBothFallbackToJavaType() {
        EntityTableMeta table = EntityTableParser.parse(AutoTypeEntity.class, new PostgreSqlTypeMapper());
        Map<String, EntityColumnMeta> columns = byName(table);

        assertEquals("timestamp", columns.get("created_at").getSqlType());
        assertEquals("timestamp", columns.get("updated_at").getSqlType());
        assertEquals("varchar(255)", columns.get("name").getSqlType());
    }

    // 验证建表字段顺序会遵循 @Column(index) 指定的排序规则，并与默认顺序共同组成最终列顺序。
    @Test
    void parserRespectsFieldIndexOrdering() {
        EntityTableMeta table = EntityTableParser.parse(OrderedEntity.class, new PostgreSqlTypeMapper());

        assertEquals("second", table.getColumns().get(0).getColumnName());
        assertEquals("first", table.getColumns().get(1).getColumnName());
        assertEquals("id", table.getColumns().get(2).getColumnName());
        assertEquals("tail", table.getColumns().get(3).getColumnName());
    }

    private Map<String, EntityColumnMeta> byName(EntityTableMeta table) {
        Map<String, EntityColumnMeta> result = new LinkedHashMap<>();
        for (EntityColumnMeta column : table.getColumns()) {
            result.put(column.getColumnName(), column);
        }
        return result;
    }

    @Table("sqlux_test_auto_type")
    static class AutoTypeEntity extends AbsEntity {

        @Column
        private LocalDateTime createdAt;

        @Column(type = ColumnType.AUTO)
        private LocalDateTime updatedAt;

        private String name;

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Table("sqlux_test_ordered")
    static class OrderedEntity extends AbsEntity {

        @Column(index = 2)
        private String first;

        @Column(index = 1)
        private String second;

        private String tail;

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }

        public String getTail() {
            return tail;
        }

        public void setTail(String tail) {
            this.tail = tail;
        }
    }
}
