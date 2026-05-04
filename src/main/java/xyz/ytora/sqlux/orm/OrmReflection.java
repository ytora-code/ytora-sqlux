package xyz.ytora.sqlux.orm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ORM 反射调用工具。
 *
 * <p>集中处理无参构造实例化和方法调用异常包装，避免映射流程掺杂反射细节。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class OrmReflection {

    private OrmReflection() {
    }

    static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("实体类需要无参构造方法: " + type.getName(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("无法实例化实体类: " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("无法访问实体类构造方法: " + type.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("实体类构造方法执行失败: " + type.getName(), e);
        }
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
