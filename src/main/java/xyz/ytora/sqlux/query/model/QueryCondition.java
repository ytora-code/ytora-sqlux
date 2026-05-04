package xyz.ytora.sqlux.query.model;

import xyz.ytora.sqlux.query.QueryOp;
import xyz.ytora.sqlux.sql.model.ColumnRef;

import java.util.Collections;
import java.util.List;

/**
 * WHERE 条件规则。
 *
 * @author ytora
 * @since 1.0
 */
public class QueryCondition {

    private final String fieldName;

    private final String columnName;

    private final ColumnRef column;

    private final QueryOp op;

    private final List<Object> values;

    public QueryCondition(String fieldName, String columnName, ColumnRef column, QueryOp op, List<Object> values) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.column = column;
        this.op = op == null ? QueryOp.EQ : op;
        this.values = values == null ? Collections.emptyList() : Collections.unmodifiableList(values);
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

    public QueryOp getOp() {
        return op;
    }

    public List<Object> getValues() {
        return values;
    }
}
