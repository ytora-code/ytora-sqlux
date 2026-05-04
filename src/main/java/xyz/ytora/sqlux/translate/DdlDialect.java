package xyz.ytora.sqlux.translate;

/**
 * DDL方言扩展点。
 *
 * <p>该接口用于承载后续 CREATE TABLE、DROP TABLE、索引、字段变更等数据库专属 DDL 能力。
 * 当前核心库暂未定义完整 DDL 模型，因此先暴露类型映射和能力标记，避免未来把 DDL 逻辑混入 DML 翻译器。</p>
 *
 * <p>使用示例：{@code dialect.ddlDialect().typeMapper().sqlType(String.class)}。
 * 输入说明：通过字段 Java 类型查询 DDL 类型。输出说明：返回当前数据库字段类型。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface DdlDialect {

    /**
     * 判断当前 DDL 方言是否已经提供完整 DDL 语句翻译能力。
     *
     * @return 已支持完整 DDL 翻译时返回 {@code true}；当前默认返回 {@code false}
     */
    boolean supportsDdl();

    /**
     * 获取 DDL 类型映射器。
     *
     * @return 类型映射器；出参用于 CREATE TABLE 等功能生成字段类型
     */
    TypeMapper typeMapper();
}
