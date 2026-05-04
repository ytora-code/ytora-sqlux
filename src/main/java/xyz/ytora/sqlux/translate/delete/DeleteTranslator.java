package xyz.ytora.sqlux.translate.delete;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.JoinClause;
import xyz.ytora.sqlux.sql.model.TableRef;
import xyz.ytora.sqlux.translate.*;
import xyz.ytora.sqlux.util.SqlRenderUtil;

/**
 * DELETE翻译器。
 *
 * @author ytora
 * @since 1.0
 */
public class DeleteTranslator implements SqlTranslator<DeleteQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    /**
     * 使用全局默认数据库类型创建 DELETE 翻译器。
     */
    public DeleteTranslator() {
        this(DialectFactory.getDialect(SQL.getSqluxGlobal().getDefaultDbType()));
    }

    /**
     * 使用指定方言创建 DELETE 翻译器。
     *
     * @param dialect 数据库方言，用于标识符转义、JOIN 能力判断和条件翻译
     */
    public DeleteTranslator(Dialect dialect) {
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

    /**
     * 将 DELETE 查询模型翻译为 SQL 和有序参数。
     *
     * @param query DELETE 查询模型
     * @return SQL 翻译结果
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
        if (!query.getJoins().isEmpty() && !dialect.supportsDeleteJoin()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持DELETE JOIN翻译");
        }
        if (!query.getDeleteTargets().isEmpty() && !dialect.supportsMultiTableDelete()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持多表DELETE目标翻译");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE ");
        appendDeleteTargets(sql, query, context);
        sql.append("FROM ").append(SqlRenderUtil.table(query.getFrom(), dialect));
        appendJoins(sql, query, context);
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
                .append(SqlRenderUtil.column(ColumnRef.of(query.getFrom().getTableClass(), logicDeleteColumn), context))
                .append(" = ")
                .append(context.addParam(1));
        appendWhere(sql, query, context);
        return new SqlResult(sql.toString(), context.getParams(), SqlType.DELETE, query);
    }

    /**
     * 追加 MySQL 风格多表 DELETE 的删除目标。
     *
     * @param sql SQL 拼接缓冲区
     * @param query DELETE 查询模型
     * @param context 翻译上下文，用于根据实体类型查找表别名
     */
    private void appendDeleteTargets(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        if (query.getDeleteTargets().isEmpty()) {
            return;
        }
        for (int i = 0; i < query.getDeleteTargets().size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            Class<?> target = query.getDeleteTargets().get(i);
            String alias = context.getStageContextHolder().getAlias(target);
            if (alias == null || alias.isEmpty()) {
                TableRef from = query.getFrom();
                if (from.getTableClass().equals(target)) {
                    alias = from.getAlias();
                }
            }
            if (alias == null || alias.isEmpty()) {
                throw new IllegalStateException("DELETE目标表未出现在FROM或JOIN上下文中: " + target.getName());
            }
            sql.append(alias);
        }
        sql.append(" ");
    }

    /**
     * 追加 DELETE JOIN 子句。
     *
     * @param sql SQL 拼接缓冲区
     * @param query DELETE 查询模型
     * @param context 翻译上下文
     */
    private void appendJoins(StringBuilder sql, DeleteQuery query, TranslateContext context) {
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
     * 追加 WHERE 子句。
     *
     * @param sql SQL 拼接缓冲区
     * @param query DELETE 查询模型
     * @param context 翻译上下文
     */
    private void appendWhere(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
    }
}
