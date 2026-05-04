package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.OracleDialect;

/**
 * Oracle 表创建器。
 *
 * @author ytora
 * @since 1.0
 */
public class OracleTableCreator extends AbstractTableCreator {

    public OracleTableCreator() {
        super(new OracleDialect());
    }

    @Override
    public DbType getDbType() {
        return DbType.ORACLE;
    }

    @Override
    protected String createTableSql(EntityTableMeta table) {
        StringBuilder sql = new StringBuilder(super.createTableSql(table));
        appendCommentOnStatements(sql, table);
        return sql.toString();
    }
}
