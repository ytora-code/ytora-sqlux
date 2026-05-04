package xyz.ytora.sqlux.core;

import java.sql.Connection;

/**
 * 数据库连接提供组件
 *
 * @author ytora
 * @since 1.0
 */
public interface IConnectionProvider {

    /**
     * 获取数据库连接
     * @return 数据库连接对象
     */
    Connection getConnection();

    /**
     * 关闭指定的数据库连接
     * @param connection 数据库连接对象
     */
    void closeConnection(Connection connection);

}
