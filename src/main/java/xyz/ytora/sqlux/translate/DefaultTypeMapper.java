package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.type.Binary;
import xyz.ytora.sqlux.orm.type.CustBean;
import xyz.ytora.sqlux.orm.type.DateRange;
import xyz.ytora.sqlux.orm.type.DateTimeRange;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Text;
import xyz.ytora.sqlux.orm.type.TimeRange;
import xyz.ytora.sqlux.orm.type.Uuid;
import xyz.ytora.sqlux.orm.type.Version;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * 默认类型映射器。
 *
 * <p>提供一组保守的 Java 到 SQL 类型映射，供具体数据库方言复用或覆盖。该类不依赖 JDBC，
 * 只输出 DDL 中的字段类型片段。</p>
 *
 * <p>使用示例：{@code new DefaultTypeMapper().sqlType(Integer.class)}。
 * 输入说明：传入 Java 类型。输出说明：返回通用 SQL 类型，例如 {@code integer}。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DefaultTypeMapper implements TypeMapper {

    @Override
    public String sqlType(ColumnType standardType) {
        if (standardType == null) {
            throw new IllegalArgumentException("标准列类型不能为空");
        }
        switch (standardType) {
            case AUTO:
                throw new IllegalArgumentException("AUTO 不是可直接翻译的标准列类型");
            case INT1:
                return "smallint";
            case INT2:
                return "smallint";
            case INT4:
                return "integer";
            case INT8:
                return "bigint";
            case BOOLEAN:
                return "boolean";
            case VARCHAR16:
                return "varchar(16)";
            case VARCHAR32:
                return "varchar(32)";
            case VARCHAR36:
                return "varchar(36)";
            case VARCHAR64:
                return "varchar(64)";
            case VARCHAR128:
                return "varchar(128)";
            case VARCHAR256:
                return "varchar(256)";
            case VARCHAR512:
                return "varchar(512)";
            case VARCHAR1024:
                return "varchar(1024)";
            case TEXT:
                return "text";
            case JSON:
                return "text";
            case DATE:
                return "date";
            case TIME:
                return "time";
            case DATETIME:
                return "timestamp";
            case BLOB:
                return "blob";
            case UUID:
                return "varchar(36)";
            case DECIMAL:
            case DECIMAL_19_4:
                return "decimal(19, 4)";
            default:
                throw new IllegalArgumentException("不支持的标准列类型: " + standardType);
        }
    }

    /**
     * 将 Java 类型映射为通用 SQL 字段类型。
     *
     * @param javaType Java字段类型；入参不能为 {@code null}
     * @return SQL字段类型；出参为通用类型名称
     */
    @Override
    public String sqlType(Class<?> javaType) {
        if (javaType == null) {
            throw new IllegalArgumentException("Java类型不能为空");
        }
        if (javaType == String.class || javaType == Character.class || javaType == char.class) {
            return "varchar(255)";
        }
        if (javaType == Integer.class || javaType == int.class) {
            return sqlType(ColumnType.INT4);
        }
        if (javaType == Long.class || javaType == long.class) {
            return sqlType(ColumnType.INT8);
        }
        if (javaType == Short.class || javaType == short.class) {
            return sqlType(ColumnType.INT2);
        }
        if (javaType == Byte.class || javaType == byte.class) {
            return sqlType(ColumnType.INT1);
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return sqlType(ColumnType.BOOLEAN);
        }
        if (javaType == Float.class || javaType == float.class) {
            return "real";
        }
        if (javaType == Double.class || javaType == double.class) {
            return "double precision";
        }
        if (javaType == BigDecimal.class) {
            return "decimal(19, 4)";
        }
        if (javaType == java.sql.Date.class || javaType == LocalDate.class) {
            return sqlType(ColumnType.DATE);
        }
        if (javaType == LocalTime.class) {
            return sqlType(ColumnType.TIME);
        }
        if (javaType == Timestamp.class || javaType == LocalDateTime.class || Date.class.isAssignableFrom(javaType)) {
            return sqlType(ColumnType.DATETIME);
        }
        if (javaType == byte[].class || javaType == Byte[].class) {
            return sqlType(ColumnType.BLOB);
        }
        if (javaType == Json.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == CustBean.class) {
            return sqlType(ColumnType.JSON);
        }
        if (javaType == Text.class) {
            return sqlType(ColumnType.TEXT);
        }
        if (javaType == Binary.class) {
            return sqlType(ColumnType.BLOB);
        }
        if (javaType == Uuid.class) {
            return sqlType(ColumnType.UUID);
        }
        if (javaType == Version.class) {
            return sqlType(ColumnType.INT8);
        }
        if (javaType == LogicDelete.class) {
            return "smallint";
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
        return "varchar(255)";
    }
}
