package xyz.ytora.sqlux.util;

import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.QuerySource;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.TableRef;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.translate.select.SelectTranslator;

import java.util.List;

/**
 * SQL片段渲染工具。
 *
 * <p>提供表名、字段名、值和字符串列表的通用渲染逻辑。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class SqlRenderUtil {

    /**
     * 工具类不允许实例化。
     */
    private SqlRenderUtil() {
    }

    /**
     * 渲染实体表引用。
     *
     * @param table 表引用
     * @param dialect 数据库方言
     * @return 带方言转义和可选别名的表 SQL 片段
     */
    public static String table(TableRef table, Dialect dialect) {
        String tableName = dialect.quoteIdentifier(table.getTableName());
        if (table.getAlias() == null || table.getAlias().isEmpty()) {
            return tableName;
        }
        return tableName + " " + table.getAlias();
    }

    /**
     * 渲染通用查询数据源。
     *
     * @param source 表或子查询数据源
     * @param context 翻译上下文
     * @return FROM/JOIN 中使用的数据源 SQL 片段
     */
    public static String source(QuerySource source, TranslateContext context) {
        if (source == null) {
            throw new IllegalArgumentException("SQL数据源不能为空");
        }
        return source.renderSource(context);
    }

    /**
     * 渲染字段引用。
     *
     * @param column 字段引用
     * @param context 翻译上下文，用于读取方言和阶段别名
     * @return 字段 SQL 片段
     */
    public static String column(ColumnRef column, TranslateContext context) {
        if (column.isRawExpression()) {
            return column.getColumnName();
        }
        String columnName = context.getDialect().quoteIdentifier(column.getColumnName());
        String alias = column.getExplicitAlias();
        if (alias == null || alias.isEmpty()) {
            alias = context.getStageContextHolder().getAlias(column.getTableClass());
        }
        if (alias == null || alias.isEmpty()) {
            return columnName;
        }
        return alias + "." + columnName;
    }

    /**
     * 渲染普通值或 SQL 表达式。
     *
     * @param value 普通参数值或 SQL 表达式
     * @param context 翻译上下文
     * @return SQL 表达式片段或参数占位符
     */
    public static String value(Object value, TranslateContext context) {
        if (value instanceof SqlExpression) {
            return expression((SqlExpression) value, context);
        }
        return context.addParam(value);
    }

    /**
     * 渲染 SQL 表达式。
     *
     * @param expression SQL 表达式
     * @param context 翻译上下文
     * @return SQL 表达式片段
     */
    public static String expression(SqlExpression expression, TranslateContext context) {
        if (expression == null) {
            throw new IllegalArgumentException("SQL表达式不能为空");
        }
        if (expression instanceof ColumnRef) {
            return column((ColumnRef) expression, context);
        }
        return expression.render(context);
    }

    /**
     * 渲染 SELECT 项。
     *
     * @param expression SELECT 字段或表达式
     * @param context 翻译上下文
     * @return SELECT 项 SQL 片段
     */
    public static String selectItem(SqlExpression expression, TranslateContext context) {
        if (expression == null) {
            throw new IllegalArgumentException("SELECT表达式不能为空");
        }
        return expression.renderSelectItem(context);
    }

    /**
     * 渲染子查询并把子查询参数追加到外层上下文。
     *
     * @param query 子查询模型
     * @param context 外层翻译上下文
     * @return 子查询 SQL，不包含外层括号
     */
    public static String subQuery(SelectQuery query, TranslateContext context) {
        if (query == null) {
            throw new IllegalArgumentException("子查询不能为空");
        }
        SelectTranslator translator = new SelectTranslator(context.getDialect());
        SqlResult result = translator.translate(query);
        context.addParams(result.getParams());
        return result.getSql();
    }

    /**
     * 渲染原始 SQL 片段并替换其中的 {@code ?} 参数占位符。
     *
     * @param rawSql 原始 SQL 片段
     * @param params 与 {@code ?} 顺序对应的参数
     * @param context 翻译上下文，用于生成方言占位符并收集参数
     * @return 替换占位符后的 SQL 片段
     */
    public static String raw(String rawSql, List<Object> params, TranslateContext context) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            throw new IllegalArgumentException("原始SQL片段不能为空");
        }
        int placeholderCount = 0;
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < rawSql.length(); i++) {
            char ch = rawSql.charAt(i);
            if (ch == '?') {
                if (params == null || placeholderCount >= params.size()) {
                    throw new IllegalArgumentException("原始SQL片段中的参数占位符数量与实参数量不匹配");
                }
                sql.append(context.addParam(params.get(placeholderCount)));
                placeholderCount++;
            } else {
                sql.append(ch);
            }
        }
        int paramSize = params == null ? 0 : params.size();
        if (placeholderCount != paramSize) {
            throw new IllegalArgumentException("原始SQL片段中的参数占位符数量与实参数量不匹配");
        }
        return sql.toString();
    }

    /**
     * 用指定分隔符拼接 SQL 片段列表。
     *
     * @param items SQL 片段列表
     * @param delimiter 分隔符
     * @return 拼接后的 SQL 片段
     */
    public static String join(List<String> items, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(items.get(i));
        }
        return builder.toString();
    }
}
