package xyz.ytora.sqlux.translate.select;

import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.OrderClause;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.ExpressionTranslator;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.SqlTranslator;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SELECT 翻译器基类。
 *
 * <p>集中处理 SELECT 主体、JOIN、WHERE、GROUP BY、HAVING 和 ORDER BY 的通用翻译，
 * 子类只需要关注不同数据库的分页语法差异。</p>
 *
 * @author ytora
 * @since 1.0
 */
abstract class AbstractSelectTranslator implements SqlTranslator<SelectQuery> {

    protected final Dialect dialect;

    protected final ExpressionTranslator expressionTranslator;

    AbstractSelectTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    @Override
    public SqlResult translate(SelectQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("SELECT查询不能为空");
        }
        if (query.getFrom() == null) {
            throw new IllegalStateException("SELECT缺少FROM阶段");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        appendSelectStart(sql, query, context);
        sql.append(translateSelectColumns(query.getSelectColumns(), context));
        sql.append(" FROM ").append(SqlRenderUtil.source(query.getFrom(), context));
        appendJoins(sql, query, context);
        appendWhere(sql, query, context);
        appendGroupBy(sql, query, context);
        appendHaving(sql, query, context);
        appendOrderBy(sql, query, context);
        appendPaging(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.SELECT, query);
    }

    protected void appendSelectStart(StringBuilder sql, SelectQuery query, TranslateContext context) {
        sql.append("SELECT ");
        if (query.isDistinct()) {
            sql.append("DISTINCT ");
        }
    }

    protected void appendOrderBy(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getOrderByColumns().isEmpty()) {
            return;
        }
        List<String> items = new ArrayList<>();
        for (OrderClause order : query.getOrderByColumns()) {
            items.add(SqlRenderUtil.expression(order.getColumn(), context) + " " + order.getOrderType().name());
        }
        sql.append(" ORDER BY ").append(SqlRenderUtil.join(items, ", "));
    }

    protected abstract void appendPaging(StringBuilder sql, SelectQuery query, TranslateContext context);

    private String translateSelectColumns(List<SqlExpression> columns, TranslateContext context) {
        if (columns == null || columns.isEmpty()) {
            return "*";
        }
        List<String> items = new ArrayList<>();
        for (SqlExpression column : columns) {
            items.add(SqlRenderUtil.selectItem(column, context));
        }
        return SqlRenderUtil.join(items, ", ");
    }

    private void appendJoins(StringBuilder sql, SelectQuery query, TranslateContext context) {
        for (JoinClause join : query.getJoins()) {
            sql.append(" ")
                    .append(join.getJoinType().getJoinKey())
                    .append(" ")
                    .append(SqlRenderUtil.source(join.getTable(), context))
                    .append(" ON ")
                    .append(expressionTranslator.translate(join.getOn(), context));
        }
    }

    private void appendWhere(StringBuilder sql, SelectQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
    }

    private void appendGroupBy(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getGroupByColumns().isEmpty()) {
            return;
        }
        List<String> items = new ArrayList<>();
        for (SqlExpression column : query.getGroupByColumns()) {
            items.add(SqlRenderUtil.expression(column, context));
        }
        sql.append(" GROUP BY ").append(SqlRenderUtil.join(items, ", "));
    }

    private void appendHaving(StringBuilder sql, SelectQuery query, TranslateContext context) {
        String having = expressionTranslator.translate(query.getHaving(), context);
        if (!having.isEmpty()) {
            sql.append(" HAVING ").append(having);
        }
    }
}
