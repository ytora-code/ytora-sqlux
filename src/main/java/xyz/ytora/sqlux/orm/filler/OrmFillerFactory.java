package xyz.ytora.sqlux.orm.filler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 自动填充器实例工厂。
 *
 * @author ytora
 * @since 1.0
 */
final class OrmFillerFactory {

    private static final ConcurrentMap<Class<? extends IFiller>, IFiller> FILLER_CACHE =
            new ConcurrentHashMap<>();

    private OrmFillerFactory() {
    }

    static IFiller getFiller(Class<? extends IFiller> fillerType) {
        IFiller cached = FILLER_CACHE.get(fillerType);
        if (cached != null) {
            return cached;
        }
        IFiller created = newInstance(fillerType);
        IFiller previous = FILLER_CACHE.putIfAbsent(fillerType, created);
        return previous == null ? created : previous;
    }

    static void clearCache() {
        FILLER_CACHE.clear();
    }

    static IFiller newInstance(Class<? extends IFiller> fillerType) {
        try {
            Constructor<? extends IFiller> constructor = fillerType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("自动填充器需要无参构造方法: " + fillerType.getName(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("无法实例化自动填充器: " + fillerType.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("无法访问自动填充器构造方法: " + fillerType.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("自动填充器构造方法执行失败: " + fillerType.getName(), e);
        }
    }
}
