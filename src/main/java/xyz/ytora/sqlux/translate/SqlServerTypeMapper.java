package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.type.Binary;
import xyz.ytora.sqlux.orm.type.CustBean;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Text;
import xyz.ytora.sqlux.orm.type.Uuid;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * SQL Server类型映射器。
 *
 * <p>SQL Server 使用 {@code nvarchar} 保存文本，使用 {@code bit} 表示布尔值，
 * 使用 {@code varbinary(max)} 保存二进制大字段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class SqlServerTypeMapper extends DefaultTypeMapper {

    @Override
    public String sqlType(ColumnType standardType) {
        if (standardType == null) {
            throw new IllegalArgumentException("标准列类型不能为空");
        }
        switch (standardType) {
            case AUTO:
                throw new IllegalArgumentException("AUTO 不是可直接翻译的标准列类型");
            case INT1:
                return "tinyint";
            case INT2:
                return "smallint";
            case INT4:
                return "int";
            case INT8:
                return "bigint";
            case BOOLEAN:
                return "bit";
            case VARCHAR16:
                return "nvarchar(16)";
            case VARCHAR32:
                return "nvarchar(32)";
            case VARCHAR36:
                return "nvarchar(36)";
            case VARCHAR64:
                return "nvarchar(64)";
            case VARCHAR128:
                return "nvarchar(128)";
            case VARCHAR256:
                return "nvarchar(256)";
            case VARCHAR512:
                return "nvarchar(512)";
            case VARCHAR1024:
                return "nvarchar(1024)";
            case TEXT:
                return "nvarchar(max)";
            case JSON:
                return "nvarchar(max)";
            case DATE:
                return "date";
            case TIME:
                return "time";
            case DATETIME:
                return "datetime2";
            case BLOB:
                return "varbinary(max)";
            case UUID:
                return "uniqueidentifier";
            case DECIMAL:
            case DECIMAL_19_4:
                return "decimal(19, 4)";
            default:
                return super.sqlType(standardType);
        }
    }

    @Override
    public String sqlType(Class<?> javaType) {
        if (javaType == String.class || javaType == Character.class || javaType == char.class) {
            return "nvarchar(255)";
        }
        if (javaType == Boolean.class || javaType == boolean.class || javaType == LogicDelete.class) {
            return sqlType(ColumnType.BOOLEAN);
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
