package xyz.ytora.sqlux.translate.delete;

import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.translate.Dialect;
import xyz.ytora.sqlux.translate.ExpressionTranslator;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.sqlux.translate.SqlTranslator;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.SqlRenderUtil;

/**
 * Oracle DELETE翻译器。
 *
 * <p>Oracle/达梦普通 DELETE 支持表别名，JOIN DELETE 暂不自动改写。存在逻辑删除字段时，
 * DELETE 会改写为不带 SET 别名前缀的 UPDATE。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OracleDeleteTranslator implements SqlTranslator<DeleteQuery> {

    private final Dialect dialect;

    private final ExpressionTranslator expressionTranslator;

    public OracleDeleteTranslator(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("SQL方言不能为空");
        }
        this.dialect = dialect;
        this.expressionTranslator = dialect.expressionTranslator();
    }

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
        if (!query.getJoins().isEmpty()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持DELETE JOIN翻译");
        }
        if (!query.getDeleteTargets().isEmpty()) {
            throw new UnsupportedOperationException("当前数据库方言暂不支持多表DELETE目标翻译");
        }
        TranslateContext context = new TranslateContext(dialect, query.getContextHolder());
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(SqlRenderUtil.table(query.getFrom(), dialect));
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

    private void appendWhere(StringBuilder sql, DeleteQuery query, TranslateContext context) {
        String where = expressionTranslator.translate(query.getWhere(), context);
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }
    }
}
