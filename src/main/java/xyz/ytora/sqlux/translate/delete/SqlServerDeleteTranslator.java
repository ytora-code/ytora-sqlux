package xyz.ytora.sqlux.translate.delete;

import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.ExpressionTranslator;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.SqlTranslator;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.SqlRenderUtil;

/**
 * SQL Server DELETE翻译器。
 *
 * <p>SQL Server 多表关联删除使用 {@code DELETE t FROM table t JOIN ...} 语法，
 * 但一次只能删除一个目标表。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerDeleteTranslator implements SqlTranslator<DeleteQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    public SqlServerDeleteTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    @Override
    public SqlResult translate(DeleteQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("DELETE查询不能为空");
        }
        if (query.getFrom() == null) {
            throw new IllegalStateException("DELETE缺少FROM阶段");
        }
        String logicDeleteColumn = OrmMapper.findLogicDeleteColumn(query.getFrom().getTableClass());
        if (logicDeleteColumn != null) {
            return translateLogicDelete(query, logicDeleteColumn);
        }
        if (!isSingleFromTarget(query)) {
            throw new UnsupportedOperationException("SQL Server不支持一次DELETE删除多个目标表");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        if (query.getJoins().isEmpty()
                && (query.getFrom().getAlias() == null || query.getFrom().getAlias().isEmpty())) {
            sql.append("DELETE FROM ").append(SqlRenderUtil.table(query.getFrom(), dialect));
        } else {
            sql.append("DELETE ").append(deleteTarget(query))
                    .append(" FROM ").append(SqlRenderUtil.table(query.getFrom(), dialect));
            appendJoins(sql, query, context);
        }
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.DELETE, query);
    }

    private SqlResult translateLogicDelete(DeleteQuery query, String logicDeleteColumn) {
        if (!query.getDeleteTargets().isEmpty() || !query.getJoins().isEmpty()) {
            throw new UnsupportedOperationException("LogicDelete暂只支持普通单表DELETE自动改写");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ")
                .append(deleteTarget(query))
                .append(" SET ")
                .append(SqlRenderUtil.column(ColumnRef.of(query.getFrom().getTableClass(), logicDeleteColumn), context))
                .append(" = ")
                .append(context.addParam(1))
                .append(" FROM ")
                .append(SqlRenderUtil.table(query.getFrom(), dialect));
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.DELETE, query);
    }

    private void appendJoins(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        for (JoinClause join : query.getJoins()) {
            sql.append(" ")
                    .append(join.getJoinType().getJoinKey())
                    .append(" ")
                    .append(SqlRenderUtil.source(join.getTable(), context))
                    .append(" ON ")
                    .append(expressionTranslator.translate(join.getOn(), context));
        }
    }

    private void appendWhere(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
    }

    private String deleteTarget(DeleteQuery query) {
        if (query.getFrom().getAlias() == null || query.getFrom().getAlias().isEmpty()) {
            return dialect.quoteIdentifier(query.getFrom().getTableName());
        }
        return query.getFrom().getAlias();
    }

    private boolean isSingleFromTarget(DeleteQuery query) {
        return query.getDeleteTargets().isEmpty()
                || (query.getDeleteTargets().size() == 1
                && query.getDeleteTargets().get(0).equals(query.getFrom().getTableClass()));
    }
}
