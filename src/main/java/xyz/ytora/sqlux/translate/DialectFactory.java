package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;

/**
 * 方言工厂。
 *
 * <p>该类把 {@link DbType} 映射为具体 SQL 方言实现。SQL 翻译器不直接判断数据库类型，
 * 而是通过方言对象处理标识符引用、占位符和部分语法能力差异。</p>
 *
 * <p>使用示例：{@code Dialect dialect = DialectFactory.getDialect(DbType.POSTGRESQL)}。
 * 输入说明：传入数据库类型。输出说明：返回可复用的无状态方言对象。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class DialectFactory {

    private static final Dialect MYSQL = new MysqlDialect();

    private static final Dialect POSTGRESQL = new PostgreSqlDialect();

    private static final Dialect ORACLE = new OracleDialect();

    private static final Dialect DM = new DmDialect();

    private static final Dialect SQLSERVER = new SqlServerDialect();

    private static final Dialect DEFAULT = MYSQL;

    /**
     * 方言工厂不允许实例化。
     */
    private DialectFactory() {
    }

    /**
     * 获取指定数据库类型对应的 SQL 方言。
     *
     * <p>示例：{@code getDialect(DbType.MYSQL)} 返回 MySQL 方言，
     * {@code getDialect(DbType.POSTGRESQL)} 返回 PostgreSQL 方言。</p>
     *
     * @param dbType 数据库类型；入参为 {@code null} 或暂未专门支持的类型时返回默认 MySQL 方言
     * @return SQL方言；出参不会为 {@code null}
     */
    public static Dialect getDialect(DbType dbType) {
        if (dbType == DbType.POSTGRESQL) {
            return POSTGRESQL;
        }
        if (dbType == DbType.ORACLE) {
            return ORACLE;
        }
        if (dbType == DbType.DM) {
            return DM;
        }
        if (dbType == DbType.SQLSERVER) {
            return SQLSERVER;
        }
        if (dbType == DbType.MYSQL || dbType == DbType.MARIADB) {
            return MYSQL;
        }
        return DEFAULT;
    }
}
