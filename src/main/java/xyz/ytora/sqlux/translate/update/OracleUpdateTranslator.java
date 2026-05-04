package xyz.ytora.sqlux.translate.update;

import xyz.ytora.sqlux.sql.model.Assignment;
import xyz.ytora.sqlux.sql.model.ColumnIncrement;
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
 * Oracle UPDATE翻译器。
 *
 * <p>Oracle/达梦 UPDATE 目标列在 SET 左侧不带表别名，JOIN UPDATE 暂不做自动改写。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OracleUpdateTranslator implements SqlTranslator<UpdateQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    public OracleUpdateTranslator(Dialect dialect) {
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
        if (!query.getJoins().isEmpty()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持UPDATE JOIN翻译");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(SqlRenderUtil.table(query.getTable(), dialect));
        appendSets(sql, query, context);
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.UPDATE, query);
    }

    private void appendSets(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        List<String> items = new ArrayList<>();
        for (Assignment assignment : query.getAssignments()) {
            String value;
            if (assignment.getValue() instanceof ColumnIncrement) {
                ColumnIncrement increment = (ColumnIncrement) assignment.getValue();
                value = dialect.quoteIdentifier(assignment.getColumn().getColumnName()) + " + " + increment.getStep();
            } else {
                value = assignment.isRaw()
                        ? String.valueOf(assignment.getValue())
                        : SqlRenderUtil.value(assignment.getValue(), context);
            }
            items.add(dialect.quoteIdentifier(assignment.getColumn().getColumnName()) + " = " + value);
        }
        sql.append(" SET ").append(SqlRenderUtil.join(items, ", "));
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
