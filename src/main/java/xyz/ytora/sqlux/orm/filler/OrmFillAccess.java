package xyz.ytora.sqlux.orm.filler;

import xyz.ytora.sqlux.orm.EntityFieldMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自动填充字段访问工具。
 *
 * @author ytora
 * @since 1.0
 */
final class OrmFillAccess {

    private OrmFillAccess() {
    }

    static Object readValue(Object entity, EntityFieldMeta field) {
        Method getter = field.getGetter();
        if (getter == null) {
            return null;
        }
        return invoke(getter, entity);
    }

    static void writeValue(Object entity, EntityFieldMeta field, Object value) {
        invoke(field.getSetter(), entity, value);
    }

    static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问方法: " + method.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("方法执行失败: " + method.getName(), e.getTargetException());
        }
    }
}
