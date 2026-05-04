package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.DmDialect;

/**
 * 达梦表创建器。
 *
 * @author ytora
 * @since 1.0
 */
public class DmTableCreator extends AbstractTableCreator {

    public DmTableCreator() {
        super(new DmDialect());
    }

    @Override
    public DbType getDbType() {
        return DbType.DM;
    }

    @Override
    protected String createTableSql(EntityTableMeta table) {
        StringBuilder sql = new StringBuilder(super.createTableSql(table));
        appendCommentOnStatements(sql, table);
        return sql.toString();
    }
}
