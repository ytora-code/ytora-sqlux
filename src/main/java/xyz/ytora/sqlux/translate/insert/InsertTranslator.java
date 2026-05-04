package xyz.ytora.sqlux.translate.insert;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.translate.*;
import xyz.ytora.sqlux.translate.select.SelectTranslator;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * INSERT翻译器。
 *
 * @author ytora
 * @since 1.0
 */
public class InsertTranslator implements SqlTranslator<InsertQuery> {

    private final Dialect dialect;

    /**
     * 使用全局默认数据库类型创建 INSERT 翻译器。
     */
    public InsertTranslator() {
        this(DialectFactory.getDialect(SQL.getSqluxGlobal().getDefaultDbType()));
    }

    /**
     * 使用指定方言创建 INSERT 翻译器。
     *
     * @param dialect 数据库方言，用于标识符转义和占位符生成
     */
    public InsertTranslator(Dialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 将 INSERT 查询模型翻译为 SQL 和有序参数。
     *
     * @param query INSERT 查询模型
     * @return SQL 翻译结果
     */
    @Override
    public SqlResult translate(InsertQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("INSERT查询不能为空");
        }
        if (query.getRows().isEmpty() && query.getSelectQuery() == null) {
            throw new IllegalStateException("INSERT缺少VALUES数据或SELECT子查询");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(dialect.quoteIdentifier(query.getTable().getTableName()));
        appendColumns(sql, query);
        appendValues(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.INSERT, query);
    }

    /**
     * 追加 INSERT 字段列表。
     *
     * @param sql SQL 拼接缓冲区
     * @param query INSERT 查询模型
     */
    private void appendColumns(StringBuilder sql, InsertQuery query) {
        if (query.getColumns().isEmpty()) {
            return;
        }
        List<String> columns = new ArrayList<>();
        for (ColumnRef column : query.getColumns()) {
            columns.add(dialect.quoteIdentifier(column.getColumnName()));
        }
        sql.append(" (").append(SqlRenderUtil.join(columns, ", ")).append(")");
    }

    /**
     * 追加 VALUES 行或 INSERT SELECT 子查询。
     *
     * @param sql SQL 拼接缓冲区
     * @param query INSERT 查询模型
     * @param context 翻译上下文，用于收集插入值参数
     */
    private void appendValues(StringBuilder sql, InsertQuery query, TranslateContext context) {
        if (query.getSelectQuery() != null) {
            SelectTranslator translator = new SelectTranslator(dialect);
            SqlResult result = translator.translate(query.getSelectQuery());
            context.addParams(result.getParams());
            sql.append(" ").append(result.getSql());
            return;
        }
        List<String> rows = new ArrayList<>();
        for (List<Object> row : query.getRows()) {
            List<String> values = new ArrayList<>();
            for (Object value : row) {
                values.add(context.addParam(value));
            }
            rows.add("(" + SqlRenderUtil.join(values, ", ") + ")");
        }
        sql.append(" VALUES ").append(SqlRenderUtil.join(rows, ", "));
    }
}
