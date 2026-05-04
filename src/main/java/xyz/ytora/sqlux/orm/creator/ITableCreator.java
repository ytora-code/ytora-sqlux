package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.AbsEntity;

import java.sql.Connection;

/**
 * 数据库表的创建器
 *
 * <p>业务代码中，所有继承{@link AbsEntity}的子类都认为是实体类，会被 Sqlux 感知到并管理</p>
 * <p>对于这些被 Sqlux 管理的实体类，Sqlux判断对应的表是否存在，如果不存在，{@link ITableCreator}会自动创建表</p>
 * <p>不同的数据库产品，对应了不同的ITableCreator实现类</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface ITableCreator {

    /**
     * 返回当前 TableCreator 所支持的数据库类型
     * @return 数据库类型 DbType
     */
    DbType getDbType();

    /**
     * 判断指定的实体类在数据库中是否存在
     * @param connection 连接对象
     * @param entityClazz 实体类型
     * @return true：该表已经存在；false：该表不存在
     */
    boolean exist(Connection connection, Class<?> entityClazz);

    /**
     * 根据实体类产生建表 DDL
     * @param connection 连接对象
     * @param clazz 实体类型
     * @return 对应的建表SQL
     */
    String toDDL(Connection connection, Class<?> clazz);

}
