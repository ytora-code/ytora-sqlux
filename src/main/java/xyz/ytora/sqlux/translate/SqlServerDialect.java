package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.delete.SqlServerDeleteTranslator;
import xyz.ytora.sqlux.translate.insert.InsertTranslator;
import xyz.ytora.sqlux.translate.select.SqlServerSelectTranslator;
import xyz.ytora.sqlux.translate.update.SqlServerUpdateTranslator;

/**
 * SQL Server SQL方言。
 *
 * <p>SQL Server 使用方括号引用标识符，支持 {@code UPDATE ... FROM ... JOIN ...}
 * 和单目标 {@code DELETE ... FROM ... JOIN ...}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerDialect implements Dialect {

    private final ExpressionTranslator expressionTranslator;

    private final SqlTranslator<SelectQuery> selectTranslator;

    private final SqlTranslator<InsertQuery> insertTranslator;

    private final SqlTranslator<UpdateQuery> updateTranslator;

    private final SqlTranslator<DeleteQuery> deleteTranslator;

    private final TypeMapper typeMapper;

    private final DdlDialect ddlDialect;

    public SqlServerDialect() {
        this.expressionTranslator = new ExpressionTranslator();
        this.selectTranslator = new SqlServerSelectTranslator(this);
        this.insertTranslator = new InsertTranslator(this);
        this.updateTranslator = new SqlServerUpdateTranslator(this);
        this.deleteTranslator = new SqlServerDeleteTranslator(this);
        this.typeMapper = new SqlServerTypeMapper();
        this.ddlDialect = new UnsupportedDdlDialect(typeMapper);
    }

    @Override
    public DbType dbType() {
        return DbType.SQLSERVER;
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
        return "[" + identifier.replace("]", "]]") + "]";
    }

    @Override
    public String placeholder(int index) {
        return "?";
    }

    @Override
    public boolean supportsUpdateJoin() {
        return true;
    }

    @Override
    public boolean supportsDeleteJoin() {
        return true;
    }
}
