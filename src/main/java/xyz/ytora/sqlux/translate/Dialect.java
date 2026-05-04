package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.InsertQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.UpdateQuery;

/**
 * SQL方言接口。
 *
 * <p>方言是数据库能力的统一入口，不只处理标识符引用和占位符，也负责提供当前数据库对应的
 * SELECT、INSERT、UPDATE、DELETE 翻译器，以及表达式、类型映射和 DDL 扩展点。不同数据库语法差异较大时，
 * 应优先通过替换语句级翻译器解决，而不是在通用翻译器中堆叠数据库判断。</p>
 *
 * <p>使用示例：{@code DialectFactory.getDialect(DbType.POSTGRESQL).updateTranslator().translate(query)}。
 * 输入说明：传入结构化 SQL 模型。输出说明：返回当前数据库语法下的 {@link SqlResult}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface Dialect {

    /**
     * 获取当前方言对应的数据库类型。
     *
     * @return 数据库类型；出参用于日志、拦截器和能力判断
     */
    DbType dbType();

    /**
     * 获取 SELECT 翻译器。
     *
     * @return SELECT翻译器；出参负责把 {@link SelectQuery} 翻译为当前数据库 SQL
     */
    SqlTranslator<SelectQuery> selectTranslator();

    /**
     * 获取 INSERT 翻译器。
     *
     * @return INSERT翻译器；出参负责把 {@link InsertQuery} 翻译为当前数据库 SQL
     */
    SqlTranslator<InsertQuery> insertTranslator();

    /**
     * 获取 UPDATE 翻译器。
     *
     * @return UPDATE翻译器；出参负责处理当前数据库 UPDATE 语法，包括多表更新差异
     */
    SqlTranslator<UpdateQuery> updateTranslator();

    /**
     * 获取 DELETE 翻译器。
     *
     * @return DELETE翻译器；出参负责处理当前数据库 DELETE 语法，包括 USING/JOIN 等差异
     */
    SqlTranslator<DeleteQuery> deleteTranslator();

    /**
     * 获取表达式翻译器。
     *
     * <p>示例：MySQL 正则可以翻译为 {@code REGEXP}，PostgreSQL 正则可以翻译为 {@code ~}。
     * 当前表达式能力尚未覆盖正则，先保留独立扩展点。</p>
     *
     * @return 表达式翻译器；出参用于 WHERE、ON、HAVING 等条件翻译
     */
    ExpressionTranslator expressionTranslator();

    /**
     * 获取 Java 类型到数据库字段类型的映射器。
     *
     * <p>该扩展点服务于后续自动建表、DDL 生成等功能。</p>
     *
     * @return 类型映射器；出参用于把 Java 字段类型翻译为当前数据库字段类型
     */
    TypeMapper typeMapper();

    /**
     * 获取 DDL 方言扩展点。
     *
     * <p>当前 DDL 尚未实现完整模型，先提供稳定入口，后续 CREATE TABLE、DROP TABLE、索引等功能
     * 都应挂到该对象下。</p>
     *
     * @return DDL方言；出参用于后续数据库专属 DDL 翻译
     */
    DdlDialect ddlDialect();

    /**
     * 引用数据库标识符。
     *
     * @param identifier 表名或字段名；入参为空时原样返回
     * @return 方言处理后的标识符；例如 MySQL 返回 {@code `user`}，PostgreSQL 返回 {@code "user"}
     */
    String quoteIdentifier(String identifier);

    /**
     * 生成参数占位符。
     *
     * @param index 参数序号，从 1 开始；入参可供 PostgreSQL 原生 {@code $1} 等风格扩展
     * @return 参数占位符；JDBC 方言通常返回 {@code ?}
     */
    String placeholder(int index);

    /**
     * 判断当前方言是否支持 UPDATE 语句直接拼接 JOIN。
     *
     * <p>示例：MySQL 支持 {@code UPDATE user u JOIN dept d ON ... SET ...}；
     * PostgreSQL 需要 {@code UPDATE ... FROM ...} 语法。</p>
     *
     * @return 支持 UPDATE JOIN 时返回 {@code true}
     */
    default boolean supportsUpdateJoin() {
        return false;
    }

    /**
     * 判断当前方言是否支持 DELETE 语句直接拼接 JOIN。
     *
     * <p>示例：MySQL 支持 {@code DELETE u FROM user u JOIN dept d ON ...}；
     * PostgreSQL 需要 {@code USING} 语法。</p>
     *
     * @return 支持 DELETE JOIN 时返回 {@code true}
     */
    default boolean supportsDeleteJoin() {
        return false;
    }

    /**
     * 判断当前方言是否支持一次 DELETE 删除多个目标表。
     *
     * <p>示例：MySQL 支持 {@code DELETE u,p FROM user u JOIN person p ON ...}；
     * PostgreSQL 不支持一次删除多个目标表。</p>
     *
     * @return 支持多表删除目标时返回 {@code true}
     */
    default boolean supportsMultiTableDelete() {
        return false;
    }
}
