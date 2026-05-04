package xyz.ytora.sqlux.translate.update;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.sql.model.Assignment;
import xyz.ytora.sqlux.sql.model.ColumnIncrement;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.*;
import xyz.ytora.sqlux.util.SqlRenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * UPDATE翻译器。
 *
 * @author ytora
 * @since 1.0
 */
public class UpdateTranslator implements SqlTranslator<UpdateQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    /**
     * 使用全局默认数据库类型创建 UPDATE 翻译器。
     */
    public UpdateTranslator() {
        this(DialectFactory.getDialect(SQL.getSqluxGlobal().getDefaultDbType()));
    }

    /**
     * 使用指定方言创建 UPDATE 翻译器。
     *
     * @param dialect 数据库方言，用于标识符转义、JOIN 能力判断和条件翻译
     */
    public UpdateTranslator(Dialect dialect) {
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    /**
     * 将 UPDATE 查询模型翻译为 SQL 和有序参数。
     *
     * @param query UPDATE 查询模型
     * @return SQL 翻译结果
     */
    @Override
    public SqlResult translate(UpdateQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("UPDATE查询不能为空");
        }
        if (query.getAssignments().isEmpty()) {
            throw new IllegalStateException("UPDATE缺少SET阶段");
        }
        if (!query.getJoins().isEmpty() && !dialect.supportsUpdateJoin()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持UPDATE JOIN翻译");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(SqlRenderUtil.table(query.getTable(), dialect));
        appendJoins(sql, query, context);
        appendSets(sql, query, context);
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.UPDATE, query);
    }

    /**
     * 追加 UPDATE JOIN 子句。
     *
     * @param sql SQL 拼接缓冲区
     * @param query UPDATE 查询模型
     * @param context 翻译上下文
     */
    private void appendJoins(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        for (JoinClause join : query.getJoins()) {
            sql.append(" ")
                    .append(join.getJoinType().getJoinKey())
                    .append(" ")
                    .append(SqlRenderUtil.source(join.getTable(), context))
                    .append(" ON ")
                    .append(expressionTranslator.translate(join.getOn(), context));
        }
    }

    /**
     * 追加 SET 赋值子句。
     *
     * @param sql SQL 拼接缓冲区
     * @param query UPDATE 查询模型
     * @param context 翻译上下文，用于收集 SET 参数
     */
    private void appendSets(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        List<String> items = new ArrayList<>();
        for (Assignment assignment : query.getAssignments()) {
            String value;
            if (assignment.getValue() instanceof ColumnIncrement) {
                ColumnIncrement increment = (ColumnIncrement) assignment.getValue();
                value = SqlRenderUtil.column(assignment.getColumn(), context) + " + " + increment.getStep();
            } else {
                value = assignment.isRaw()
                        ? String.valueOf(assignment.getValue())
                        : SqlRenderUtil.value(assignment.getValue(), context);
            }
            items.add(SqlRenderUtil.column(assignment.getColumn(), context) + " = " + value);
        }
        sql.append(" SET ").append(SqlRenderUtil.join(items, ", "));
    }

    /**
     * 追加 WHERE 子句。
     *
     * @param sql SQL 拼接缓冲区
     * @param query UPDATE 查询模型
     * @param context 翻译上下文
     */
    private void appendWhere(StringBuilder sql, UpdateQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
        if (query.hasVersionLock()) {
            if (where.isEmpty()) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }
            sql.append(SqlRenderUtil.column(query.getVersionColumn(), context))
                    .append(" = ")
                    .append(context.addParam(query.getVersionValue()));
        }
    }
}
