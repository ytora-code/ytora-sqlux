package xyz.ytora.sqlux.translate.delete;

import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.JoinClause;
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
 * PostgreSQL DELETE翻译器。
 *
 * <p>PostgreSQL 多表删除不支持 MySQL 的 {@code DELETE t FROM ... JOIN ...} 写法，
 * 需要改写为 {@code DELETE FROM t USING other_table WHERE ...}。该翻译器专门承载 PostgreSQL DELETE
 * 语法差异。</p>
 *
 * <p>使用示例：{@code new PostgreSqlDeleteTranslator(dialect).translate(query)}。
 * 输入说明：传入 DELETE 查询模型。输出说明：返回 PostgreSQL 可执行 SQL 和有序参数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PostgreSqlDeleteTranslator implements SqlTranslator<DeleteQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    /**
     * 创建 PostgreSQL DELETE 翻译器。
     *
     * @param dialect PostgreSQL方言；入参提供标识符引用、占位符和表达式翻译能力
     */
    public PostgreSqlDeleteTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    /**
     * 翻译 DELETE 查询模型。
     *
     * @param query DELETE查询模型；入参必须包含 FROM 表
     * @return PostgreSQL DELETE翻译结果；出参包含 SQL、参数、SQL类型和源模型
     */
    @Override
    public SqlResult translate(DeleteQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("DELETE查询不能为空");
        }
        if (query.getFrom() == null) {
            throw new IllegalStateException("DELETE缺少FROM阶段");
        }
        String logicDeleteColumn = OrmMapper.findLogicDeleteColumn(query.getFrom().getTableClass());
        if (logicDeleteColumn != null) {
            return translateLogicDelete(query, logicDeleteColumn);
        }
        if (!isSingleFromTarget(query)) {
            throw new UnsupportedOperationException("PostgreSQL不支持一次DELETE删除多个目标表");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(SqlRenderUtil.table(query.getFrom(), dialect));
        appendUsing(sql, query, context);
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.DELETE, query);
    }

    private SqlResult translateLogicDelete(DeleteQuery query, String logicDeleteColumn) {
        if (!query.getDeleteTargets().isEmpty() || !query.getJoins().isEmpty()) {
            throw new UnsupportedOperationException("LogicDelete暂只支持普通单表DELETE自动改写");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ")
                .append(SqlRenderUtil.table(query.getFrom(), dialect))
                .append(" SET ")
                .append(dialect.quoteIdentifier(logicDeleteColumn))
                .append(" = ")
                .append(context.addParam(1));
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.DELETE, query);
    }

    /**
     * 拼接 USING 子句。
     *
     * @param sql SQL构造器；入参在存在 JOIN 时会被追加 USING 子句
     * @param query DELETE查询模型；入参提供 JOIN 表
     */
    private void appendUsing(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        if (query.getJoins().isEmpty()) {
            return;
        }
        List<String> items = new ArrayList<>();
        for (JoinClause join : query.getJoins()) {
            assertInnerJoin(join);
            items.add(SqlRenderUtil.source(join.getTable(), context));
        }
        sql.append(" USING ").append(SqlRenderUtil.join(items, ", "));
    }

    /**
     * 拼接 WHERE 子句。
     *
     * <p>JOIN 的 ON 条件会作为 PostgreSQL DELETE USING 的关联条件写入 WHERE，并与用户 WHERE 条件合并。</p>
     *
     * @param sql SQL构造器；入参会被追加 WHERE 子句
     * @param query DELETE查询模型；入参提供 JOIN ON 和 WHERE 条件
     * @param context 翻译上下文；入参用于收集参数
     */
    private void appendWhere(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        List<String> conditions = new ArrayList<>();
        for (JoinClause join : query.getJoins()) {
            assertInnerJoin(join);
            conditions.add(expressionTranslator.translate(join.getOn(), context));
        }
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            conditions.add(where);
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
            throw new UnsupportedOperationException("PostgreSQL DELETE USING暂只支持INNER JOIN改写");
        }
    }

    /**
     * 判断 DELETE 目标是否可在 PostgreSQL 中按普通 FROM 表处理。
     *
     * @param query DELETE查询模型；入参提供 DELETE 目标和 FROM 表
     * @return 未显式指定目标或目标等于 FROM 表时返回 {@code true}
     */
    private boolean isSingleFromTarget(DeleteQuery query) {
        return query.getDeleteTargets().isEmpty()
                || (query.getDeleteTargets().size() == 1
                && query.getDeleteTargets().get(0).equals(query.getFrom().getTableClass()));
    }
}
