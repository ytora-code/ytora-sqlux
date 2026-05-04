package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;

/**
 * Java类型到数据库字段类型的映射器。
 *
 * <p>该接口为后续自动建表、字段同步和 DDL 生成预留扩展点。不同数据库可把同一个 Java 类型映射为
 * 不同字段类型，例如 {@code String.class} 在 MySQL 中通常为 {@code varchar(255)}，在 PostgreSQL 中可为
 * {@code varchar(255)} 或 {@code text}。</p>
 *
 * <p>使用示例：{@code dialect.typeMapper().sqlType(String.class)}。
 * 输入说明：传入 Java 字段类型。输出说明：返回当前数据库可识别的字段类型片段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface TypeMapper {

    /**
     * 将 Java 类型映射为数据库字段类型。
     *
     * @param javaType Java字段类型；入参不能为 {@code null}
     * @return 数据库字段类型；出参可直接用于 DDL 字段定义
     */
    String sqlType(Class<?> javaType);

    /**
     * 将 Sqlux 标准列类型映射为当前数据库的具体字段类型。
     *
     * @param standardType Sqlux 标准列类型
     * @return 当前数据库可执行的字段类型片段
     */
    String sqlType(ColumnType standardType);

}
