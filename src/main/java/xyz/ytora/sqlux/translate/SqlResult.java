package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.SqlType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL翻译结果。
 *
 * <p>包含可提交给 JDBC 的 SQL 文本和按占位符顺序排列的参数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlResult {

    private final String sql;

    private final List<Object> params;

    private final SqlType sqlType;

    private final Object statement;

    /**
     * 创建 SQL 翻译结果。
     *
     * @param sql SQL文本
     * @param params 有序参数列表
     */
    public SqlResult(String sql, List<Object> params) {
        this(sql, params, SqlType.UNKNOWN, null);
    }

    /**
     * 创建带 SQL 类型的 SQL 翻译结果。
     *
     * <p>示例：{@code new SqlResult("SELECT * FROM user", params, SqlType.SELECT)}
     * 表示该结果来自 SELECT 语句。</p>
     *
     * @param sql SQL文本；入参是已经翻译完成的可执行 SQL
     * @param params 有序参数列表；入参顺序与 SQL 占位符顺序一致
     * @param sqlType SQL语句类型；入参为 {@code null} 时按 {@link SqlType#UNKNOWN} 处理
     */
    public SqlResult(String sql, List<Object> params, SqlType sqlType) {
        this(sql, params, sqlType, null);
    }

    /**
     * 创建带 SQL 类型和源查询模型的 SQL 翻译结果。
     *
     * <p>示例：SELECT 翻译器可以传入 {@code SqlType.SELECT} 和 {@code SelectQuery}，
     * 后续拦截器或日志插件可以知道该 SQL 的来源类型和结构模型。</p>
     *
     * @param sql SQL文本；入参是已经翻译完成的可执行 SQL
     * @param params 有序参数列表；入参顺序与 SQL 占位符顺序一致
     * @param sqlType SQL语句类型；入参为 {@code null} 时按 {@link SqlType#UNKNOWN} 处理
     * @param statement 源查询模型；入参可能是 {@code SelectQuery}、{@code InsertQuery}、
     *                  {@code UpdateQuery}、{@code DeleteQuery}，也可以为 {@code null}
     */
    public SqlResult(String sql, List<Object> params, SqlType sqlType, Object statement) {
        this.sql = sql;
        if (params == null) {
            this.params = Collections.emptyList();
        } else {
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
        }
        this.sqlType = sqlType == null ? SqlType.UNKNOWN : sqlType;
        this.statement = statement;
    }

    /**
     * 获取 SQL 文本。
     *
     * @return SQL文本
     */
    public String getSql() {
        return sql;
    }

    /**
     * 获取 SQL 占位符对应的有序参数。
     *
     * @return 不可变参数列表
     */
    public List<Object> getParams() {
        return params;
    }

    /**
     * 获取 SQL 语句类型。
     *
     * <p>示例：SELECT 翻译结果返回 {@link SqlType#SELECT}，
     * UPDATE 翻译结果返回 {@link SqlType#UPDATE}。</p>
     *
     * @return SQL语句类型；出参不会为 {@code null}
     */
    public SqlType getSqlType() {
        return sqlType;
    }

    /**
     * 获取源查询模型。
     *
     * <p>示例：SELECT 翻译结果通常返回 {@code SelectQuery}，INSERT 翻译结果通常返回
     * {@code InsertQuery}。该对象主要用于拦截器和调试场景读取结构化信息。</p>
     *
     * @return 源查询模型；使用兼容构造方法或外部手工创建时可能为 {@code null}
     */
    public Object getStatement() {
        return statement;
    }

    /**
     * 返回 SQL 文本，便于日志和调试输出。
     *
     * @return SQL文本
     */
    @Override
    public String toString() {
        return sql;
    }
}
