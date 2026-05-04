package xyz.ytora.sqlux.translate.select;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.DialectFactory;
import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 通用 SELECT 翻译器。
 *
 * <p>适用于 MySQL、PostgreSQL 等使用 {@code LIMIT/OFFSET} 分页语法的数据库。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SelectTranslator extends AbstractSelectTranslator {

    /**
     * 使用全局默认数据库类型创建 SELECT 翻译器。
     */
    public SelectTranslator() {
        this(DialectFactory.getDialect(SQL.getSqluxGlobal().getDefaultDbType()));
    }

    /**
     * 使用指定方言创建 SELECT 翻译器。
     *
     * @param dialect 数据库方言
     */
    public SelectTranslator(Dialect dialect) {
        super(dialect);
    }

    @Override
    protected void appendPaging(StringBuilder sql, SelectQuery query, TranslateContext context) {
        if (query.getLimit() != null) {
            sql.append(" LIMIT ").append(context.addParam(query.getLimit()));
        }
        if (query.getOffset() != null) {
            sql.append(" OFFSET ").append(context.addParam(query.getOffset()));
        }
    }
}
