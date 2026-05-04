package xyz.ytora.sqlux.query.model;

import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * GROUP BY 字段规则。
 *
 * @author ytora
 * @since 1.0
 */
public class QueryGroup {

    private final String fieldName;

    private final String columnName;

    private final ColumnRef column;

    public QueryGroup(String fieldName, String columnName, ColumnRef column) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.column = column;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColumnRef getColumn() {
        return column;
    }
}
