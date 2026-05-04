package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.orm.type.Version;
import xyz.ytora.sqlux.rw.SqlFieldReader;
import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;
import xyz.ytora.sqlux.rw.TypeHandlers;
import xyz.ytora.toolkit.convert.Converts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ORM 字段值转换工具。
 *
 * <p>集中处理数据库读取转换、写入转换和基础类型转换，映射流程无需关心具体类型细节。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class OrmValueConverter {

    private OrmValueConverter() {
    }

    static Object read(EntityFieldMeta field, Object value) {
        Object handled = TypeHandlers.read(value, field.getField());
        if (handled != value) {
            return handled;
        }
        if (SqlFieldReader.class.isAssignableFrom(field.getField().getType())) {
            SqlFieldReader reader = (SqlFieldReader) OrmReflection.newInstance(field.getField().getType());
            return reader.read(value, field.getField());
        }
        if (!SqlReader.class.isAssignableFrom(field.getField().getType())) {
            return convert(value, field.getField().getType());
        }
        SqlReader reader = (SqlReader) OrmReflection.newInstance(field.getField().getType());
        return reader.read(value);
    }

    static Object write(EntityFieldMeta field, Object value) {
        if (value == null && field.getField().getType() == Version.class) {
            return Version.initial().write();
        }
        Object handled = TypeHandlers.write(value, field.getField());
        if (handled != value) {
            return handled;
        }
        if (value instanceof SqlWriter) {
            return ((SqlWriter) value).write();
        }
        if (value == null && SqlWriter.class.isAssignableFrom(field.getField().getType())) {
            SqlWriter writer = (SqlWriter) OrmReflection.newInstance(field.getField().getType());
            return writer.write();
        }
        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }
        return value;
    }

    static Object convert(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        try {
            // 先尝试类型转换
            return Converts.convert(value, targetType);
        } catch (Exception ignore) {

        }

        if (targetType == LocalDate.class) {
            return convertLocalDate(value);
        }
        if (targetType == LocalDateTime.class) {
            return convertLocalDateTime(value);
        }
        if (targetType == LocalTime.class) {
            return convertLocalTime(value);
        }
        if (CharSequence.class.isAssignableFrom(targetType) || targetType.equals(Character.class)) {
            return String.valueOf(value);
        }
        Class<?> boxedType = box(targetType);
        if (boxedType.isInstance(value)) {
            return value;
        }
        if (boxedType.isEnum()) {
            return convertEnumValue(value, boxedType);
        }
        if (Number.class.isAssignableFrom(boxedType) && value instanceof Number) {
            Number number = (Number) value;
            if (boxedType.equals(Integer.class)) {
                return number.intValue();
            }
            if (boxedType.equals(Long.class)) {
                return number.longValue();
            }
            if (boxedType.equals(Short.class)) {
                return number.shortValue();
            }
            if (boxedType.equals(Byte.class)) {
                return number.byteValue();
            }
            if (boxedType.equals(Float.class)) {
                return number.floatValue();
            }
            if (boxedType.equals(Double.class)) {
                return number.doubleValue();
            }
            if (boxedType.equals(BigDecimal.class)) {
                return new BigDecimal(number.toString());
            }
            if (boxedType.equals(BigInteger.class)) {
                return BigInteger.valueOf(number.longValue());
            }
        }
        return value;
    }

    private static Object convertLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return value;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().toLocalDate();
        }
        if (value instanceof CharSequence) {
            return LocalDate.parse(String.valueOf(value));
        }
        return value;
    }

    private static Object convertLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        }
        if (value instanceof CharSequence) {
            return LocalDateTime.parse(String.valueOf(value));
        }
        return value;
    }

    private static Object convertLocalTime(Object value) {
        if (value instanceof LocalTime) {
            return value;
        }
        if (value instanceof java.sql.Time) {
            return ((java.sql.Time) value).toLocalTime();
        }
        if (value instanceof CharSequence) {
            return LocalTime.parse(String.valueOf(value));
        }
        return value;
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type.equals(int.class)) {
            return Integer.class;
        }
        if (type.equals(long.class)) {
            return Long.class;
        }
        if (type.equals(short.class)) {
            return Short.class;
        }
        if (type.equals(byte.class)) {
            return Byte.class;
        }
        if (type.equals(float.class)) {
            return Float.class;
        }
        if (type.equals(double.class)) {
            return Double.class;
        }
        if (type.equals(boolean.class)) {
            return Boolean.class;
        }
        if (type.equals(char.class)) {
            return Character.class;
        }
        return type;
    }

    private static Object convertEnumValue(Object value, Class<?> enumType) {
        String name = String.valueOf(value);
        Object[] constants = enumType.getEnumConstants();
        for (Object constant : constants) {
            Enum<?> enumValue = (Enum<?>) constant;
            if (enumValue.name().equalsIgnoreCase(name)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("枚举值无法匹配: " + enumType.getName() + "." + name);
    }
}
