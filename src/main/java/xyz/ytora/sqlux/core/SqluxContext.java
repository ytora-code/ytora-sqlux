package xyz.ytora.sqlux.core;

import xyz.ytora.sqlux.core.enums.DbType;

import java.util.concurrent.Callable;

/**
 * Sqlux 当前线程上下文。
 *
 * <p>该类用于在一次业务调用链中临时指定数据库类型，适合动态数据源和多租户场景。
 * 线程上下文优先级高于执行器自动探测和全局默认配置。</p>
 *
 * <p>使用示例：{@code SqluxContext.use(DbType.POSTGRESQL, () -> SQL.select(...).submit())}。
 * 输入说明：传入临时数据库类型和要执行的任务。输出说明：任务内部 SQL 使用指定方言，任务结束后恢复旧上下文。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class SqluxContext {

    private static final ThreadLocal<DbType> DB_TYPE = new ThreadLocal<>();

    /**
     * 线程上下文工具类不允许实例化。
     */
    private SqluxContext() {
    }

    /**
     * 获取当前线程数据库类型。
     *
     * <p>示例：在 {@code SqluxContext.use(DbType.POSTGRESQL, task)} 的 task 内返回 {@code POSTGRESQL}。</p>
     *
     * @return 当前线程数据库类型；未设置时返回 {@code null}
     */
    public static DbType getDbType() {
        return DB_TYPE.get();
    }

    /**
     * 设置当前线程数据库类型。
     *
     * <p>示例：动态数据源切换后调用 {@code setDbType(DbType.MYSQL)}。</p>
     *
     * @param dbType 当前线程数据库类型；入参为 {@code null} 时清除上下文
     */
    public static void setDbType(DbType dbType) {
        if (dbType == null) {
            DB_TYPE.remove();
        } else {
            DB_TYPE.set(dbType);
        }
    }

    /**
     * 清除当前线程数据库类型。
     *
     * <p>示例：请求结束或测试结束时调用，避免 ThreadLocal 值影响后续任务。</p>
     */
    public static void clear() {
        DB_TYPE.remove();
    }

    /**
     * 在指定数据库类型上下文中执行任务。
     *
     * <p>示例：{@code use(DbType.POSTGRESQL, () -> SQL.select(User::getName).from(User.class).submit())}。</p>
     *
     * @param dbType 临时数据库类型；入参会覆盖当前线程已有类型
     * @param runnable 待执行任务；入参不能为 {@code null}
     */
    public static void use(DbType dbType, Runnable runnable) {
        DbType old = DB_TYPE.get();
        setDbType(dbType);
        try {
            runnable.run();
        } finally {
            restore(old);
        }
    }

    /**
     * 在指定数据库类型上下文中执行任务并返回结果。
     *
     * <p>示例：{@code List<User> users = call(DbType.POSTGRESQL, () -> SQL.select(...).submit(User.class))}。</p>
     *
     * @param dbType 临时数据库类型；入参会覆盖当前线程已有类型
     * @param callable 待执行任务；入参不能为 {@code null}
     * @return 任务返回值；出参由 {@code callable} 决定
     * @param <T> 返回值泛型
     */
    public static <T> T call(DbType dbType, Callable<T> callable) {
        DbType old = DB_TYPE.get();
        setDbType(dbType);
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Sqlux上下文任务执行失败", e);
        } finally {
            restore(old);
        }
    }

    /**
     * 恢复进入临时上下文前的数据库类型。
     *
     * @param old 原线程上下文数据库类型；为空时清除线程变量
     */
    private static void restore(DbType old) {
        if (old == null) {
            DB_TYPE.remove();
        } else {
            DB_TYPE.set(old);
        }
    }
}
