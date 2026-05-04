package xyz.ytora.sqlux.orm.filler;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 自动填充值转换工具。
 *
 * @author ytora
 * @since 1.0
 */
final class OrmFillValueConverter {

    private OrmFillValueConverter() {
    }

    static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        Class<?> boxedType = box(targetType);
        if (boxedType.isInstance(value)) {
            return value;
        }
        if (CharSequence.class.isAssignableFrom(boxedType)) {
            return String.valueOf(value);
        }
        if (Number.class.isAssignableFrom(boxedType) && value instanceof Number) {
            return convertNumber((Number) value, boxedType);
        }
        return value;
    }

    private static Object convertNumber(Number number, Class<?> boxedType) {
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
        return number;
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
}
