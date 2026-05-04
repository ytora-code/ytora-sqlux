package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;

/**
 * 达梦 SQL方言。
 *
 * <p>达梦与 Oracle 语法接近，当前复用 Oracle 的 SQL 翻译器，只替换数据库类型和类型映射器。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DmDialect extends OracleDialect {

    private final TypeMapper typeMapper = new DmTypeMapper();

    private final DdlDialect ddlDialect = new UnsupportedDdlDialect(typeMapper);

    @Override
    public DbType dbType() {
        return DbType.DM;
    }

    @Override
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    @Override
    public DdlDialect ddlDialect() {
        return ddlDialect;
    }
}
