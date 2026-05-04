package xyz.ytora.sqlux.translate.update;

import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.sql.model.Assignment;
import xyz.ytora.sqlux.sql.model.ColumnIncrement;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.ExpressionTranslator;
import xyz.ytora.sqlux.util.SqlRenderUtil;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.SqlTranslator;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.TranslateContext;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL UPDATE翻译器。
 *
 * <p>PostgreSQL 多表更新不支持 MySQL 的 {@code UPDATE a JOIN b ... SET ...} 写法，
 * 需要改写为 {@code UPDATE a SET ... FROM b WHERE ...}。该翻译器专门承载 PostgreSQL UPDATE
 * 语法，避免通用 UPDATE 翻译器混入数据库判断。</p>
 *
 * <p>使用示例：{@code new PostgreSqlUpdateTranslator(dialect).translate(query)}。
 * 输入说明：传入 UPDATE 查询模型。输出说明：返回 PostgreSQL 可执行 SQL 和有序参数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PostgreSqlUpdateTranslator implements SqlTranslator<UpdateQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    /**
     * 创建 PostgreSQL UPDATE 翻译器。
     *
     * @param dialect PostgreSQL方言；入参提供标识符引用、占位符和表达式翻译能力
     */
    public PostgreSqlUpdateTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    /**
     * 翻译 UPDATE 查询模型。
     *
     * @param query UPDATE查询模型；入参必须包含目标表和至少一个 SET 赋值项
     * @return PostgreSQL UPDATE翻译结果；出参包含 SQL、参数、SQL类型和源模型
     */
    @Override
    public SqlResult translate(UpdateQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("UPDATE查询不能为空");
        }
        if (query.getAssignments().isEmpty()) {
            throw new IllegalStateException("UPDATE缺少SET阶段");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(SqlRenderUtil.table(query.getTable(), dialect));
        appendSets(sql, query, context);
        appendFrom(sql, query, context);
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.UPDATE, query);
    }

    /**
     * 拼接 SET 子句。
     *
     * <p>PostgreSQL UPDATE 目标列在 SET 左侧不带目标表别名，因此这里只渲染字段名。</p>
     *
     * @param sql SQL构造器；入参会被追加 SET 子句
     * @param query UPDATE查询模型；入参提供赋值项
     * @param context 翻译上下文；入参用于收集参数
     */
    private void appendSets(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        List<String> items = new ArrayList<>();
        for (Assignment assignment : query.getAssignments()) {
            String value;
            if (assignment.getValue() instanceof ColumnIncrement) {
                ColumnIncrement increment = (ColumnIncrement) assignment.getValue();
                value = dialect.quoteIdentifier(assignment.getColumn().getColumnName()) + " + " + increment.getStep();
            } else {
                value = assignment.isRaw()
                        ? String.valueOf(assignment.getValue())
                        : SqlRenderUtil.value(assignment.getValue(), context);
            }
            items.add(dialect.quoteIdentifier(assignment.getColumn().getColumnName()) + " = " + value);
        }
        sql.append(" SET ").append(SqlRenderUtil.join(items, ", "));
    }

    /**
     * 拼接 FROM 子句。
     *
     * @param sql SQL构造器；入参在存在 JOIN 时会被追加 FROM 子句
     * @param query UPDATE查询模型；入参提供 JOIN 表
     */
    private void appendFrom(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        if (query.getJoins().isEmpty()) {
            return;
        }
        List<String> items = new ArrayList<>();
        for (JoinClause join : query.getJoins()) {
            assertInnerJoin(join);
            items.add(SqlRenderUtil.source(join.getTable(), context));
        }
        sql.append(" FROM ").append(SqlRenderUtil.join(items, ", "));
    }

    /**
     * 拼接 WHERE 子句。
     *
     * <p>JOIN 的 ON 条件会作为 PostgreSQL UPDATE FROM 的关联条件写入 WHERE，并与用户 WHERE 条件合并。</p>
     *
     * @param sql SQL构造器；入参会被追加 WHERE 子句
     * @param query UPDATE查询模型；入参提供 JOIN ON 和 WHERE 条件
     * @param context 翻译上下文；入参用于收集参数
     */
    private void appendWhere(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        List<String> conditions = new ArrayList<>();
        for (JoinClause join : query.getJoins()) {
            assertInnerJoin(join);
            conditions.add(expressionTranslator.translate(join.getOn(), context));
        }
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            conditions.add(where);
        }
        if (query.hasVersionLock()) {
            conditions.add(SqlRenderUtil.column(query.getVersionColumn(), context)
                    + " = " + context.addParam(query.getVersionValue()));
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(SqlRenderUtil.join(conditions, " AND "));
        }
    }

    /**
     * 校验 JOIN 类型。
     *
     * @param join JOIN子句；入参必须是 INNER JOIN
     */
    private void assertInnerJoin(JoinClause join) {
        if (join.getJoinType() != JoinType.INNER_JOIN) {
            throw new UnsupportedOperationException("PostgreSQL UPDATE FROM暂只支持INNER JOIN改写");
        }
    }
}
