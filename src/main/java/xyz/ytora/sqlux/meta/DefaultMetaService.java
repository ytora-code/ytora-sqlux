package xyz.ytora.sqlux.meta;

import xyz.ytora.sqlux.core.IConnectionProvider;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.meta.model.*;

import java.sql.*;
import java.util.*;

/**
 * 默认数据库元数据服务。
 */
public class DefaultMetaService implements IMetaService {

    private final IConnectionProvider connectionProvider;

    public DefaultMetaService() {
        this(SQL.getSqluxGlobal().getConnectionProvider());
    }

    public DefaultMetaService(IConnectionProvider connectionProvider) {
        if (connectionProvider == null) {
            throw new IllegalArgumentException("connectionProvider不能为空");
        }
        this.connectionProvider = connectionProvider;
    }

    @Override
    public DbType inferDbType() {
        Connection connection = getConnection();
        try {
            return inferDbType(connection);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public DbType inferDbType(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection不能为空");
        }
        try {
            return DbType.fromString(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listCatalogs() {
        Connection connection = getConnection();
        try {
            List<String> list = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getCatalogs()) {
                while (rs.next()) {
                    addIfPresent(list, safeGetString(rs, "TABLE_CAT"));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<String> listSchemas(String catalog) {
        Connection connection = getConnection();
        try {
            List<String> list = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getSchemas(catalog, null)) {
                while (rs.next()) {
                    addIfPresent(list, safeGetString(rs, "TABLE_SCHEM"));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<TableMeta> listTables(String catalog, String schema, String table) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            String tablePattern = pattern(table);
            List<TableMeta> list = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(effectiveCatalog, schema, tablePattern, new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = safeGetString(rs, "TABLE_NAME");
                    TableMeta tableMeta = new TableMeta();
                    tableMeta.setName(tableName);
                    tableMeta.setComment(safeGetString(rs, "REMARKS"));
                    tableMeta.setCatalog(firstNonNull(safeGetString(rs, "TABLE_CAT"), effectiveCatalog));
                    tableMeta.setSchema(firstNonNull(safeGetString(rs, "TABLE_SCHEM"), schema));

                    List<String> primaryKeys = parsePrimaryKeys(metaData, tableMeta.getCatalog(), tableMeta.getSchema(), tableName);
                    tableMeta.setPrimaryKeys(primaryKeys);
                    tableMeta.setColumnMetas(parseColumns(metaData, tableMeta.getCatalog(), tableMeta.getSchema(), tableName, primaryKeys));
                    tableMeta.setForeignKeyMetas(parseForeignKeys(metaData, tableMeta.getCatalog(), tableMeta.getSchema(), tableName));
                    tableMeta.setIndexMetas(parseIndexes(metaData, tableMeta.getCatalog(), tableMeta.getSchema(), tableName));
                    list.add(tableMeta);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<ViewMeta> listViews(String catalog, String schema, String viewName) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            List<ViewMeta> list = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(effectiveCatalog, schema, pattern(viewName), new String[]{"VIEW"})) {
                while (rs.next()) {
                    String name = safeGetString(rs, "TABLE_NAME");
                    ViewMeta viewMeta = new ViewMeta();
                    viewMeta.setName(name);
                    viewMeta.setComment(safeGetString(rs, "REMARKS"));
                    viewMeta.setCatalog(firstNonNull(safeGetString(rs, "TABLE_CAT"), effectiveCatalog));
                    viewMeta.setSchema(firstNonNull(safeGetString(rs, "TABLE_SCHEM"), schema));
                    viewMeta.setColumnMetas(parseColumns(metaData, viewMeta.getCatalog(), viewMeta.getSchema(), name, Collections.emptyList()));
                    list.add(viewMeta);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<FunctionMeta> listFunctions(String catalog, String schema, String function) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            List<FunctionMeta> list = new ArrayList<>();
            try (ResultSet rs = metaData.getFunctions(effectiveCatalog, schema, pattern(function))) {
                while (rs.next()) {
                    FunctionMeta meta = new FunctionMeta();
                    meta.setName(safeGetString(rs, "FUNCTION_NAME"));
                    meta.setCatalog(firstNonNull(safeGetString(rs, "FUNCTION_CAT"), effectiveCatalog));
                    meta.setSchema(firstNonNull(safeGetString(rs, "FUNCTION_SCHEM"), schema));
                    meta.setComment(safeGetString(rs, "REMARKS"));
                    meta.setReturnType(safeGetShort(rs, "FUNCTION_TYPE", (short) 0));
                    list.add(meta);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<ProcedureMeta> listProcedures(String catalog, String schema, String procedure) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            List<ProcedureMeta> list = new ArrayList<>();
            try (ResultSet rs = metaData.getProcedures(effectiveCatalog, schema, pattern(procedure))) {
                while (rs.next()) {
                    ProcedureMeta meta = new ProcedureMeta();
                    meta.setName(safeGetString(rs, "PROCEDURE_NAME"));
                    meta.setCatalog(firstNonNull(safeGetString(rs, "PROCEDURE_CAT"), effectiveCatalog));
                    meta.setSchema(firstNonNull(safeGetString(rs, "PROCEDURE_SCHEM"), schema));
                    meta.setComment(safeGetString(rs, "REMARKS"));
                    meta.setProcedureType(safeGetShort(rs, "PROCEDURE_TYPE", (short) 0));
                    list.add(meta);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<SequenceMeta> listSequences(String catalog, String schema, String sequence) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            List<SequenceMeta> list = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(effectiveCatalog, schema, pattern(sequence), new String[]{"SEQUENCE"})) {
                while (rs.next()) {
                    SequenceMeta meta = new SequenceMeta();
                    meta.setName(safeGetString(rs, "TABLE_NAME"));
                    meta.setCatalog(firstNonNull(safeGetString(rs, "TABLE_CAT"), effectiveCatalog));
                    meta.setSchema(firstNonNull(safeGetString(rs, "TABLE_SCHEM"), schema));
                    meta.setComment(safeGetString(rs, "REMARKS"));
                    list.add(meta);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<ColumnMeta> listColumns(String catalog, String schema, String tableName) {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String effectiveCatalog = effectiveCatalog(connection, catalog);
            List<String> primaryKeys = parsePrimaryKeys(metaData, effectiveCatalog, schema, tableName);
            return parseColumns(metaData, effectiveCatalog, schema, tableName, primaryKeys);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<String> listPrimaryKeys(String catalog, String schema, String tableName) {
        Connection connection = getConnection();
        try {
            return parsePrimaryKeys(connection.getMetaData(), effectiveCatalog(connection, catalog), schema, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<ForeignKeyMeta> listForeignKeys(String catalog, String schema, String tableName) {
        Connection connection = getConnection();
        try {
            return parseForeignKeys(connection.getMetaData(), effectiveCatalog(connection, catalog), schema, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<IndexMeta> listIndexes(String catalog, String schema, String tableName) {
        Connection connection = getConnection();
        try {
            return parseIndexes(connection.getMetaData(), effectiveCatalog(connection, catalog), schema, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private Connection getConnection() {
        Connection connection = connectionProvider.getConnection();
        if (connection == null) {
            throw new IllegalStateException("IConnectionProvider返回的Connection不能为空");
        }
        return connection;
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            connectionProvider.closeConnection(connection);
        }
    }

    private String effectiveCatalog(Connection connection, String catalog) throws SQLException {
        return catalog != null ? catalog : connection.getCatalog();
    }

    private String pattern(String value) {
        return value == null || value.trim().isEmpty() ? "%" : value;
    }

    private List<String> parsePrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) {
        List<KeyColumn> keys = new ArrayList<>();
        try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                String column = safeGetString(rs, "COLUMN_NAME");
                if (column != null) {
                    keys.add(new KeyColumn(safeGetShort(rs, "KEY_SEQ", Short.MAX_VALUE), column));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        keys.sort(Comparator.comparingInt(o -> o.seq));
        List<String> list = new ArrayList<>(keys.size());
        for (KeyColumn key : keys) {
            list.add(key.name);
        }
        return list;
    }

    private List<ColumnMeta> parseColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName,
                                          List<String> primaryKeys) throws SQLException {
        Set<String> keySet = new LinkedHashSet<>(primaryKeys);
        try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, null)) {
            List<ColumnMeta> list = new ArrayList<>();
            while (rs.next()) {
                ColumnMeta columnMeta = new ColumnMeta();
                columnMeta.setCatalog(firstNonNull(safeGetString(rs, "TABLE_CAT"), catalog));
                columnMeta.setSchema(firstNonNull(safeGetString(rs, "TABLE_SCHEM"), schema));
                columnMeta.setTable(firstNonNull(safeGetString(rs, "TABLE_NAME"), tableName));
                columnMeta.setColumnName(safeGetString(rs, "COLUMN_NAME"));
                columnMeta.setOrdinalPosition(safeGetInteger(rs, "ORDINAL_POSITION"));
                columnMeta.setColumnType(safeGetString(rs, "TYPE_NAME"));

                Integer jdbcType = safeGetInteger(rs, "DATA_TYPE");
                columnMeta.setJdbcType(jdbcType);
                columnMeta.setJavaType(toJavaTypeName(jdbcType));

                columnMeta.setAutoIncrement(toYesNoBoolean(safeGetString(rs, "IS_AUTOINCREMENT")));
                columnMeta.setColumnLength(safeGetInteger(rs, "COLUMN_SIZE"));
                columnMeta.setDecimalDigits(safeGetInteger(rs, "DECIMAL_DIGITS"));
                columnMeta.setNullable(toNullableBoolean(safeGetInteger(rs, "NULLABLE")));
                columnMeta.setColumnComment(safeGetString(rs, "REMARKS"));
                columnMeta.setDefaultValue(safeGetString(rs, "COLUMN_DEF"));
                columnMeta.setPrimaryKey(keySet.contains(columnMeta.getColumnName()));
                list.add(columnMeta);
            }
            return list;
        }
    }

    private List<ForeignKeyMeta> parseForeignKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) {
        Map<String, ForeignKeyMeta> fkMap = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getImportedKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                String fkName = firstNonNull(safeGetString(rs, "FK_NAME"), "FK_" + tableName);
                ForeignKeyMeta fk = fkMap.get(fkName);
                if (fk == null) {
                    fk = new ForeignKeyMeta();
                    fk.setName(fkName);
                    fk.setPkCatalog(safeGetString(rs, "PKTABLE_CAT"));
                    fk.setPkSchema(safeGetString(rs, "PKTABLE_SCHEM"));
                    fk.setPkTable(safeGetString(rs, "PKTABLE_NAME"));
                    fk.setFkCatalog(safeGetString(rs, "FKTABLE_CAT"));
                    fk.setFkSchema(safeGetString(rs, "FKTABLE_SCHEM"));
                    fk.setFkTable(safeGetString(rs, "FKTABLE_NAME"));
                    fk.setDeleteRule(safeGetShort(rs, "DELETE_RULE", (short) 0));
                    fk.setUpdateRule(safeGetShort(rs, "UPDATE_RULE", (short) 0));
                    fkMap.put(fkName, fk);
                }
                fk.addColumn(
                        safeGetShort(rs, "KEY_SEQ", Short.MAX_VALUE),
                        safeGetString(rs, "FKCOLUMN_NAME"),
                        safeGetString(rs, "PKCOLUMN_NAME")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>(fkMap.values());
    }

    private List<IndexMeta> parseIndexes(DatabaseMetaData metaData, String catalog, String schema, String tableName) {
        Map<String, IndexMeta> indexMap = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getIndexInfo(catalog, schema, tableName, false, false)) {
            while (rs.next()) {
                String indexName = safeGetString(rs, "INDEX_NAME");
                String column = safeGetString(rs, "COLUMN_NAME");
                if (indexName == null || column == null) {
                    continue;
                }
                IndexMeta index = indexMap.get(indexName);
                if (index == null) {
                    index = new IndexMeta();
                    index.setName(indexName);
                    index.setUnique(!safeGetBoolean(rs, "NON_UNIQUE", true));
                    indexMap.put(indexName, index);
                }
                index.addColumn(
                        safeGetShort(rs, "ORDINAL_POSITION", Short.MAX_VALUE),
                        column,
                        safeGetString(rs, "ASC_OR_DESC")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>(indexMap.values());
    }

    private void addIfPresent(List<String> list, String value) {
        if (value != null) {
            list.add(value);
        }
    }

    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private String safeGetString(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer safeGetInteger(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    private short safeGetShort(ResultSet rs, String column, short defaultValue) throws SQLException {
        try {
            short value = rs.getShort(column);
            return rs.wasNull() ? defaultValue : value;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private boolean safeGetBoolean(ResultSet rs, String column, boolean defaultValue) throws SQLException {
        try {
            boolean value = rs.getBoolean(column);
            return rs.wasNull() ? defaultValue : value;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private Boolean toYesNoBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }

    private Boolean toNullableBoolean(Integer value) {
        if (value == null || value == DatabaseMetaData.columnNullableUnknown) {
            return null;
        }
        return value == DatabaseMetaData.columnNullable;
    }

    private String toJavaTypeName(Integer jdbcType) {
        if (jdbcType == null) {
            return null;
        }
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.SQLXML:
                return String.class.getName();
            case Types.TINYINT:
                return Byte.class.getName();
            case Types.SMALLINT:
                return Short.class.getName();
            case Types.INTEGER:
                return Integer.class.getName();
            case Types.BIGINT:
                return Long.class.getName();
            case Types.REAL:
                return Float.class.getName();
            case Types.FLOAT:
            case Types.DOUBLE:
                return Double.class.getName();
            case Types.NUMERIC:
            case Types.DECIMAL:
                return java.math.BigDecimal.class.getName();
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class.getName();
            case Types.DATE:
                return java.sql.Date.class.getName();
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return java.sql.Time.class.getName();
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return java.sql.Timestamp.class.getName();
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return byte[].class.getName();
            case Types.BLOB:
                return java.sql.Blob.class.getName();
            case Types.ARRAY:
                return java.sql.Array.class.getName();
            case Types.REF:
            case Types.REF_CURSOR:
                return java.sql.Ref.class.getName();
            default:
                return Object.class.getName();
        }
    }

    private static class KeyColumn {
        private final short seq;
        private final String name;

        private KeyColumn(short seq, String name) {
            this.seq = seq;
            this.name = name;
        }
    }
}
