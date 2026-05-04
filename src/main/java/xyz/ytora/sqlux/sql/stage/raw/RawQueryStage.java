package xyz.ytora.sqlux.sql.stage.raw;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 原生 SELECT 查询阶段。
 */
public class RawQueryStage {

    private final String sql;

    private final Object[] params;

    private final Map<String, ?> namedParams;

    public RawQueryStage(String sql, Object... params) {
        this.sql = sql;
        this.params = params;
        this.namedParams = null;
    }

    public RawQueryStage(String sql, Map<String, ?> namedParams) {
        this.sql = sql;
        this.params = null;
        this.namedParams = namedParams;
    }

    public List<Map<String, Object>> submit() {
        return SQL.getSqluxGlobal().getExecutor().query(toSql());
    }

    public <T> List<T> submit(Class<T> resultType) {
        return SQL.getSqluxGlobal().getExecutor().query(toSql(), resultType);
    }

    public <T> Optional<T> submit(Class<T> resultType, int index) {
        List<T> rows = submit(resultType);
        if (index < 0 || index >= rows.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rows.get(index));
    }

    public SqlResult toSql() {
        if (namedParams != null) {
            return RawSqlSupport.toSqlResult(sql, SqlType.SELECT, namedParams);
        }
        return RawSqlSupport.toSqlResult(sql, SqlType.SELECT, params);
    }
}
