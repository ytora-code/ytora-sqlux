package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.interceptor.log.SqlLogEvent;
import xyz.ytora.sqlux.interceptor.log.SqlLogger;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlRewriteContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalAndInterceptorTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.recreateManagedTables();
    }

    // 验证 beforeTranslate 拦截器可以在 SQL 翻译前动态追加查询条件，从而改写最终查询结果。
    @Test
    void beforeTranslateInterceptorCanAppendSelectFilter() {
        SqluxTestDept dept = new SqluxTestDept();
        dept.setDeptName("it");
        SQL.insert(SqluxTestDept.class).into().values(dept).submit();

        SqluxTestUser alice = new SqluxTestUser();
        alice.setLoginName("alice");
        alice.setDeptId(dept.getId());
        alice.setVersion(xyz.ytora.sqlux.orm.type.Version.initial());
        alice.setDeleted(xyz.ytora.sqlux.orm.type.LogicDelete.normal());

        SqluxTestUser bob = new SqluxTestUser();
        bob.setLoginName("bob");
        bob.setDeptId(dept.getId());
        bob.setVersion(xyz.ytora.sqlux.orm.type.Version.initial());
        bob.setDeleted(xyz.ytora.sqlux.orm.type.LogicDelete.normal());

        SQL.insert(SqluxTestUser.class).into().values(java.util.Arrays.asList(alice, bob)).submitBatch();

        SQL.getSqluxGlobal().registerInterceptor(new Interceptor() {
            @Override
            public void beforeTranslate(SqlRewriteContext context) {
                if (context.getSqlType() == xyz.ytora.sqlux.core.enums.SqlType.SELECT) {
                    context.andWhere(where -> where.eq(SqluxTestUser::getLoginName, "alice"));
                }
            }
        });

        List<SqluxTestUser> result = SQL.select().from(SqluxTestUser.class).submit(SqluxTestUser.class);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getLoginName());
    }

    // 验证 SQL 日志器能够同时收到执行前、执行成功和执行失败三种事件回调。
    @Test
    void sqlLoggerReceivesSuccessAndFailureEvents() {
        final List<String> events = new ArrayList<>();
        SQL.getSqluxGlobal().registerSqlLogger(new SqlLogger() {
            @Override
            public void beforeExecute(SqlLogEvent event) {
                events.add("before:" + event.getSqlType());
            }

            @Override
            public void afterSuccess(SqlLogEvent event) {
                events.add("success:" + event.getSqlType());
            }

            @Override
            public void afterFailure(SqlLogEvent event) {
                events.add("failure:" + event.getSqlType());
            }
        });

        SQL.rawQuery("select 1 as value").submit();
        assertThrows(IllegalStateException.class, () -> SQL.rawQuery("select * from test.not_exists").submit());

        assertTrue(events.stream().anyMatch(item -> item.startsWith("before:SELECT")));
        assertTrue(events.stream().anyMatch(item -> item.startsWith("success:SELECT")));
        assertTrue(events.stream().anyMatch(item -> item.startsWith("failure:SELECT")));
    }

    // 验证框架内置的安全保护会阻止没有 where 条件的更新操作，避免误更新整张表。
    @Test
    void safeMutationInterceptorBlocksUpdateWithoutWhere() {
        assertThrows(IllegalStateException.class, () -> SQL.update(SqluxTestUser.class)
                .set(SqluxTestUser::getAge, 18)
                .submit());
    }

    // 验证安全拦截器会拦截 where 1 = 1 这类显而易见的永真更新条件，避免借助原生条件绕过保护。
    @Test
    void safeMutationInterceptorBlocksUpdateWithAlwaysTrueRawWhere() {
        assertThrows(IllegalStateException.class, () -> SQL.update(SqluxTestUser.class)
                .set(SqluxTestUser::getAge, 18)
                .where(w -> w.raw("1 = 1"))
                .submit());
    }

    // 验证安全拦截器会拦截字段与自身比较这种显而易见的永真删除条件，避免结构化 DSL 绕过保护。
    @Test
    void safeMutationInterceptorBlocksDeleteWithAlwaysTrueDslWhere() {
        assertThrows(IllegalStateException.class, () -> SQL.delete()
                .from(SqluxTestUser.class)
                .where(w -> w.eq(SqluxTestUser::getId, SqluxTestUser::getId))
                .submit());
    }
}
