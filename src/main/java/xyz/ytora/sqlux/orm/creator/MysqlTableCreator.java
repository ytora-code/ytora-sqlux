package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.creator.model.EntityColumnMeta;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.MysqlDialect;

/**
 * MySQL 表创建器。
 *
 * @author ytora
 * @since 1.0
 */
public class MysqlTableCreator extends AbstractTableCreator {

    public MysqlTableCreator() {
        super(new MysqlDialect());
    }

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    protected void appendColumnOptions(StringBuilder sql, EntityColumnMeta column) {
        if (hasText(column.getComment())) {
            sql.append(" COMMENT '").append(escapeSqlLiteral(column.getComment())).append("'");
        }
    }

    @Override
    protected void appendTableOptions(StringBuilder sql, EntityTableMeta table) {
        if (hasText(table.getComment())) {
            sql.append(" COMMENT='").append(escapeSqlLiteral(table.getComment())).append("'");
        }
    }
}
