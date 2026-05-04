package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.translate.SqlServerDialect;

/**
 * SQL Server 表创建器。
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerTableCreator extends AbstractTableCreator {

    public SqlServerTableCreator() {
        super(new SqlServerDialect());
    }

    @Override
    public DbType getDbType() {
        return DbType.SQLSERVER;
    }
}
