package xyz.ytora.sqlux.orm.creator;

import xyz.ytora.sqlux.orm.creator.model.EntityColumnMeta;
import xyz.ytora.sqlux.orm.creator.model.EntityTableMeta;
import xyz.ytora.sqlux.translate.Dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 通用表创建器骨架。
 *
 * @author ytora
 * @since 1.0
 */
public abstract class AbstractTableCreator implements ITableCreator {

    private final Dialect dialect;

    protected AbstractTableCreator(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public boolean exist(Connection connection, Class<?> entityClazz) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection不能为空");
        }
        EntityTableMeta table = parse(entityClazz);
        try {
            return exists(connection, table.getTableName());
        } catch (SQLException e) {
            throw new IllegalStateException("判断表是否存在失败: " + table.getTableName(), e);
        }
    }

    @Override
    public String toDDL(Connection connection, Class<?> clazz) {
        return createTableSql(parse(clazz));
    }

    protected EntityTableMeta parse(Class<?> entityClass) {
        return EntityTableParser.parse(entityClass, dialect.typeMapper());
    }

    protected Dialect dialect() {
        return dialect;
    }

    /**
     * 产生通用的建表SQL
     * @param table 表
     * @return 建表SQL
     */
    protected String createTableSql(EntityTableMeta table) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(quote(table.getTableName())).append(" (\n");
        List<EntityColumnMeta> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            EntityColumnMeta column = columns.get(i);
            sql.append("    ").append(columnSql(column));
            if (i < columns.size() - 1 || !table.getKeyColumns().isEmpty()) {
                sql.append(",");
            }
            sql.append("\n");
        }
        if (!table.getKeyColumns().isEmpty()) {
            sql.append("    PRIMARY KEY (").append(quoteNames(table.getKeyColumns())).append(")\n");
        }
        sql.append(")");
        appendTableOptions(sql, table);
        return sql.toString();
    }

    /**
     * 通用的字段注释生成器
     * @param sql SQL
     * @param table 表
     */
    protected void appendCommentOnStatements(StringBuilder sql, EntityTableMeta table) {
        if (hasText(table.getComment())) {
            sql.append(";\nCOMMENT ON TABLE ")
                    .append(quote(table.getTableName()))
                    .append(" IS '")
                    .append(escapeSqlLiteral(table.getComment()))
                    .append("'");
        }
        for (EntityColumnMeta column : table.getColumns()) {
            if (hasText(column.getComment())) {
                sql.append(";\nCOMMENT ON COLUMN ")
                        .append(quote(table.getTableName()))
                        .append(".")
                        .append(quote(column.getColumnName()))
                        .append(" IS '")
                        .append(escapeSqlLiteral(column.getComment()))
                        .append("'");
            }
        }
    }

    protected String columnSql(EntityColumnMeta column) {
        StringBuilder sql = new StringBuilder();
        sql.append(quote(column.getColumnName())).append(" ").append(column.getSqlType());
        if (column.isNotNull()) {
            sql.append(" NOT NULL");
        }
        appendColumnOptions(sql, column);
        return sql.toString();
    }

    protected void appendColumnOptions(StringBuilder sql, EntityColumnMeta column) {
    }

    protected void appendTableOptions(StringBuilder sql, EntityTableMeta table) {
    }

    protected boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    protected String quote(String identifier) {
        return dialect.quoteIdentifier(identifier);
    }

    private String quoteNames(List<String> names) {
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(quote(names.get(i)));
        }
        return sql.toString();
    }

    private boolean exists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String schema = connection.getSchema();
        if (exists(metaData, connection.getCatalog(), schema, tableName)) {
            return true;
        }
        if (exists(metaData, connection.getCatalog(), schema, tableName.toUpperCase())) {
            return true;
        }
        return exists(metaData, connection.getCatalog(), schema, tableName.toLowerCase());
    }

    private boolean exists(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {
        try (ResultSet tables = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }
}
