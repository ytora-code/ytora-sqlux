package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.rw.SqlWriter;
import xyz.ytora.sqlux.rw.TypeHandlers;
import xyz.ytora.sqlux.translate.SqlResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC 参数绑定器。
 *
 * <p>负责把翻译后的参数按顺序绑定到 {@link PreparedStatement}，并补足手工参数的写转换。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class JdbcParameterBinder {

    private JdbcParameterBinder() {
    }

    static void bind(PreparedStatement statement, SqlResult sqlResult) throws SQLException {
        bind(statement, sqlResult.getParams());
    }

    static void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, normalizeParam(params.get(i)));
        }
    }

    private static Object normalizeParam(Object value) {
        Object handled = TypeHandlers.write(value, null);
        if (handled != value) {
            return handled;
        }
        if (value instanceof SqlWriter) {
            return ((SqlWriter) value).write();
        }
        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }
        return value;
    }
}
