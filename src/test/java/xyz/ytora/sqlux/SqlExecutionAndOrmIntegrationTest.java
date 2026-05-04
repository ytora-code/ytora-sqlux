package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.orm.Page;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Uuid;
import xyz.ytora.sqlux.orm.type.Version;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlExecutionAndOrmIntegrationTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.recreateManagedTables();
    }

    // 验证批量插入、ORM 查询映射、字段填充器和分页查询可以在真实数据库里串联工作。
    @Test
    void batchInsertQueryMappingAndPagingWorkEndToEnd() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("engineering");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser alice = newUser("alice", 18, dept.getId());
        SqluxTestUser bob = newUser("bob", 28, dept.getId());
        SqluxTestUser carol = newUser("carol", 38, dept.getId());

        int[] affected = SQL.insert(SqluxTestUser.class).into().values(Arrays.asList(alice, bob, carol)).submitBatch();
        assertEquals(3, affected.length);
        assertNotNull(alice.getId());
        assertEquals(SqluxTestSupport.INSERT_TIME, alice.getCreatedAt());
        assertEquals(SqluxTestSupport.INSERT_TIME, alice.getUpdatedAt());

        List<SqluxTestUser> rows = SQL.select()
                .from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getLoginName, "alice"))
                .submit(SqluxTestUser.class);

        assertEquals(1, rows.size());
        assertEquals(SqluxTestStatus.ENABLED, rows.get(0).getStatus());
        assertEquals("core", rows.get(0).getProfile().getString("team"));
        assertEquals("seed", rows.get(0).getRemark());

        Page<SqluxTestUser> page = SQL.select()
                .from(SqluxTestUser.class)
                .orderByAsc(SqluxTestUser::getAge)
                .submit(Page.of(1, 2));

        assertEquals(3L, page.getTotal().longValue());
        assertEquals(2, page.getRecords().size());
        assertEquals("alice", page.getRecords().get(0).getLoginName());
    }

    // 验证实体更新时会忽略 null 字段、遵守乐观锁版本控制，并支持显式把某个字段更新为空。
    @Test
    void updateEntityIgnoresNullFieldsHonorsVersionAndAllowsExplicitNullSet() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("ops");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser user = newUser("dave", 26, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(user).submit();

        SqluxTestUser patch = new SqluxTestUser();
        patch.setId(user.getId());
        patch.setLoginName("dave-v2");
        patch.setVersion(Version.of(1L));
        int affected = SQL.update(SqluxTestUser.class).set(patch).where(w -> w.eq(SqluxTestUser::getId, user.getId())).submit();
        assertEquals(1, affected);

        SqluxTestUser updated = SQL.select().from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit(SqluxTestUser.class, 0)
                .get();
        assertEquals("dave-v2", updated.getLoginName());
        assertEquals("seed", updated.getRemark());
        assertEquals(2L, updated.getVersion().getValue().longValue());

        SqluxTestUser stale = new SqluxTestUser();
        stale.setId(user.getId());
        stale.setLoginName("should-not-apply");
        stale.setVersion(Version.of(1L));
        assertEquals(0, SQL.update(SqluxTestUser.class).set(stale)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit().intValue());

        assertEquals(1, SQL.update(SqluxTestUser.class)
                .set(SqluxTestUser::getRemark, null)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit().intValue());

        SqluxTestUser cleared = SQL.select().from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit(SqluxTestUser.class, 0)
                .get();
        assertNull(cleared.getRemark());
    }

    // 验证带逻辑删除类型的实体执行 delete 后，只会更新删除标志，不会把数据从表中真正删掉。
    @Test
    void deleteUsesLogicDeleteFlagInsteadOfPhysicalDeletion() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("finance");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser user = newUser("erin", 31, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(user).submit();

        assertEquals(1, SQL.delete().from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit().intValue());

        SqluxTestUser deleted = SQL.select().from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, user.getId()))
                .submit(SqluxTestUser.class, 0)
                .get();
        assertNotNull(deleted.getDeleted());
        assertTrue(deleted.getDeleted().isDeleted());
    }

    // 验证原生 SQL 查询在真实数据库环境下也支持集合参数打平，并能正常完成结果映射。
    @Test
    void rawSqlSupportsFlattenedCollectionParameters() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("raw");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser alpha = newUser("alpha", 20, dept.getId());
        SqluxTestUser beta = newUser("beta", 21, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(Arrays.asList(alpha, beta)).submitBatch();

        List<SqluxTestUser> users = SQL.rawQuery(
                "select * from test.sqlux_test_user where id in (?, ?)",
                Arrays.asList(alpha.getId(), beta.getId())
        ).submit(SqluxTestUser.class);

        assertEquals(2, users.size());
    }

    // 验证原生 SQL 支持命名参数查询和更新，并且命名集合参数可以自动展开为 in 条件。
    @Test
    void rawSqlSupportsNamedParametersForQueryAndUpdate() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("named");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser alpha = newUser("named-alpha", 24, dept.getId());
        SqluxTestUser beta = newUser("named-beta", 25, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(Arrays.asList(alpha, beta)).submitBatch();

        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("ids", Arrays.asList(alpha.getId(), beta.getId()));
        List<SqluxTestUser> users = SQL.rawQuery(
                "select * from test.sqlux_test_user where id in :ids order by age asc",
                queryParams
        ).submit(SqluxTestUser.class);
        assertEquals(2, users.size());
        assertEquals("named-alpha", users.get(0).getLoginName());

        Map<String, Object> updateParams = new LinkedHashMap<>();
        updateParams.put("remark", "raw-named");
        updateParams.put("id", alpha.getId());
        assertEquals(1, SQL.rawUpdate(
                "update test.sqlux_test_user set remark = :remark where id = :id",
                updateParams
        ).submit().intValue());

        SqluxTestUser updated = SQL.select().from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, alpha.getId()))
                .submit(SqluxTestUser.class, 0)
                .get();
        assertEquals("raw-named", updated.getRemark());
    }

    // 验证原生 SQL 在执行 in 空集合查询时不会报 SQL 语法错，而是直接返回空结果集。
    @Test
    void rawSqlReturnsEmptyResultWhenInCollectionIsEmpty() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("empty-in");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser user = newUser("empty-in-user", 29, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(user).submit();

        List<SqluxTestUser> users = SQL.rawQuery(
                "select * from test.sqlux_test_user where id in :ids",
                Collections.<String, Object>singletonMap("ids", Collections.emptyList())
        ).submit(SqluxTestUser.class);

        assertTrue(users.isEmpty());
    }

    // 验证 DSL 查询在 in 空集合时不会报错，而是安全返回空结果。
    @Test
    void dslQueryReturnsEmptyResultWhenInCollectionIsEmpty() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("dsl-empty-in");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser user = newUser("dsl-empty-in-user", 30, dept.getId());
        SQL.insert(SqluxTestUser.class).into().values(user).submit();

        List<SqluxTestUser> users = SQL.select()
                .from(SqluxTestUser.class)
                .where(w -> w.in(SqluxTestUser::getId, Collections.emptyList()))
                .submit(SqluxTestUser.class);

        assertTrue(users.isEmpty());
    }

    // 验证 DSL 查询在 notIn 空集合时会直接抛错，避免执行语义不安全的全量匹配条件。
    @Test
    void dslNotInEmptyCollectionThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> SQL.select()
                .from(SqluxTestUser.class)
                .where(w -> w.notIn(SqluxTestUser::getId, Collections.emptyList()))
                .submit(SqluxTestUser.class));
    }

    // 验证当主键由数据库自增生成时，插入后框架能够把数据库返回的主键值回填到实体对象。
    @Test
    void insertBackfillsDatabaseGeneratedKeysWhenPrimaryKeyComesFromDatabase() {
        SqluxTestSupport.recreateGeneratedKeyTable();

        SqluxGeneratedKeyUser user = new SqluxGeneratedKeyUser();
        user.setUserName("pg-generated");

        assertEquals(1, SQL.insert(SqluxGeneratedKeyUser.class)
                .into(SqluxGeneratedKeyUser::getUserName)
                .values(user)
                .submit().intValue());
        assertNotNull(user.getId());
    }

    private SqluxTestUser newUser(String loginName, int age, String deptId) {
        SqluxTestUser user = new SqluxTestUser();
        user.setLoginName(loginName);
        user.setAge(age);
        user.setDeptId(deptId);
        user.setStatus(SqluxTestStatus.ENABLED);
        user.setSalary(new BigDecimal("123.45"));
        user.setProfile(Json.object());
        user.getProfile().put("team", "core");
        user.setExternalId(Uuid.of(java.util.UUID.randomUUID()));
        user.setRemark("seed");
        user.setVersion(Version.initial());
        user.setDeleted(LogicDelete.normal());
        return user;
    }
}
