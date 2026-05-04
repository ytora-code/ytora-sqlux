package xyz.ytora.sqlux.core.execute;

import xyz.ytora.sqlux.core.IConnectionProvider;
import xyz.ytora.sqlux.core.IDbTypeProvider;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.SqluxGlobal;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.interceptor.SqlExecutionContext;
import xyz.ytora.sqlux.translate.SqlResult;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * 基于 JDBC 的 SQL 执行器。
 *
 * <p>连接的创建和关闭都委托给 {@link IConnectionProvider}，本类只负责执行翻译后的 SQL、
 * 绑定参数、读取结果集和按需做 ORM 映射。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class JDBCExecutor implements SqlExecutor {

    private final IConnectionProvider connectionProvider;

    private final DbType configuredDbType;

    private volatile DbType detectedDbType;

    /**
     * 创建 JDBC 执行器。
     * <p>会使用{@link SqluxGlobal}里设置的数据库连接器</p>
     */
    public JDBCExecutor() {
        this(SQL.getSqluxGlobal().getConnectionProvider(), null);
    }

    /**
     * 创建 JDBC 执行器。
     *
     * <p>示例：{@code SqlExecutors.set(new JDBCExecutor(connectionProvider));}</p>
     *
     * @param connectionProvider 数据库连接提供组件；入参不能为 {@code null}，例如用户实现的连接池适配器
     */
    public JDBCExecutor(IConnectionProvider connectionProvider) {
        this(connectionProvider, null);
    }

    /**
     * 创建 JDBC 执行器，并显式指定数据库类型。
     *
     * <p>示例：{@code new JDBCExecutor(connectionProvider, DbType.POSTGRESQL)} 会让该执行器默认使用
     * PostgreSQL 方言，避免执行器访问 JDBC metadata 自动探测。</p>
     *
     * @param connectionProvider 数据库连接提供组件；入参不能为 {@code null}
     * @param dbType 数据库类型；入参为 {@code null} 时允许执行器自动探测或使用全局默认值
     */
    public JDBCExecutor(IConnectionProvider connectionProvider, DbType dbType) {
        if (connectionProvider == null) {
            throw new IllegalArgumentException("IConnectionProvider不能为空");
        }
        this.connectionProvider = connectionProvider;
        this.configuredDbType = dbType;
    }

    /**
     * 执行查询并返回原始结果集。
     *
     * <p>示例：传入 SQL 为 {@code SELECT id, user_name FROM user WHERE id = ?}、
     * 参数为 {@code [1]}，返回值可能是 {@code [{id=1, user_name=ytora}]}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含可执行 SQL 文本和按占位符顺序排列的参数
     * @return 原始查询结果集；每行是一个 {@link Map}，key 是 JDBC 列标签或列名，value 是 {@link ResultSet#getObject(int)} 的值
     */
    @Override
    public List<Map<String, Object>> query(SqlResult sqlResult) {
        return queryRaw(sqlResult, null);
    }

    /**
     * 执行查询并返回原始结果集，但不触发 SQL 拦截器。
     *
     * <p>适用于审计、删除前备份等非常规业务场景。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @return 原始查询结果集
     */
    @Override
    public List<Map<String, Object>> queryWithoutInterceptors(SqlResult sqlResult) {
        return executeQuery(sqlResult);
    }

    /**
     * 执行查询并将原始结果集映射为实体集合。
     *
     * <p>示例：传入 {@code resultType=User.class}，原始行中存在 {@code user_name}，
     * 且 {@code User} 有 {@code setUserName(String)} 时，会返回已绑定字段值的 {@code List<User>}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含可执行 SQL 文本和按占位符顺序排列的参数
     * @param resultType 结果实体类型；入参不能缺少无参构造方法，字段绑定依赖 setter
     * @return 映射后的实体集合；如果查询没有结果，则返回空集合
     * @param <T> 结果实体泛型，例如 {@code User}
     */
    @Override
    public <T> List<T> query(SqlResult sqlResult, Class<T> resultType) {
        return OrmMapper.mapRows(queryRaw(sqlResult, resultType), resultType);
    }

    /**
     * 执行查询并映射为指定实体类型，但不触发 SQL 拦截器。
     *
     * @param sqlResult SQL翻译结果；入参包含SQL文本和有序参数
     * @param resultType 结果实体类型；入参例如 {@code User.class}
     * @param <T> 结果实体类型泛型，例如 {@code User}
     * @return 映射后的实体集合
     */
    @Override
    public <T> List<T> queryWithoutInterceptors(SqlResult sqlResult, Class<T> resultType) {
        return OrmMapper.mapRows(executeQuery(sqlResult), resultType);
    }

    /**
     * 执行 INSERT、UPDATE 或 DELETE。
     *
     * <p>示例：传入 SQL 为 {@code UPDATE user SET name = ? WHERE id = ?}、
     * 参数为 {@code [ytora, 1]}，返回数据库报告的受影响行数。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含可执行 SQL 文本和按占位符顺序排列的参数
     * @return JDBC {@link PreparedStatement#executeUpdate()} 返回的受影响行数
     */
    @Override
    public int update(SqlResult sqlResult) {
        return update(sqlResult, null);
    }

    /**
     * 执行 INSERT、UPDATE 或 DELETE，并在 JDBC 返回 generated keys 时回填到实体对象。
     *
     * <p>示例：调用 {@code SQL.insert(User.class).into(User::getName).values(user).submit()} 后，
     * 如果 JDBC 返回主键 {@code id=10}，且 {@code User} 存在 {@code setId(Integer)}，
     * 则该方法会尝试把 {@code 10} 回填到 {@code user}。</p>
     *
     * @param sqlResult SQL翻译结果；入参包含可执行 SQL 文本和按占位符顺序排列的参数
     * @param generatedKeyTargets 需要回填主键的实体对象集合；为空或 {@code null} 时不请求 generated keys
     * @return JDBC {@link PreparedStatement#executeUpdate()} 返回的受影响行数
     */
    @Override
    public int update(SqlResult sqlResult, List<?> generatedKeyTargets) {
        SqlExecutionContext context = new SqlExecutionContext(sqlResult, null, generatedKeyTargets);
        ExecutionInterceptorChain interceptors = ExecutionInterceptorChain.snapshot();
        try {
            interceptors.before(context);
            int affectedRows = executeUpdate(context.getSqlResult(), context.getGeneratedKeyTargets());
            context.setResult(affectedRows);
            context.markEnd();
            interceptors.success(context);
            return affectedRows;
        } catch (RuntimeException | Error e) {
            interceptors.failure(context, e);
            throw e;
        } finally {
            interceptors.finish(context);
        }
    }

    /**
     * 使用 JDBC batch 批量执行 INSERT、UPDATE 或 DELETE。
     *
     * @param sqlResult SQL翻译结果；入参 SQL 应只包含一组占位符
     * @param batchParams 批量参数
     * @param generatedKeyTargets 需要回填主键的实体对象；为空时不请求 generated keys
     * @return 每条 SQL 的影响行数
     */
    @Override
    public int[] updateBatch(SqlResult sqlResult, List<List<Object>> batchParams, List<?> generatedKeyTargets) {
        if (batchParams == null || batchParams.isEmpty()) {
            return new int[0];
        }
        SqlExecutionContext context = new SqlExecutionContext(sqlResult, null, generatedKeyTargets);
        ExecutionInterceptorChain interceptors = ExecutionInterceptorChain.snapshot();
        try {
            interceptors.before(context);
            int[] affectedRows = executeBatch(context.getSqlResult(), batchParams, context.getGeneratedKeyTargets());
            context.setResult(affectedRows);
            context.markEnd();
            interceptors.success(context);
            return affectedRows;
        } catch (RuntimeException | Error e) {
            interceptors.failure(context, e);
            throw e;
        } finally {
            interceptors.finish(context);
        }
    }

    /**
     * 获取当前 JDBC 执行器使用的数据库类型。
     *
     * <p>优先级为：构造参数显式指定、连接提供器实现 {@link IDbTypeProvider}、JDBC metadata 一次探测缓存、
     * 全局默认配置。</p>
     *
     * @return 数据库类型；出参不会为 {@code null}
     */
    @Override
    public DbType getDbType() {
        if (configuredDbType != null) {
            return configuredDbType;
        }
        if (connectionProvider instanceof IDbTypeProvider) {
            DbType providerDbType = ((IDbTypeProvider) connectionProvider).getDbType();
            if (providerDbType != null) {
                return providerDbType;
            }
        }
        DbType cached = detectedDbType;
        if (cached != null) {
            return cached;
        }
        detectedDbType = detectDbType();
        return detectedDbType;
    }

    /**
     * 通过 JDBC metadata 探测数据库类型。
     *
     * <p>该方法只在执行器未显式配置数据库类型，且连接提供器也未提供数据库类型时调用。
     * 探测结果会缓存在当前 {@code JDBCExecutor} 实例中，避免每次翻译 SQL 都访问 metadata。</p>
     *
     * @return 探测到的数据库类型；探测失败时返回全局默认数据库类型
     */
    private DbType detectDbType() {
        Connection connection = getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            return DbType.fromString(metaData.getDatabaseProductName());
        } catch (SQLException | IllegalArgumentException e) {
            return SQL.getSqluxGlobal().getDefaultDbType();
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }

    /**
     * 执行原始查询，并在 JDBC 前后触发拦截器回调。
     *
     * <p>示例：普通 {@code submit()} 会传入 {@code resultType=null}；
     * {@code submit(User.class)} 会传入 {@code User.class}，方便拦截器识别 ORM 查询。</p>
     *
     * @param sqlResult SQL翻译结果；入参可能会被前置拦截器替换
     * @param resultType 查询实体结果类型；入参仅用于拦截器上下文
     * @return 原始查询结果集；出参尚未做 Bean 映射
     */
    private List<Map<String, Object>> queryRaw(SqlResult sqlResult, Class<?> resultType) {
        SqlExecutionContext context = new SqlExecutionContext(sqlResult, resultType, null);
        ExecutionInterceptorChain interceptors = ExecutionInterceptorChain.snapshot();
        try {
            interceptors.before(context);
            List<Map<String, Object>> rows = executeQuery(context.getSqlResult());
            context.setResult(rows);
            context.markEnd();
            interceptors.success(context);
            return rows;
        } catch (RuntimeException | Error e) {
            interceptors.failure(context, e);
            throw e;
        } finally {
            interceptors.finish(context);
        }
    }

    /**
     * 执行 JDBC 查询，不触发拦截器。
     *
     * <p>示例：前置拦截器完成 SQL 改写后，本方法只负责使用最终的 SQL 和参数执行查询。</p>
     *
     * @param sqlResult SQL翻译结果；入参已经是拦截器处理后的最终 SQL
     * @return 原始查询结果集
     */
    private List<Map<String, Object>> executeQuery(SqlResult sqlResult) {
        Connection connection = getConnection();
        try {
            try (PreparedStatement statement = connection.prepareStatement(sqlResult.getSql())) {
                JdbcParameterBinder.bind(statement, sqlResult);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return JdbcResultReader.readRows(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("执行查询SQL失败: " + sqlResult.getSql() + "\n详情: " + e.getMessage(), e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }

    /**
     * 执行 JDBC 更新，不触发拦截器。
     *
     * <p>示例：前置拦截器完成参数改写后，本方法只负责使用最终的 SQL 和参数执行更新。</p>
     *
     * @param sqlResult SQL翻译结果；入参已经是拦截器处理后的最终 SQL
     * @param generatedKeyTargets 需要回填主键的实体对象；入参为空时不请求 generated keys
     * @return JDBC返回的受影响行数
     */
    private int executeUpdate(SqlResult sqlResult, List<?> generatedKeyTargets) {
        Connection connection = getConnection();
        try {
            boolean needGeneratedKeys = JdbcResultReader.shouldReturnGeneratedKeys(generatedKeyTargets);
            try (PreparedStatement statement = needGeneratedKeys
                    ? connection.prepareStatement(sqlResult.getSql(), JdbcResultReader.generatedKeysFlag())
                    : connection.prepareStatement(sqlResult.getSql())) {
                JdbcParameterBinder.bind(statement, sqlResult);
                int affectedRows = statement.executeUpdate();
                if (needGeneratedKeys) {
                    JdbcResultReader.backfillGeneratedKeys(statement, generatedKeyTargets);
                }
                return affectedRows;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("执行更新SQL失败: " + sqlResult.getSql() + "\n详情: " + e.getMessage(), e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }

    /**
     * 执行 JDBC 批处理，不触发拦截器。
     *
     * @param sqlResult SQL翻译结果
     * @param batchParams 批量参数
     * @param generatedKeyTargets 需要回填主键的实体对象
     * @return 每条 SQL 的影响行数
     */
    private int[] executeBatch(SqlResult sqlResult, List<List<Object>> batchParams, List<?> generatedKeyTargets) {
        Connection connection = getConnection();
        try {
            boolean needGeneratedKeys = JdbcResultReader.shouldReturnGeneratedKeys(generatedKeyTargets);
            try (PreparedStatement statement = needGeneratedKeys
                    ? connection.prepareStatement(sqlResult.getSql(), JdbcResultReader.generatedKeysFlag())
                    : connection.prepareStatement(sqlResult.getSql())) {
                for (List<Object> params : batchParams) {
                    JdbcParameterBinder.bind(statement, params);
                    statement.addBatch();
                }
                int[] affectedRows = statement.executeBatch();
                if (needGeneratedKeys) {
                    JdbcResultReader.backfillGeneratedKeys(statement, generatedKeyTargets);
                }
                return affectedRows;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("批量执行SQL失败: " + sqlResult.getSql() + "\n详情: " + e.getMessage(), e);
        } finally {
            connectionProvider.closeConnection(connection);
        }
    }

    /**
     * 从连接提供器获取一个可用连接。
     *
     * <p>示例：如果 {@link IConnectionProvider#getConnection()} 返回连接池中的连接，
     * 本方法直接返回该连接；如果返回 {@code null}，则抛出异常阻止后续执行。</p>
     *
     * @return 数据库连接；出参一定不为 {@code null}
     */
    private Connection getConnection() {
        Connection connection = connectionProvider.getConnection();
        if (connection == null) {
            throw new IllegalStateException("IConnectionProvider返回的Connection不能为空");
        }
        return connection;
    }

}
