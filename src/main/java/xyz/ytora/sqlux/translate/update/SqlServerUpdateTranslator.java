package xyz.ytora.sqlux.translate.update;

import xyz.ytora.sqlux.sql.model.Assignment;
import xyz.ytora.sqlux.sql.model.ColumnIncrement;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.ExpressionTranslator;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.SqlTranslator;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL Server UPDATE翻译器。
 *
 * <p>SQL Server 多表更新使用 {@code UPDATE t SET ... FROM table t JOIN ...} 语法。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerUpdateTranslator implements SqlTranslator<UpdateQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    public SqlServerUpdateTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    @Override
    public SqlResult translate(UpdateQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("UPDATE查询不能为空");
        }
        if (query.getAssignments().isEmpty()) {
            throw new IllegalStateException("UPDATE缺少SET阶段");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(updateTarget(query));
        appendSets(sql, query, context);
        appendFrom(sql, query, context);
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.UPDATE, query);
    }

    private String updateTarget(UpdateQuery query) {
        if (query.getTable().getAlias() == null || query.getTable().getAlias().isEmpty()) {
            return dialect.quoteIdentifier(query.getTable().getTableName());
        }
        return query.getTable().getAlias();
    }

    private void appendSets(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        List<String> items = new ArrayList<>();
        for (Assignment assignment : query.getAssignments()) {
            String value;
            if (assignment.getValue() instanceof ColumnIncrement) {
                ColumnIncrement increment = (ColumnIncrement) assignment.getValue();
                value = SqlRenderUtil.column(assignment.getColumn(), context) + " + " + increment.getStep();
            } else {
                value = assignment.isRaw()
                        ? String.valueOf(assignment.getValue())
                        : SqlRenderUtil.value(assignment.getValue(), context);
            }
            items.add(SqlRenderUtil.column(assignment.getColumn(), context) + " = " + value);
        }
        sql.append(" SET ").append(SqlRenderUtil.join(items, ", "));
    }

    private void appendFrom(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        if (query.getJoins().isEmpty() && (query.getTable().getAlias() == null || query.getTable().getAlias().isEmpty())) {
            return;
        }
        sql.append(" FROM ").append(SqlRenderUtil.table(query.getTable(), dialect));
        for (JoinClause join : query.getJoins()) {
            sql.append(" ")
                    .append(join.getJoinType().getJoinKey())
                    .append(" ")
                    .append(SqlRenderUtil.source(join.getTable(), context))
                    .append(" ON ")
                    .append(expressionTranslator.translate(join.getOn(), context));
        }
    }

    private void appendWhere(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
        if (query.hasVersionLock()) {
            if (where.isEmpty()) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }
            sql.append(SqlRenderUtil.column(query.getVersionColumn(), context))
                    .append(" = ")
                    .append(context.addParam(query.getVersionValue()));
        }
    }
}
