package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.SqlRewriteContext;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.translate.DialectFactory;
import xyz.ytora.sqlux.translate.SqlResult;

/**
 * SELECT 翻译辅助工具。
 *
 * <p>集中处理翻译前拦截器和方言翻译，避免阶段对象和分页执行器重复持有翻译细节。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class SelectSqlSupport {

    private SelectSqlSupport() {
    }

    static SqlResult toSql(SelectQuery query) {
        applyBeforeTranslate(query);
        return translateDirect(query, SQL.getSqluxGlobal().getDbType());
    }

    static SqlResult toSql(SelectQuery query, DbType dbType) {
        applyBeforeTranslate(query);
        return translateDirect(query, dbType);
    }

    static void applyBeforeTranslate(SelectQuery query) {
        for (Interceptor interceptor : SQL.getSqluxGlobal().snapshotInterceptors()) {
            interceptor.beforeTranslate(new SqlRewriteContext(SqlType.SELECT, query));
        }
    }

    static SqlResult translateDirect(SelectQuery query, DbType dbType) {
        return DialectFactory.getDialect(dbType).selectTranslator().translate(query);
    }
}
