package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.ytora.sqlux.sql.func.SqlFuncAggregation.alias;

class SqlDslTranslationTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.resetGlobal();
        SqluxTestSupport.registerCommonTypeHandlers();
    }

    // 验证 select DSL 在 PostgreSQL 下能正确翻译出联表、条件、排序和分页 SQL，并保持参数顺序正确。
    @Test
    void selectJoinWhereOrderLimitTranslatesForPostgresql() {
        SqlResult sql = SQL.select(SqluxTestUser::getLoginName, SqluxTestDept::getDeptName)
                .from(SqluxTestUser.class)
                .leftJoin(SqluxTestDept.class, on -> on.eq(SqluxTestUser::getDeptId, SqluxTestDept::getId))
                .where(w -> {
                    w.ge(SqluxTestUser::getAge, 18);
                    w.like(SqluxTestUser::getLoginName, "%a%");
                })
                .orderByDesc(SqluxTestUser::getAge)
                .limit(10)
                .offset(5)
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("FROM \"sqlux_test_user\""));
        assertTrue(sql.getSql().contains("LEFT JOIN \"sqlux_test_dept\""));
        assertTrue(sql.getSql().contains("\"login_name\""));
        assertTrue(sql.getSql().contains("ORDER BY"));
        assertTrue(sql.getSql().contains("LIMIT ?"));
        assertTrue(sql.getSql().contains("OFFSET ?"));
        assertEquals(Arrays.<Object>asList(18, "%%a%%", 10, 5), sql.getParams());
    }

    // 验证 update DSL 支持显式把字段更新为 null，并正确生成对应的占位参数。
    @Test
    void updateSetNullTranslatesForPostgresql() {
        SqlResult sql = SQL.update(SqluxTestUser.class)
                .set(SqluxTestUser::getRemark, null)
                .where(w -> w.eq(SqluxTestUser::getId, 1L))
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().startsWith("UPDATE "));
        assertTrue(sql.getSql().contains("\"remark\" = ?"));
        assertEquals(Arrays.<Object>asList(null, 1L), sql.getParams());
    }

    // 验证逻辑删除实体在执行 delete DSL 时不会物理删除，而是翻译成更新删除标记的 SQL。
    @Test
    void deleteOnLogicDeleteEntityTranslatesToSoftDeleteUpdate() {
        SqlResult sql = SQL.delete()
                .from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, 7L))
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().startsWith("UPDATE "));
        assertTrue(sql.getSql().contains("\"deleted\""));
        assertEquals(Arrays.<Object>asList(1, 7L), sql.getParams());
    }

    // 验证原生 SQL 模式下传入集合参数时，框架会把集合打平为逐个 JDBC 参数。
    @Test
    void rawQueryFlattensCollectionParameters() {
        SqlResult sql = SQL.rawQuery(
                "select * from sqlux_test_user where id in ?",
                Arrays.asList(1L, 2L, 3L)
        ).toSql();

        assertTrue(sql.getSql().contains("id in (?, ?, ?)"));
        assertEquals(Arrays.<Object>asList(1L, 2L, 3L), sql.getParams());
    }

    // 验证原生 SQL 模式支持命名参数，并且会为集合参数自动展开 in 占位符。
    @Test
    void rawQuerySupportsNamedParametersAndCollectionExpansion() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("status", "ENABLED");
        params.put("ids", Arrays.asList(3L, 5L));

        SqlResult sql = SQL.rawQuery(
                "select * from sqlux_test_user where status = :status and id in :ids",
                params
        ).toSql();

        assertTrue(sql.getSql().contains("status = ?"));
        assertTrue(sql.getSql().contains("id in (?, ?)"));
        assertEquals(Arrays.<Object>asList("ENABLED", 3L, 5L), sql.getParams());
    }

    // 验证原生 SQL 在接收到空集合 in 参数时，会自动改写为合法但查不出数据的条件，避免生成 in() 语法错误。
    @Test
    void rawQueryRewritesEmptyCollectionToSafeNoResultCondition() {
        SqlResult sql = SQL.rawQuery(
                "select * from sqlux_test_user where id in :ids",
                Collections.<String, Object>singletonMap("ids", Collections.emptyList())
        ).toSql();

        assertTrue(sql.getSql().contains("id in (NULL)"));
        assertEquals(Collections.emptyList(), sql.getParams());
    }

    // 验证原生 SQL 的 not in 空集合会直接报错，避免生成语义不安全的条件。
    @Test
    void rawQueryNotInEmptyCollectionThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> SQL.rawQuery(
                "select * from sqlux_test_user where id not in :ids",
                Collections.<String, Object>singletonMap("ids", Collections.emptyList())
        ).toSql());
    }

    // 验证 DSL 的 in 空集合会自动翻译为 in (NULL)，既保证 SQL 合法，也不会查出任何数据。
    @Test
    void dslInEmptyCollectionRewritesToInNull() {
        SqlResult sql = SQL.select()
                .from(SqluxTestUser.class)
                .where(w -> w.in(SqluxTestUser::getId, Collections.emptyList()))
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("\"id\" IN (NULL)"));
        assertEquals(Collections.emptyList(), sql.getParams());
    }

    // 验证 DSL 的 notIn 空集合会直接报错，避免生成语义不安全的条件。
    @Test
    void dslNotInEmptyCollectionThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> SQL.select()
                .from(SqluxTestUser.class)
                .where(w -> w.notIn(SqluxTestUser::getId, Collections.emptyList()))
                .toSql(DbType.POSTGRESQL));
    }

    // 验证继承自父类的 getter 方法引用也能绑定到当前实体别名，避免联表时生成未限定列名。
    @Test
    void inheritedGetterUsesCurrentEntityAliasInSelectAndJoin() {
        SqlResult sql = SQL.select(alias(SqluxTestUser::getId).as("user_id"))
                .select(SqluxTestDept::getDeptName)
                .select(SqluxTestUser::getRemark)
                .from(SqluxTestUser.class)
                .leftJoin(SqluxTestDept.class, on -> on.eq(SqluxTestUser::getDeptId, SqluxTestDept::getId))
                .where(w -> w.eq(SqluxTestUser::getId, "42"))
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("tu1.\"id\" AS user_id"));
        assertTrue(sql.getSql().contains("tu1.\"remark\""));
        assertTrue(sql.getSql().contains("tu1.\"dept_id\" = std2.\"id\""));
        assertTrue(sql.getSql().contains("WHERE"));
        assertEquals(Collections.<Object>singletonList("42"), sql.getParams());
    }

    // 验证字符串形式的 FROM 会直接保留原始表片段，不再做实体表名解析或标识符转义。
    @Test
    void rawFromSourceIsRenderedAsPassedToken() {
        SqlResult sql = SQL.selectRaw("count(*)")
                .from("ytora.sys_user")
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("FROM ytora.sys_user"));
        assertEquals(Collections.emptyList(), sql.getParams());
    }

    // 验证字符串形式的 FROM 支持单独传入别名，并在 SQL 中直接追加该别名。
    @Test
    void rawFromSourceSupportsExplicitAlias() {
        SqlResult sql = SQL.selectRaw("u.id")
                .from("ytora.sys_user", "u")
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("FROM ytora.sys_user u"));
        assertEquals(Collections.emptyList(), sql.getParams());
    }
}
