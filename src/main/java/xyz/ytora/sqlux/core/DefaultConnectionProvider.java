package xyz.ytora.sqlux.core;

import java.sql.Connection;

/**
 * 默认的数据库连接提供器
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class DefaultConnectionProvider implements IConnectionProvider {
    @Override
    public Connection getConnection() {
        throw new UnsupportedOperationException("无法获取数据库连接...");
    }

    @Override
    public void closeConnection(Connection connection) {
        throw new UnsupportedOperationException("无法关闭数据库连接...");
    }
}
