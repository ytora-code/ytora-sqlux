package xyz.ytora.sqlux.translate.select;

import xyz.ytora.sqlux.sql.model.OrderClause;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL Server SELECT翻译器。
 *
 * <p>SQL Server 无偏移分页使用 {@code TOP (?)}，带偏移分页使用
 * {@code OFFSET ... ROWS FETCH NEXT ... ROWS ONLY}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerSelectTranslator extends AbstractSelectTranslator {

    public SqlServerSelectTranslator(Dialect dialect) {
        super(dialect);
    }

    @Override
    protected void appendSelectStart(StringBuilder sql, SelectQuery query, TranslateContext context) {
        super.appendSelectStart(sql, query, context);
        if (query.getLimit() != null && query.getOffset() == null) {
            sql.append("TOP (").append(context.addParam(query.getLimit())).append(") ");
        }
    }

    @Override
    protected void appendOrderBy(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getOrderByColumns().isEmpty()) {
            if (query.getOffset() != null) {
                sql.append(" ORDER BY (SELECT 0)");
            }
            return;
        }
        List<String> items = new ArrayList<>();
        for (OrderClause order : query.getOrderByColumns()) {
            items.add(SqlRenderUtil.expression(order.getColumn(), context) + " " + order.getOrderType().name());
        }
        sql.append(" ORDER BY ").append(SqlRenderUtil.join(items, ", "));
    }

    @Override
    protected void appendPaging(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getOffset() == null) {
            return;
        }
        sql.append(" OFFSET ").append(context.addParam(query.getOffset())).append(" ROWS");
        if (query.getLimit() != null) {
            sql.append(" FETCH NEXT ").append(context.addParam(query.getLimit())).append(" ROWS ONLY");
        }
    }
}
