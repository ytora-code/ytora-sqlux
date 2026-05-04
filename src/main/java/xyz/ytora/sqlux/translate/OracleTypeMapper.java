package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.type.Binary;
import xyz.ytora.sqlux.orm.type.CustBean;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Text;
import xyz.ytora.sqlux.orm.type.Uuid;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * Oracle类型映射器。
 *
 * <p>Oracle 没有原生 boolean 类型，数值和大文本、二进制类型也与通用 SQL 存在差异。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class OracleTypeMapper extends DefaultTypeMapper {

    @Override
    public String sqlType(ColumnType standardType) {
        if (standardType == null) {
            throw new IllegalArgumentException("标准列类型不能为空");
        }
        switch (standardType) {
            case AUTO:
                throw new IllegalArgumentException("AUTO 不是可直接翻译的标准列类型");
            case INT1:
                return "number(3)";
            case INT2:
                return "number(5)";
            case INT4:
                return "number(10)";
            case INT8:
                return "number(19)";
            case BOOLEAN:
                return "number(1)";
            case VARCHAR16:
                return "varchar2(16)";
            case VARCHAR32:
                return "varchar2(32)";
            case VARCHAR36:
                return "varchar2(36)";
            case VARCHAR64:
                return "varchar2(64)";
            case VARCHAR128:
                return "varchar2(128)";
            case VARCHAR256:
                return "varchar2(256)";
            case VARCHAR512:
                return "varchar2(512)";
            case VARCHAR1024:
                return "varchar2(1024)";
            case TEXT:
                return "clob";
            case JSON:
                return "clob";
            case DATE:
                return "date";
            case TIME:
                return "varchar2(16)";
            case DATETIME:
                return "timestamp";
            case BLOB:
                return "blob";
            case UUID:
                return "char(36)";
            case DECIMAL:
            case DECIMAL_19_4:
                return "number(19, 4)";
            default:
                return super.sqlType(standardType);
        }
    }

    @Override
    public String sqlType(Class<?> javaType) {
        if (javaType == String.class || javaType == Character.class || javaType == char.class) {
            return "varchar2(255)";
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
        if (javaType == Boolean.class || javaType == boolean.class || javaType == LogicDelete.class) {
            return sqlType(ColumnType.BOOLEAN);
        }
        if (javaType == Float.class || javaType == float.class) {
            return "binary_float";
        }
        if (javaType == Double.class || javaType == double.class) {
            return "binary_double";
        }
        if (javaType == BigDecimal.class) {
            return "number(19, 4)";
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
        if (javaType == byte[].class || javaType == Byte[].class || javaType == Binary.class) {
            return sqlType(ColumnType.BLOB);
        }
        if (javaType == Json.class || javaType == CustBean.class || javaType == Text.class) {
            return sqlType(ColumnType.TEXT);
        }
        if (javaType == Uuid.class) {
            return sqlType(ColumnType.UUID);
        }
        return super.sqlType(javaType);
    }
}
