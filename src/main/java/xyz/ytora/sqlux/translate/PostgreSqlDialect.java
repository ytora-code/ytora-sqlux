package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.delete.PostgreSqlDeleteTranslator;
import xyz.ytora.sqlux.translate.insert.InsertTranslator;
import xyz.ytora.sqlux.translate.select.SelectTranslator;
import xyz.ytora.sqlux.translate.update.PostgreSqlUpdateTranslator;

/**
 * PostgreSQL SQL方言。
 *
 * <p>PostgreSQL 使用双引号引用表名和字段名，JDBC 参数占位符使用 {@code ?}。多表 UPDATE
 * 使用 {@code UPDATE ... SET ... FROM ... WHERE ...}，多表 DELETE 使用 {@code DELETE FROM ... USING ...}，
 * 因此提供 PostgreSQL 专属 UPDATE/DELETE 翻译器。</p>
 *
 * <p>使用示例：{@code DialectFactory.getDialect(DbType.POSTGRESQL).updateTranslator().translate(query)}。
 * 输入说明：传入 UPDATE 模型。输出说明：返回 PostgreSQL 风格 SQL。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PostgreSqlDialect implements Dialect {

    private final ExpressionTranslator expressionTranslator;

    private final SqlTranslator<SelectQuery> selectTranslator;

    private final SqlTranslator<InsertQuery> insertTranslator;

    private final SqlTranslator<UpdateQuery> updateTranslator;

    private final SqlTranslator<DeleteQuery> deleteTranslator;

    private final TypeMapper typeMapper;

    private final DdlDialect ddlDialect;

    /**
     * 创建 PostgreSQL 方言并初始化可复用翻译器。
     */
    public PostgreSqlDialect() {
        this.expressionTranslator = new ExpressionTranslator();
        this.selectTranslator = new SelectTranslator(this);
        this.insertTranslator = new InsertTranslator(this);
        this.updateTranslator = new PostgreSqlUpdateTranslator(this);
        this.deleteTranslator = new PostgreSqlDeleteTranslator(this);
        this.typeMapper = new PostgreSqlTypeMapper();
        this.ddlDialect = new UnsupportedDdlDialect(typeMapper);
    }

    /**
     * 获取数据库类型。
     *
     * @return 固定返回 {@link DbType#POSTGRESQL}
     */
    @Override
    public DbType dbType() {
        return DbType.POSTGRESQL;
    }

    /**
     * 获取 SELECT 翻译器。
     *
     * @return PostgreSQL SELECT翻译器
     */
    @Override
    public SqlTranslator<SelectQuery> selectTranslator() {
        return selectTranslator;
    }

    /**
     * 获取 INSERT 翻译器。
     *
     * @return PostgreSQL INSERT翻译器
     */
    @Override
    public SqlTranslator<InsertQuery> insertTranslator() {
        return insertTranslator;
    }

    /**
     * 获取 UPDATE 翻译器。
     *
     * @return PostgreSQL UPDATE翻译器
     */
    @Override
    public SqlTranslator<UpdateQuery> updateTranslator() {
        return updateTranslator;
    }

    /**
     * 获取 DELETE 翻译器。
     *
     * @return PostgreSQL DELETE翻译器
     */
    @Override
    public SqlTranslator<DeleteQuery> deleteTranslator() {
        return deleteTranslator;
    }

    /**
     * 获取表达式翻译器。
     *
     * @return PostgreSQL 表达式翻译器
     */
    @Override
    public ExpressionTranslator expressionTranslator() {
        return expressionTranslator;
    }

    /**
     * 获取类型映射器。
     *
     * @return PostgreSQL 类型映射器
     */
    @Override
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    /**
     * 获取 DDL 扩展点。
     *
     * @return PostgreSQL DDL方言扩展点
     */
    @Override
    public DdlDialect ddlDialect() {
        return ddlDialect;
    }

    /**
     * 使用双引号引用 PostgreSQL 标识符。
     *
     * @param identifier 表名或字段名；入参为空时原样返回
     * @return PostgreSQL标识符；例如 {@code user} 返回 {@code "user"}
     */
    @Override
    public String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /**
     * 生成 JDBC 参数占位符。
     *
     * @param index 参数序号；当前 JDBC 翻译使用 {@code ?}
     * @return 固定返回 {@code ?}
     */
    @Override
    public String placeholder(int index) {
        return "?";
    }
}
