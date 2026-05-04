package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.type.CustBean;
import xyz.ytora.sqlux.orm.type.DateRange;
import xyz.ytora.sqlux.orm.type.DateTimeRange;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.TimeRange;
import xyz.ytora.sqlux.orm.type.Uuid;

/**
 * MySQL类型映射器。
 *
 * <p>在默认映射基础上处理 MySQL 专属字段类型，例如二进制字段使用 {@code blob}。</p>
 *
 * <p>使用示例：{@code new MysqlTypeMapper().sqlType(Boolean.class)}。
 * 输入说明：传入 Java 类型。输出说明：返回 MySQL 字段类型片段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class MysqlTypeMapper extends DefaultTypeMapper {

    @Override
    public String sqlType(ColumnType standardType) {
        if (standardType == null) {
            throw new IllegalArgumentException("标准列类型不能为空");
        }
        switch (standardType) {
            case INT1:
                return "tinyint";
            case INT2:
                return "smallint";
            case INT4:
                return "int";
            case INT8:
                return "bigint";
            case BOOLEAN:
                return "tinyint(1)";
            case JSON:
                return "json";
            case DATETIME:
                return "datetime";
            case UUID:
                return "char(36)";
            default:
                return super.sqlType(standardType);
        }
    }

    /**
     * 将 Java 类型映射为 MySQL 字段类型。
     *
     * @param javaType Java字段类型；入参不能为 {@code null}
     * @return MySQL字段类型；出参可用于 DDL 字段定义
     */
    @Override
    public String sqlType(Class<?> javaType) {
        if (javaType == Json.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == CustBean.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == Uuid.class) {
            return sqlType(ColumnType.UUID);
        }
        if (javaType == LogicDelete.class) {
            return sqlType(ColumnType.BOOLEAN);
        }
        if (javaType == DateRange.class) {
            return "varchar(32)";
        }
        if (javaType == DateTimeRange.class) {
            return "varchar(64)";
        }
        if (javaType == TimeRange.class) {
            return "varchar(32)";
        }
        return super.sqlType(javaType);
    }
}
