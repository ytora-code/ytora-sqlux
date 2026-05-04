package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.util.NamedUtil;
import xyz.ytora.toolkit.text.Printers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 自动建表入口。
 *
 * <p>调用方在应用启动后主动调用该类，Sqlux 会扫描配置路径下的实体类，并为缺失表执行建表 DDL。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class TableCreators {

    private static final Map<DbType, ITableCreator> CREATORS = new EnumMap<>(DbType.class);

    static {
        register(new MysqlTableCreator());
        register(DbType.MARIADB, new MysqlTableCreator());
        register(new PostgreSqlTableCreator());
        register(new OracleTableCreator());
        register(new DmTableCreator());
        register(new SqlServerTableCreator());
    }

    private TableCreators() {
    }

    /**
     * 注册表创建器。
     *
     * @param creator 表创建器；入参为 {@code null} 时忽略
     */
    public static void register(ITableCreator creator) {
        if (creator != null) {
            CREATORS.put(creator.getDbType(), creator);
        }
    }

    /**
     * 为指定数据库类型注册表创建器。
     *
     * @param dbType 数据库类型；入参为空时忽略
     * @param creator 表创建器；入参为空时忽略
     */
    public static void register(DbType dbType, ITableCreator creator) {
        if (dbType != null && creator != null) {
            CREATORS.put(dbType, creator);
        }
    }

    /**
     * 使用全局实体路径和当前执行器数据库类型执行自动建表。
     *
     * @param connection 数据库连接；调用方负责管理连接生命周期
     * @return 每个扫描实体的建表结果
     */
    public static List<TableCreateResult> createMissingTables(Connection connection) {
        return createMissingTables(connection, SQL.getSqluxGlobal().getDbType(), SQL.getSqluxGlobal().getEntityPath());
    }

    /**
     * 使用指定实体路径和当前执行器数据库类型执行自动建表。
     *
     * @param connection 数据库连接；调用方负责管理连接生命周期
     * @param entityPath 实体扫描路径
     * @return 每个扫描实体的建表结果
     */
    public static List<TableCreateResult> createMissingTables(Connection connection, String entityPath) {
        return createMissingTables(connection, SQL.getSqluxGlobal().getDbType(), entityPath);
    }

    /**
     * 使用指定数据库类型和实体路径执行自动建表。
     *
     * @param connection 数据库连接；调用方负责管理连接生命周期
     * @param dbType 数据库类型
     * @param entityPath 实体扫描路径
     * @return 每个扫描实体的建表结果
     */
    public static List<TableCreateResult> createMissingTables(Connection connection, DbType dbType, String entityPath) {
        List<Class<? extends AbsEntity>> entityClasses = EntityScanner.scan(entityPath);
        return createMissingTables(connection, dbType, entityClasses);
    }

    /**
     * 为指定实体列表执行自动建表。
     *
     * @param connection 数据库连接；调用方负责管理连接生命周期
     * @param dbType 数据库类型
     * @param entityClasses 实体类型列表
     * @return 每个实体的建表结果
     */
    public static List<TableCreateResult> createMissingTables(Connection connection, DbType dbType,
                                                              List<Class<? extends AbsEntity>> entityClasses) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection不能为空");
        }
        if (entityClasses == null || entityClasses.isEmpty()) {
            return Collections.emptyList();
        }
        ITableCreator creator = getCreator(dbType);
        List<TableCreateResult> results = new ArrayList<>();
        for (Class<? extends AbsEntity> entityClass : entityClasses) {
            String tableName = NamedUtil.parseTableName(entityClass);
            if (creator.exist(connection, entityClass)) {
                results.add(new TableCreateResult(entityClass, tableName, false, null));
                continue;
            }
            String ddl = creator.toDDL(connection, entityClass);
            System.out.println("解析实体类[" + entityClass.getName() + "]，并产生建表SQL...");
            Printers.print(ddl, Printers.PrintStyle.BOX_HEAVY);
            execute(connection, ddl);
            System.out.println("建表SQL已成功提交数据库!");
            results.add(new TableCreateResult(entityClass, tableName, true, ddl));
        }
        return results;
    }

    /**
     * 扫描全局配置路径下的实体类型。
     *
     * @return 实体类型列表
     */
    public static List<Class<? extends AbsEntity>> scanEntities() {
        return EntityScanner.scan(SQL.getSqluxGlobal().getEntityPath());
    }

    private static ITableCreator getCreator(DbType dbType) {
        ITableCreator creator = CREATORS.get(dbType == null ? SQL.getSqluxGlobal().getDefaultDbType() : dbType);
        if (creator == null) {
            throw new IllegalArgumentException("当前数据库暂不支持自动建表: " + dbType);
        }
        return creator;
    }

    private static void execute(Connection connection, String ddl) {
        try (Statement statement = connection.createStatement()) {
            for (String sql : splitStatements(ddl)) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("执行建表DDL失败: " + ddl, e);
        }
    }

    private static List<String> splitStatements(String ddl) {
        if (ddl == null || ddl.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < ddl.length(); i++) {
            char c = ddl.charAt(i);
            if (c == '\'') {
                current.append(c);
                if (inString && i + 1 < ddl.length() && ddl.charAt(i + 1) == '\'') {
                    current.append(ddl.charAt(++i));
                    continue;
                }
                inString = !inString;
                continue;
            }
            if (c == ';' && !inString) {
                addStatement(statements, current);
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        addStatement(statements, current);
        return statements;
    }

    private static void addStatement(List<String> statements, StringBuilder sql) {
        String statement = sql.toString().trim();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }
}
