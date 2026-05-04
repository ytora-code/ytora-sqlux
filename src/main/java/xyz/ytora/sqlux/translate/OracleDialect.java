package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.delete.OracleDeleteTranslator;
import xyz.ytora.sqlux.translate.insert.InsertTranslator;
import xyz.ytora.sqlux.translate.select.FetchSelectTranslator;
import xyz.ytora.sqlux.translate.update.OracleUpdateTranslator;

/**
 * Oracle SQL方言。
 *
 * <p>Oracle 使用双引号引用标识符，JDBC 参数占位符使用 {@code ?}，分页使用
 * {@code OFFSET/FETCH} 语法。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OracleDialect implements Dialect {

    private final ExpressionTranslator expressionTranslator;

    private final SqlTranslator<SelectQuery> selectTranslator;

    private final SqlTranslator<InsertQuery> insertTranslator;

    private final SqlTranslator<UpdateQuery> updateTranslator;

    private final SqlTranslator<DeleteQuery> deleteTranslator;

    private final TypeMapper typeMapper;

    private final DdlDialect ddlDialect;

    public OracleDialect() {
        this.expressionTranslator = new ExpressionTranslator();
        this.selectTranslator = new FetchSelectTranslator(this);
        this.insertTranslator = new InsertTranslator(this);
        this.updateTranslator = new OracleUpdateTranslator(this);
        this.deleteTranslator = new OracleDeleteTranslator(this);
        this.typeMapper = new OracleTypeMapper();
        this.ddlDialect = new UnsupportedDdlDialect(typeMapper);
    }

    @Override
    public DbType dbType() {
        return DbType.ORACLE;
    }

    @Override
    public SqlTranslator<SelectQuery> selectTranslator() {
        return selectTranslator;
    }

    @Override
    public SqlTranslator<InsertQuery> insertTranslator() {
        return insertTranslator;
    }

    @Override
    public SqlTranslator<UpdateQuery> updateTranslator() {
        return updateTranslator;
    }

    @Override
    public SqlTranslator<DeleteQuery> deleteTranslator() {
        return deleteTranslator;
    }

    @Override
    public ExpressionTranslator expressionTranslator() {
        return expressionTranslator;
    }

    @Override
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    @Override
    public DdlDialect ddlDialect() {
        return ddlDialect;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    public String placeholder(int index) {
        return "?";
    }
}
