package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.orm.OrmMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 结果读取器。
 *
 * <p>负责读取查询结果和 generated keys，执行器无需关心结果集元数据细节。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class JdbcResultReader {

    private JdbcResultReader() {
    }

    static List<Map<String, Object>> readRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String key = metaData.getColumnLabel(i);
                if (key == null || key.isEmpty()) {
                    key = metaData.getColumnName(i);
                }
                row.put(key, resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    static void backfillGeneratedKeys(PreparedStatement statement, List<?> generatedKeyTargets) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            List<Map<String, Object>> keyRows = readRows(generatedKeys);
            int count = Math.min(keyRows.size(), generatedKeyTargets.size());
            for (int i = 0; i < count; i++) {
                OrmMapper.backfillGeneratedKeys(generatedKeyTargets.get(i), keyRows.get(i));
            }
        }
    }

    static boolean shouldReturnGeneratedKeys(List<?> generatedKeyTargets) {
        return generatedKeyTargets != null && !generatedKeyTargets.isEmpty();
    }

    static int generatedKeysFlag() {
        return Statement.RETURN_GENERATED_KEYS;
    }
}
