package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.type.Binary;
import xyz.ytora.sqlux.orm.type.CustBean;
import xyz.ytora.sqlux.orm.type.DateRange;
import xyz.ytora.sqlux.orm.type.DateTimeRange;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.TimeRange;
import xyz.ytora.sqlux.orm.type.Uuid;

/**
 * PostgreSQL类型映射器。
 *
 * <p>在默认映射基础上处理 PostgreSQL 专属字段类型，例如二进制字段使用 {@code bytea}。</p>
 *
 * <p>使用示例：{@code new PostgreSqlTypeMapper().sqlType(byte[].class)}。
 * 输入说明：传入 Java 类型。输出说明：返回 PostgreSQL 字段类型片段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PostgreSqlTypeMapper extends DefaultTypeMapper {

    @Override
    public String sqlType(ColumnType standardType) {
        if (standardType == null) {
            throw new IllegalArgumentException("标准列类型不能为空");
        }
        switch (standardType) {
            case JSON:
                return "jsonb";
            case DATETIME:
                return "timestamp";
            case BLOB:
                return "bytea";
            case UUID:
                return "uuid";
            default:
                return super.sqlType(standardType);
        }
    }

    /**
     * 将 Java 类型映射为 PostgreSQL 字段类型。
     *
     * @param javaType Java字段类型；入参不能为 {@code null}
     * @return PostgreSQL字段类型；出参可用于 DDL 字段定义
     */
    @Override
    public String sqlType(Class<?> javaType) {
        if (javaType == byte[].class || javaType == Byte[].class) {
            return sqlType(ColumnType.BLOB);
        }
        if (javaType == Binary.class) {
            return sqlType(ColumnType.BLOB);
        }
        if (javaType == Json.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == CustBean.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == Uuid.class) {
            return sqlType(ColumnType.UUID);
        }
        if (javaType == DateRange.class) {
            return "daterange";
        }
        if (javaType == DateTimeRange.class) {
            return "tsrange";
        }
        if (javaType == TimeRange.class) {
            return "varchar(32)";
        }
        return super.sqlType(javaType);
    }
}
