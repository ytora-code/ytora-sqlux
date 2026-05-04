package xyz.ytora.sqlux.orm.type;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JSON 标量值转换工具。
 *
 * @author ytora
 * @since 1.0
 */
final class JsonScalars {

    private JsonScalars() {
    }

    static Number number(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof CharSequence) {
            return new BigDecimal(value.toString());
        }
        throw new IllegalArgumentException("[" + value.getClass().getName() + "]类型不能转为数字类型");
    }

    static BigDecimal bigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number || value instanceof CharSequence) {
            return new BigDecimal(value.toString());
        }
        throw new IllegalArgumentException("[" + value.getClass().getName() + "]类型不能转为BigDecimal");
    }

    static Boolean bool(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if ("true".equalsIgnoreCase(value.toString())) {
            return true;
        }
        if ("false".equalsIgnoreCase(value.toString())) {
            return false;
        }
        throw new IllegalArgumentException("[" + value + "]不能转为布尔类型");
    }
}
