package xyz.ytora.sqlux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.query.SqluxQueryBuilder;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqluxQueryBuilderTest {

    @BeforeEach
    void setUp() {
        SqluxTestSupport.resetGlobal();
        SqluxTestSupport.registerCommonTypeHandlers();
    }

    // 验证参数式查询构建器可以把 distinct、查询列、条件、分组和排序参数翻译成完整 SQL。
    @Test
    void queryBuilderParsesSelectDistinctWhereGroupAndOrder() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("sql_distinct_enable", "true");
        params.put("sql_query_col", "loginName, age");
        params.put("age_ge", "18");
        params.put("loginName_like", "%a%");
        params.put("sql_group_col", "age");
        params.put("sql_order_col", "age:desc, loginName:asc");

        SqlResult sql = new SqluxQueryBuilder<>(SqluxTestUser.class)
                .params(params)
                .build()
                .toSql(DbType.POSTGRESQL);

        assertTrue(sql.getSql().contains("SELECT DISTINCT"));
        assertTrue(sql.getSql().contains("\"login_name\""));
        assertTrue(sql.getSql().contains("GROUP BY"));
        assertTrue(sql.getSql().contains("ORDER BY"));
        assertEquals(Arrays.<Object>asList(18, "%%a%%"), sql.getParams());
    }

    // 验证参数式查询构建器可以把 in 和 between 这种多值条件转换为正确的 SQL 与参数列表。
    @Test
    void queryBuilderParsesInAndBetweenValues() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("age_in", Arrays.asList("18", "20", "22"));
        params.put("createdAt_between", Arrays.asList("2024-01-01T00:00:00", "2024-01-31T00:00:00"));

        SqlResult sql = new SqluxQueryBuilder<>(BuilderEntity.class)
                .params(params)
                .build()
                .toSql(DbType.POSTGRESQL);

        String lowerSql = sql.getSql().toLowerCase();
        assertTrue(lowerSql.contains(" in "));
        assertTrue(lowerSql.contains(" between "));
        assertEquals(5, sql.getParams().size());
        assertEquals(18, sql.getParams().get(0));
        assertEquals(22, sql.getParams().get(2));
    }

    @Table("sqlux_test_builder")
    static class BuilderEntity extends xyz.ytora.sqlux.orm.AbsEntity {

        private Integer age;

        private java.time.LocalDateTime createdAt;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
