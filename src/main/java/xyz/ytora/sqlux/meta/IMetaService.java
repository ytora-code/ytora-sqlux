package xyz.ytora.sqlux.meta;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.meta.model.*;

import java.sql.Connection;
import java.util.List;

/**
 * 数据库元数据服务
 *
 * <p>提供获取数据库元数据的相关API</p>
 *
 * @author ytora 
 * @since 1.0
 */
public interface IMetaService {

    /**
     * 推断数据库类型
     * @return 数据库类型枚举{@code DbType}
     */
    DbType inferDbType();

    /**
     * 从指定数据库连接中推断数据库类型
     * @param connection 数据库连接
     * @return 数据库类型枚举{@code DbType}
     */
    DbType inferDbType(Connection connection);

    /**
     * 获取所有数据库名称，MySQL中 catalog 和 schema 是等价的
     */
    List<String> listCatalogs();

    /**
     * 获取指定库下的所有模式名称，MySQL调用该方法获取的是空数组
     */
    List<String> listSchemas(String catalog);

    /**
     * 获取指定库的指定模式下面表元数据
     */
    List<TableMeta> listTables(String catalog, String schema, String table);

    /**
     * 获取当前连接可见的所有表元数据
     */
    default List<TableMeta> listTables() {
        return listTables(null, null, null);
    }

    /**
     * 获取当前连接可见的指定表元数据
     */
    default TableMeta getTable(String tableName) {
        List<TableMeta> tables = listTables(null, null, tableName);
        return tables.isEmpty() ? null : tables.get(0);
    }

    /**
     * 判断当前连接可见范围内是否存在指定表
     */
    default boolean tableExists(String tableName) {
        return getTable(tableName) != null;
    }

    /**
     * 获取指定库的指定模式下面视图元数据
     */
    List<ViewMeta> listViews(String catalog, String schema, String table);

    /**
     * 获取当前连接可见的所有视图元数据
     */
    default List<ViewMeta> listViews() {
        return listViews(null, null, null);
    }

    /**
     * 获取指定库的指定模式下面函数元数据
     */
    List<FunctionMeta> listFunctions(String catalog, String schema, String function);

    /**
     * 获取指定库的指定模式下面存储过程元数据
     */
    List<ProcedureMeta> listProcedures(String catalog, String schema, String procedure);

    /**
     * 获取指定库的指定模式下面序列元数据
     */
    List<SequenceMeta> listSequences(String catalog, String schema, String sequence);

    /**
     * 获取指定表的所有列消息
     */
    List<ColumnMeta> listColumns(String catalog, String schema, String tableName);

    /**
     * 获取当前连接可见范围内指定表的所有列信息
     */
    default List<ColumnMeta> listColumns(String tableName) {
        return listColumns(null, null, tableName);
    }

    /**
     * 获取指定表的所有主键
     */
    List<String> listPrimaryKeys(String catalog, String schema, String tableName);

    /**
     * 获取当前连接可见范围内指定表的所有主键
     */
    default List<String> listPrimaryKeys(String tableName) {
        return listPrimaryKeys(null, null, tableName);
    }

    /**
     * 获取指定表的所有外键
     */
    List<ForeignKeyMeta> listForeignKeys(String catalog, String schema, String tableName);

    /**
     * 获取当前连接可见范围内指定表的所有外键
     */
    default List<ForeignKeyMeta> listForeignKeys(String tableName) {
        return listForeignKeys(null, null, tableName);
    }

    /**
     * 获取指定表的所有索引
     */
    List<IndexMeta> listIndexes(String catalog, String schema, String tableName);

    /**
     * 获取当前连接可见范围内指定表的所有索引
     */
    default List<IndexMeta> listIndexes(String tableName) {
        return listIndexes(null, null, tableName);
    }

}
