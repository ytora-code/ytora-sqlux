package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;
import xyz.ytora.sqlux.translate.delete.DeleteTranslator;
import xyz.ytora.sqlux.translate.insert.InsertTranslator;
import xyz.ytora.sqlux.translate.select.SelectTranslator;
import xyz.ytora.sqlux.translate.update.UpdateTranslator;

/**
 * MySQL SQL方言。
 *
 * <p>MySQL 使用反引号引用表名和字段名，JDBC 参数占位符使用 {@code ?}。MySQL 支持
 * {@code UPDATE ... JOIN ... SET ...} 和 {@code DELETE target FROM ... JOIN ...} 语法，
 * 因此复用通用 UPDATE/DELETE 翻译器即可。</p>
 *
 * <p>使用示例：{@code DialectFactory.getDialect(DbType.MYSQL).deleteTranslator().translate(query)}。
 * 输入说明：传入 DELETE 模型。输出说明：返回 MySQL 风格 SQL。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class MysqlDialect implements Dialect {

    private final ExpressionTranslator expressionTranslator;

    private final SqlTranslator<SelectQuery> selectTranslator;

    private final SqlTranslator<InsertQuery> insertTranslator;

    private final SqlTranslator<UpdateQuery> updateTranslator;

    private final SqlTranslator<DeleteQuery> deleteTranslator;

    private final TypeMapper typeMapper;

    private final DdlDialect ddlDialect;

    /**
     * 创建 MySQL 方言并初始化可复用翻译器。
     */
    public MysqlDialect() {
        this.expressionTranslator = new ExpressionTranslator();
        this.selectTranslator = new SelectTranslator(this);
        this.insertTranslator = new InsertTranslator(this);
        this.updateTranslator = new UpdateTranslator(this);
        this.deleteTranslator = new DeleteTranslator(this);
        this.typeMapper = new MysqlTypeMapper();
        this.ddlDialect = new UnsupportedDdlDialect(typeMapper);
    }

    /**
     * 获取数据库类型。
     *
     * @return 固定返回 {@link DbType#MYSQL}
     */
    @Override
    public DbType dbType() {
        return DbType.MYSQL;
    }

    /**
     * 获取 SELECT 翻译器。
     *
     * @return MySQL SELECT翻译器
     */
    @Override
    public SqlTranslator<SelectQuery> selectTranslator() {
        return selectTranslator;
    }

    /**
     * 获取 INSERT 翻译器。
     *
     * @return MySQL INSERT翻译器
     */
    @Override
    public SqlTranslator<InsertQuery> insertTranslator() {
        return insertTranslator;
    }

    /**
     * 获取 UPDATE 翻译器。
     *
     * @return MySQL UPDATE翻译器
     */
    @Override
    public SqlTranslator<UpdateQuery> updateTranslator() {
        return updateTranslator;
    }

    /**
     * 获取 DELETE 翻译器。
     *
     * @return MySQL DELETE翻译器
     */
    @Override
    public SqlTranslator<DeleteQuery> deleteTranslator() {
        return deleteTranslator;
    }

    /**
     * 获取表达式翻译器。
     *
     * @return MySQL 表达式翻译器
     */
    @Override
    public ExpressionTranslator expressionTranslator() {
        return expressionTranslator;
    }

    /**
     * 获取类型映射器。
     *
     * @return MySQL 类型映射器
     */
    @Override
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    /**
     * 获取 DDL 扩展点。
     *
     * @return MySQL DDL方言扩展点
     */
    @Override
    public DdlDialect ddlDialect() {
        return ddlDialect;
    }

    /**
     * 使用反引号引用 MySQL 标识符。
     *
     * @param identifier 表名或字段名；入参为空时原样返回
     * @return MySQL标识符；例如 {@code user} 返回 {@code `user`}
     */
    @Override
    public String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        return "`" + identifier.replace("`", "``") + "`";
    }

    /**
     * 生成 JDBC 参数占位符。
     *
     * @param index 参数序号；MySQL JDBC 不使用该值
     * @return 固定返回 {@code ?}
     */
    @Override
    public String placeholder(int index) {
        return "?";
    }

    /**
     * 判断是否支持 UPDATE JOIN。
     *
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean supportsUpdateJoin() {
        return true;
    }

    /**
     * 判断是否支持 DELETE JOIN。
     *
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean supportsDeleteJoin() {
        return true;
    }

    /**
     * 判断是否支持多目标 DELETE。
     *
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean supportsMultiTableDelete() {
        return true;
    }
}
