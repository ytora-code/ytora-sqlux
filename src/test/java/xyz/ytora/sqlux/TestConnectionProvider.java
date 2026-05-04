package xyz.ytora.sqlux;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.ytora.sqlux.core.IConnectionProvider;
import xyz.ytora.sqlux.core.IDbTypeProvider;
import xyz.ytora.sqlux.core.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 描述
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class TestConnectionProvider implements IConnectionProvider, IDbTypeProvider, AutoCloseable {

    public static final String URL = "jdbc:postgresql://localhost:5432/ytora?currentSchema=test&ssl=false&stringtype=unspecified";

    public static final String USERNAME = "admin";

    public static final String PASSWORD = "220600";

    public static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private final HikariDataSource dataSource;

    public TestConnectionProvider() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setDriverClassName(DRIVER_CLASS_NAME);
        config.setPoolName("sqlux-test-pool");
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setAutoCommit(true);
        config.setConnectionTimeout(5000L);
        config.setValidationTimeout(3000L);
        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("获取测试数据库连接失败", e);
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("关闭测试数据库连接失败", e);
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.POSTGRESQL;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
