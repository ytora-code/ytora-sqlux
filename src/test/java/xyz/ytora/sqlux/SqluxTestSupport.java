package xyz.ytora.sqlux;

import xyz.ytora.sqlux.core.SqluxGlobal;
import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.sqlux.core.json.SqluxJson;
import xyz.ytora.sqlux.interceptor.log.SqlLogger;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.EntityMetas;
import xyz.ytora.sqlux.orm.creator.TableCreators;
import xyz.ytora.sqlux.orm.filler.IFiller;
import xyz.ytora.sqlux.orm.filler.OrmFieldFiller;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Uuid;
import xyz.ytora.sqlux.orm.type.Version;
import xyz.ytora.sqlux.rw.TypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

public final class SqluxTestSupport {

    static final LocalDateTime INSERT_TIME = LocalDateTime.of(2024, 1, 2, 3, 4, 5);

    static final LocalDateTime UPDATE_TIME = LocalDateTime.of(2024, 1, 3, 4, 5, 6);

    private static final TestConnectionProvider PROVIDER = new TestConnectionProvider();

    private SqluxTestSupport() {
    }

    static SqluxGlobal resetGlobal() {
        EntityMetas.clear();
        OrmFieldFiller.clearCache();
        SqluxGlobal.clearTypeHandlers();
        SqluxGlobal global = new SqluxGlobal();
        global.setDefaultDbType(DbType.POSTGRESQL);
        global.registerConnectionProvider(PROVIDER);
        global.registerSqluxJson(new TestSqluxJson());
        global.clearInterceptors();
        global.registerSqlLogger(null);
        ensureSchema();
        return global;
    }

    static void registerCommonTypeHandlers() {
        SqluxGlobal.registerTypeHandler(new SqluxTestStatusHandler());
    }

    static void recreateManagedTables() {
        resetGlobal();
        registerCommonTypeHandlers();
        dropObjects(
                "DROP VIEW IF EXISTS test.sqlux_test_user_view",
                "DROP TABLE IF EXISTS test.sqlux_test_user CASCADE",
                "DROP TABLE IF EXISTS test.sqlux_test_dept CASCADE"
        );
        try (Connection connection = PROVIDER.getConnection()) {
            TableCreators.createMissingTables(connection, DbType.POSTGRESQL,
                    Arrays.asList(SqluxTestDept.class, SqluxTestUser.class));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to recreate managed test tables", e);
        }
    }

    static void recreateGeneratedKeyTable() {
        resetGlobal();
        registerCommonTypeHandlers();
        dropObjects("DROP TABLE IF EXISTS test.sqlux_test_generated_user CASCADE");
        execute(
                "CREATE TABLE test.sqlux_test_generated_user (" +
                        "id bigserial PRIMARY KEY, " +
                        "user_name varchar(64) NOT NULL" +
                        ")"
        );
    }

    static void ensureSchema() {
        execute("CREATE SCHEMA IF NOT EXISTS test");
    }

    static void execute(String sql) {
        try (Connection connection = PROVIDER.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute SQL: " + sql, e);
        }
    }

    static void dropObjects(String... sqlList) {
        for (String sql : sqlList) {
            execute(sql);
        }
    }

    static TestConnectionProvider provider() {
        return PROVIDER;
    }
}

enum SqluxTestStatus {
    ENABLED("E"),
    DISABLED("D");

    private final String code;

    SqluxTestStatus(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

    static SqluxTestStatus fromCode(String code) {
        for (SqluxTestStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

final class SqluxTestStatusHandler implements TypeHandler<SqluxTestStatus> {

    @Override
    public boolean supports(Class<?> type) {
        return type == SqluxTestStatus.class;
    }

    @Override
    public Object write(SqluxTestStatus value, Field field) {
        return value == null ? null : value.getCode();
    }

    @Override
    public SqluxTestStatus read(Object value, Field field) {
        return value == null ? null : SqluxTestStatus.fromCode(String.valueOf(value));
    }
}

final class FixedTimeFiller implements IFiller {

    @Override
    public Object onInsert() {
        return SqluxTestSupport.INSERT_TIME;
    }

    @Override
    public Object onUpdate() {
        return SqluxTestSupport.UPDATE_TIME;
    }
}

final class TestSqluxJson implements SqluxJson {

    @Override
    public Object parse(String json) {
        if (json == null) {
            return null;
        }
        String text = json.trim();
        if (text.isEmpty() || "null".equals(text)) {
            return null;
        }
        if (text.startsWith("{") && text.endsWith("}")) {
            return parseObject(text.substring(1, text.length() - 1));
        }
        if (text.startsWith("[") && text.endsWith("]")) {
            return parseArray(text.substring(1, text.length() - 1));
        }
        return unquote(text);
    }

    @Override
    public Object parse(String json, Type type) {
        return parse(json);
    }

    @Override
    public String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?>) {
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                builder.append(stringify(entry.getValue()));
                first = false;
            }
            return builder.append("}").toString();
        }
        if (value instanceof Iterable<?>) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) {
                    builder.append(",");
                }
                builder.append(stringify(item));
                first = false;
            }
            return builder.append("]").toString();
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private Map<String, Object> parseObject(String body) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (body.trim().isEmpty()) {
            return result;
        }
        for (String entry : splitTopLevel(body)) {
            int index = entry.indexOf(':');
            String key = unquote(entry.substring(0, index).trim());
            String value = entry.substring(index + 1).trim();
            result.put(key, parse(value));
        }
        return result;
    }

    private List<Object> parseArray(String body) {
        List<Object> result = new ArrayList<>();
        if (body.trim().isEmpty()) {
            return result;
        }
        for (String item : splitTopLevel(body)) {
            result.add(parse(item.trim()));
        }
        return result;
    }

    private List<String> splitTopLevel(String text) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString) {
                if (ch == '{' || ch == '[') {
                    depth++;
                } else if (ch == '}' || ch == ']') {
                    depth--;
                } else if (ch == ',' && depth == 0) {
                    result.add(current.toString());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(ch);
        }
        result.add(current.toString());
        return result;
    }

    private String unquote(String text) {
        String value = text.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

@Table(value = "sqlux_test_dept", idType = IdType.SNOWFLAKE)
class SqluxTestDept extends AbsEntity {

    @Column(type = ColumnType.VARCHAR64, notNull = true)
    private String deptName;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}

@Table(value = "sqlux_test_user", idType = IdType.SNOWFLAKE)
class SqluxTestUser extends AbsEntity {

    @Column(value = "login_name", type = ColumnType.VARCHAR64, notNull = true, index = 1)
    private String loginName;

    @Column(type = ColumnType.INT4)
    private Integer age;

    @Column(type = ColumnType.INT8)
    private String deptId;

    @Column(type = ColumnType.VARCHAR16)
    private SqluxTestStatus status;

    @Column(type = ColumnType.DECIMAL_19_4)
    private BigDecimal salary;

    @Column(type = ColumnType.JSON)
    private Json profile;

    @Column(type = ColumnType.UUID)
    private Uuid externalId;

    @Column(type = ColumnType.TEXT)
    private String remark;

    @Column(type = ColumnType.DATETIME, fillOn = FillType.INSERT, filler = FixedTimeFiller.class)
    private LocalDateTime createdAt;

    @Column(type = ColumnType.DATETIME, fillOn = FillType.INSERT_UPDATE, filler = FixedTimeFiller.class)
    private LocalDateTime updatedAt;

    @Column
    private Version version;

    @Column
    private LogicDelete deleted;

    @Column(exist = false)
    private String transientNote;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public SqluxTestStatus getStatus() {
        return status;
    }

    public void setStatus(SqluxTestStatus status) {
        this.status = status;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Json getProfile() {
        return profile;
    }

    public void setProfile(Json profile) {
        this.profile = profile;
    }

    public Uuid getExternalId() {
        return externalId;
    }

    public void setExternalId(Uuid externalId) {
        this.externalId = externalId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

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

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public LogicDelete getDeleted() {
        return deleted;
    }

    public void setDeleted(LogicDelete deleted) {
        this.deleted = deleted;
    }

    public String getTransientNote() {
        return transientNote;
    }

    public void setTransientNote(String transientNote) {
        this.transientNote = transientNote;
    }
}

@Table(value = "sqlux_test_generated_user")
class SqluxGeneratedKeyUser extends AbsEntity {

    @Column(value = "user_name", type = ColumnType.VARCHAR64, notNull = true)
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
