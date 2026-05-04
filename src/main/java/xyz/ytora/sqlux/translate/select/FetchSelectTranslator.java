package xyz.ytora.sqlux.translate.select;

import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 标准 OFFSET/FETCH SELECT翻译器。
 *
 * <p>Oracle 12c、达梦等数据库支持 {@code OFFSET ... ROWS FETCH NEXT ... ROWS ONLY}
 * 语法。该翻译器复用通用 SELECT 主体，仅替换分页片段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class FetchSelectTranslator extends AbstractSelectTranslator {

    public FetchSelectTranslator(Dialect dialect) {
        super(dialect);
    }

    @Override
    protected void appendPaging(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getOffset() != null) {
            sql.append(" OFFSET ").append(context.addParam(query.getOffset())).append(" ROWS");
        }
        if (query.getLimit() != null) {
            if (query.getOffset() == null) {
                sql.append(" FETCH FIRST ").append(context.addParam(query.getLimit())).append(" ROWS ONLY");
            } else {
                sql.append(" FETCH NEXT ").append(context.addParam(query.getLimit())).append(" ROWS ONLY");
            }
        }
    }
}
