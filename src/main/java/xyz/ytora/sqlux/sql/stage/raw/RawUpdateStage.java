package xyz.ytora.sqlux.sql.stage.raw;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.Map;

/**
 * 原生更新语句阶段。
 */
public class RawUpdateStage {

    private final String sql;

    private final Object[] params;

    private final Map<String, ?> namedParams;

    public RawUpdateStage(String sql, Object... params) {
        this.sql = sql;
        this.params = params;
        this.namedParams = null;
    }

    public RawUpdateStage(String sql, Map<String, ?> namedParams) {
        this.sql = sql;
        this.params = null;
        this.namedParams = namedParams;
    }

    public Integer submit() {
        return SQL.getSqluxGlobal().getExecutor().update(toSql());
    }

    public SqlResult toSql() {
        if (namedParams != null) {
            return RawSqlSupport.toSqlResult(sql, RawSqlSupport.detectMutationType(sql), namedParams);
        }
        return RawSqlSupport.toSqlResult(sql, RawSqlSupport.detectMutationType(sql), params);
    }
}
