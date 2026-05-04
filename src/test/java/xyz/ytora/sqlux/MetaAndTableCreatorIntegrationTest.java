package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.meta.DefaultMetaService;
import xyz.ytora.sqlux.meta.model.ForeignKeyMeta;
import xyz.ytora.sqlux.meta.model.IndexMeta;
import xyz.ytora.sqlux.meta.model.TableMeta;
import xyz.ytora.sqlux.meta.model.ViewMeta;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.creator.TableCreateResult;
import xyz.ytora.sqlux.orm.creator.TableCreators;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaAndTableCreatorIntegrationTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.resetGlobal();
        SqluxTestSupport.registerCommonTypeHandlers();
        SqluxTestSupport.dropObjects(
                "DROP VIEW IF EXISTS test.sqlux_test_user_view",
                "DROP TABLE IF EXISTS test.sqlux_test_user CASCADE",
                "DROP TABLE IF EXISTS test.sqlux_test_dept CASCADE"
        );
    }

    // 验证自动建表只会创建缺失表，并且元数据服务能够读取表、主键和列结构信息。
    @Test
    void createMissingTablesBuildsTablesAndMetaServiceReadsColumns() throws SQLException {
        List<TableCreateResult> first;
        try (Connection connection = SqluxTestSupport.provider().getConnection()) {
            first = TableCreators.createMissingTables(connection, DbType.POSTGRESQL,
                    Arrays.asList(SqluxTestDept.class, SqluxTestUser.class));
        }
        assertEquals(2, first.size());
        assertTrue(first.get(0).isCreated() || first.get(1).isCreated());

        List<TableCreateResult> second;
        try (Connection connection = SqluxTestSupport.provider().getConnection()) {
            second = TableCreators.createMissingTables(connection, DbType.POSTGRESQL,
                    Arrays.asList(SqluxTestDept.class, SqluxTestUser.class));
        }
        assertFalse(second.get(0).isCreated());
        assertFalse(second.get(1).isCreated());

        DefaultMetaService metaService = new DefaultMetaService(SqluxTestSupport.provider());
        assertEquals(DbType.POSTGRESQL, metaService.inferDbType());
        assertTrue(metaService.listSchemas(null).contains("test"));

        List<TableMeta> tables = metaService.listTables(null, "test", "sqlux_test_user");
        assertEquals(1, tables.size());
        TableMeta table = tables.get(0);
        assertEquals("sqlux_test_user", table.getName());
        assertTrue(table.getPrimaryKeys().contains("id"));
        assertTrue(table.getColumnMetas().stream().anyMatch(item -> "login_name".equals(item.getColumnName())));
        assertTrue(table.getColumnMetas().stream().anyMatch(item -> "profile".equals(item.getColumnName())));
    }

    // 验证元数据服务除了表结构外，还能正确读取视图、索引和外键信息。
    @Test
    void metaServiceReadsViewsIndexesAndForeignKeys() {
        SqluxTestSupport.recreateManagedTables();
        SqluxTestSupport.execute(
                "ALTER TABLE test.sqlux_test_user " +
                        "ADD CONSTRAINT fk_sqlux_test_user_dept " +
                        "FOREIGN KEY (dept_id) REFERENCES test.sqlux_test_dept(id)"
        );
        SqluxTestSupport.execute("CREATE INDEX idx_sqlux_test_user_status ON test.sqlux_test_user(status)");
        SqluxTestSupport.execute(
                "CREATE VIEW test.sqlux_test_user_view AS " +
                        "SELECT id, login_name FROM test.sqlux_test_user"
        );

        DefaultMetaService metaService = new DefaultMetaService(SqluxTestSupport.provider());
        List<ViewMeta> views = metaService.listViews(null, "test", "sqlux_test_user_view");
        List<ForeignKeyMeta> foreignKeys = metaService.listForeignKeys(null, "test", "sqlux_test_user");
        List<IndexMeta> indexes = metaService.listIndexes(null, "test", "sqlux_test_user");

        assertEquals(1, views.size());
        assertTrue(foreignKeys.stream().anyMatch(item -> "fk_sqlux_test_user_dept".equalsIgnoreCase(item.getName())));
        assertTrue(indexes.stream().anyMatch(item -> "idx_sqlux_test_user_status".equalsIgnoreCase(item.getName())));
    }
}
