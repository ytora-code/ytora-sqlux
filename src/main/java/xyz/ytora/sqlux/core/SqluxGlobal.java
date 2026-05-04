package xyz.ytora.sqlux.core;

import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.execute.JDBCExecutor;
import xyz.ytora.sqlux.core.execute.MissingSqlExecutor;
import xyz.ytora.sqlux.core.execute.SqlExecutor;
import xyz.ytora.sqlux.core.json.DefaultSqluxJson;
import xyz.ytora.sqlux.core.json.SqluxJson;
import xyz.ytora.sqlux.interceptor.Interceptor;
import xyz.ytora.sqlux.interceptor.log.LogInterceptor;
import xyz.ytora.sqlux.interceptor.log.SqlLogger;
import xyz.ytora.sqlux.interceptor.safety.SafeMutationInterceptor;
import xyz.ytora.sqlux.rw.TypeHandler;
import xyz.ytora.sqlux.rw.TypeHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sqlux 全局配置。
 *
 * <p>该类保存进程级默认配置。多数据源场景应优先使用 {@link SqluxContext} 或执行器配置，
 * 单数据库项目可以只设置一次全局默认数据库类型。</p>
 *
 * <p>使用示例：{@code SqluxConfig.setDefaultDbType(DbType.POSTGRESQL)}。
 * 输入说明：设置进程级数据库类型。输出说明：后续未显式指定数据库类型的 SQL 会使用该配置。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class SqluxGlobal {

    /**
     * 默认数据库类型
     */
    private volatile DbType defaultDbType;

    /**
     * 实体扫描路径配置。
     *
     * <p>entityPath 用于指定 Sqlux 自动建表时要扫描的实体类路径。框架会在指定路径下扫描
     * 所有继承 AbsEntity 的类，并在调用 TableCreators.createMissingTables(connection) 时，
     * 为数据库中不存在的实体表自动执行建表。</p>
     *
     * <p>支持 Java 包名风格路径，并支持通配符：</p>
     * <ul>
     *     <li>*：匹配单个路径片段，不跨包层级。</li>
     *     <li>**：匹配任意多层路径。</li>
     * </ul>
     *
     * <p>示例：</p>
     * <pre>{@code
     * SqluxGlobalConfig.setEntityPath("com.demo.entity.**");
     * TableCreators.createMissingTables(connection);
     * }</pre>
     *
     * <p>常见配置：</p>
     * <pre>{@code
     * com.demo.entity.**        // 扫描 entity 包及其所有子包
     * com.demo.*.entity.*       // 扫描单层模块下 entity 包中的实体类
     * com.demo.**.entity.**     // 扫描任意层模块下 entity 包及其子包
     * }</pre>
     *
     * <p>多个路径可用逗号、分号或空白分隔：</p>
     * <pre>{@code
     * SqluxGlobalConfig.setEntityPath("com.demo.user.entity.**, com.demo.order.entity.**");
     * }</pre>
     */
    private String entityPath;

    /**
     * 数据库连接提供器
     */
    private IConnectionProvider connectionProvider;

    /**
     * SQL执行器
     */
    private volatile SqlExecutor executor;

    /**
     * 拦截器
     */
    private final CopyOnWriteArrayList<Interceptor> INTERCEPTORS;

    /**
     * 日志记录器
     */
    private SqlLogger sqlLogger;

    /**
     * Sqlux使用的json处理器
     */
    private volatile SqluxJson sqluxJson;


    /**
     * 全局配置工具类不允许实例化。
     */
    public SqluxGlobal() {
        SQL.registerSqluxGlobal(this);
        defaultDbType = DbType.MYSQL;
        connectionProvider = new DefaultConnectionProvider();
        INTERCEPTORS = new CopyOnWriteArrayList<>();
        sqluxJson = new DefaultSqluxJson();
        executor = new JDBCExecutor();
        registerBuiltInInterceptors();
    }

    /**
     * 获取当前 SQL 翻译应使用的数据库类型。
     *
     * <p>优先使用 {@link SqluxContext} 当前线程数据库类型；未设置时使用当前执行器提供的数据库类型。</p>
     *
     * @return 数据库类型；出参不会为 {@code null}
     */
    public DbType getDbType() {
        DbType contextDbType = SqluxContext.getDbType();
        if (contextDbType != null) {
            return contextDbType;
        }
        return executor.getDbType();
    }

    /**
     * 获取全局默认数据库类型。
     *
     * <p>示例：应用启动时未做任何配置时返回 {@link DbType#MYSQL}。</p>
     *
     * @return 全局默认数据库类型；出参不会为 {@code null}
     */
    public DbType getDefaultDbType() {
        return this.defaultDbType;
    }

    /**
     * 设置全局默认数据库类型。
     *
     * <p>示例：{@code setDefaultDbType(DbType.POSTGRESQL)} 后，普通 {@code toSql()} 默认使用 PostgreSQL 方言。</p>
     *
     * @param dbType 数据库类型；入参为 {@code null} 时恢复为 {@link DbType#MYSQL}
     */
    public void setDefaultDbType(DbType dbType) {
        this.defaultDbType = dbType == null ? DbType.MYSQL : dbType;
    }

    /**
     * 获取实体扫描路径。
     *
     * <p>示例：{@code xyz.ytora.demo.entity,xyz.ytora.admin.**}。路径支持逗号、分号或空白分隔；
     * {@code *} 表示单段通配，{@code **} 表示多段通配。</p>
     *
     * @return 实体扫描路径；未配置时返回 {@code null}
     */
    public String getEntityPath() {
        return this.entityPath;
    }

    /**
     * 设置实体扫描路径。
     *
     * <p>该配置供自动建表等 ORM 扫描能力使用。示例：{@code setEntityPath("com.demo.entity.**")}。</p>
     *
     * @param path 实体扫描路径；入参为空白字符串时等价于未配置
     */
    public void setEntityPath(String path) {
        this.entityPath = path == null || path.trim().isEmpty() ? null : path.trim();
    }

    /**
     * 获取当前执行器。
     *
     * @return 当前注册的执行器；未注册时返回默认失败实现
     */
    public SqlExecutor getExecutor() {
        return this.executor;
    }

    /**
     * 注册 SQL 执行器。
     *
     * <p>传入 {@code null} 会恢复为默认失败实现。</p>
     *
     * @param sqlExecutor SQL执行器实现
     */
    public void setExecutor(SqlExecutor sqlExecutor) {
        if (sqlExecutor == null) {
            executor = new MissingSqlExecutor();
            return;
        }
        executor = sqlExecutor;
    }

    /**
     * 获取数据库连接提供器
     * @return IConnectionProvider实现类
     */
    public IConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * 设置数据库连接提供器
     * @param connectionProvider IConnectionProvider实现类
     */
    public void registerConnectionProvider(IConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider == null
                ? new DefaultConnectionProvider()
                : connectionProvider;
        executor = new JDBCExecutor(this.connectionProvider);
    }

    /**
     * 注册一个 SQL 拦截器。
     *
     * <p>示例：{@code Interceptors.add(new SqlLogInterceptor())} 会把日志拦截器追加到链尾。</p>
     *
     * @param interceptor SQL拦截器；入参为 {@code null} 时直接忽略
     */
    public void registerInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            INTERCEPTORS.add(interceptor);
        }
    }

    /**
     * 移除一个 SQL 拦截器。
     *
     * <p>示例：测试结束时可调用 {@code Interceptors.remove(interceptor)} 清理单个拦截器。</p>
     *
     * @param interceptor 待移除拦截器；入参为 {@code null} 时直接忽略
     */
    public void removeInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            INTERCEPTORS.remove(interceptor);
        }
    }

    /**
     * 清空所有已注册拦截器，并恢复框架内置日志拦截器。
     */
    public void clearInterceptors() {
        INTERCEPTORS.clear();
        registerBuiltInInterceptors();
    }

    /**
     * 获取拦截器快照。
     *
     * <p>执行器和翻译器应优先使用该方法，避免外部修改拦截器列表影响当前执行链。</p>
     *
     * @return 不可变拦截器列表
     */
    public List<Interceptor> snapshotInterceptors() {
        return Collections.unmodifiableList(new ArrayList<>(INTERCEPTORS));
    }

    public SqlLogger getSqlLogger() {
        return sqlLogger;
    }

    public void registerSqlLogger(SqlLogger sqlLogger) {
        this.sqlLogger = sqlLogger;
    }

    /**
     * 设置当前 JSON 编解码器。
     *
     * @param json JSON编解码器；入参不能为 {@code null}
     */
    public void registerSqluxJson(SqluxJson json) {
        if (json == null) {
            throw new IllegalArgumentException("SqluxJson不能为空");
        }
        sqluxJson = json;
    }

    /**
     * 获取当前 JSON 编解码器。
     *
     * @return JSON编解码器；未配置时返回默认失败实现
     */
    public SqluxJson getSqluxJson() {
        return sqluxJson;
    }

    /**
     * 注册字段类型处理器。
     *
     * @param handler 字段类型处理器；入参为 {@code null} 时直接忽略
     */
    public static void registerTypeHandler(TypeHandler<?> handler) {
        TypeHandlers.register(handler);
    }

    /**
     * 移除字段类型处理器。
     *
     * @param handler 字段类型处理器；入参为 {@code null} 时直接忽略
     */
    public static void removeTypeHandler(TypeHandler<?> handler) {
        TypeHandlers.remove(handler);
    }

    /**
     * 清空字段类型处理器。
     */
    public static void clearTypeHandlers() {
        TypeHandlers.clear();
    }

    /**
     * 注册框架内置拦截器。
     */
    private void registerBuiltInInterceptors() {
        registerInterceptor(new SafeMutationInterceptor());
        registerInterceptor(new LogInterceptor());
    }
}
