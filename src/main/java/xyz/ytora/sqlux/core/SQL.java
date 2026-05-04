package xyz.ytora.sqlux.core;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.SelectSubQuery;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.TableWildcard;
import xyz.ytora.sqlux.sql.stage.select.AbsSelect;
import xyz.ytora.sqlux.sql.stage.raw.RawQueryStage;
import xyz.ytora.sqlux.sql.stage.raw.RawUpdateStage;
import xyz.ytora.sqlux.sql.stage.delete.DeleteStage;
import xyz.ytora.sqlux.sql.stage.insert.InsertStage;
import xyz.ytora.sqlux.sql.stage.select.DistinctStage;
import xyz.ytora.sqlux.sql.stage.select.SelectStage;
import xyz.ytora.sqlux.sql.stage.update.UpdateStage;

import java.util.Map;

/**
 * Sqlux 的统一 SQL 构造入口。
 *
 * <p>该类只负责创建不同语句的起始阶段，实际 SQL 信息会写入内部查询模型，
 * 最终通过 {@code submit()} 执行或通过 {@code toSql()} 查看翻译结果。</p>
 *
 * <p>使用示例：</p>
 *
 * <pre>{@code
 * List<Map<String, Object>> rows = SQL.select(User::getName, User::getAge)
 *     .from(User.class)
 *     .where(w -> w.eq(User::getId, 1))
 *     .submit();
 * }</pre>
 *
 * <p>输入说明：每个静态方法接收实体类型或实体 getter 方法引用，用来构造结构化 SQL 模型。
 * 输出说明：返回对应的阶段对象，继续通过链式 API 补齐 SQL 子句。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class SQL {

    private static SqluxGlobal sqluxGlobal;

    /**
     * 工具入口类不允许实例化。
     */
    private SQL() {
    }

    public static void registerSqluxGlobal(SqluxGlobal sqluxGlobal) {
        SQL.sqluxGlobal = sqluxGlobal;
    }

    public static SqluxGlobal getSqluxGlobal() {
        if (sqluxGlobal == null) {
            throw new IllegalStateException("在一切开始前，请先执行[SQL.registerSqluxGlobal]注册配置");
        }
        return sqluxGlobal;
    }

    /**
     * 创建 以 DISTINCT 开头的 SELECT 查询
     *
     * <p>示例：{@code SQL.distinct().select().from(User.class)}</p>
     *
     * @return DistinctStage 阶段对象,后面继续 SELECT
     * @param <T> 字段所属实体类型泛型
     */
    public static <T> DistinctStage distinct() {
        SelectQuery query = new SelectQuery();
        return new DistinctStage(query);
    }

    /**
     * 创建 SELECT 查询，并指定查询字段。
     *
     * <p>示例：{@code SQL.select(User::getName).from(User.class)}</p>
     *
     * @param columns 查询字段的方法引用；入参为空或 {@code null} 时由后续阶段决定是否等价于 SELECT *
     * @return SELECT 阶段对象；出参可继续调用 {@code from(...)}、{@code distinct()} 等方法
     * @param <T> 字段所属实体类型泛型
     */
    @SafeVarargs
    public static <T> SelectStage select(ColFunction<T, ?>... columns) {
        SelectQuery query = new SelectQuery();
        if (columns != null) {
            for (ColFunction<T, ?> column : columns) {
                query.addSelectColumn(ColumnRef.from(column));
            }
        }
        return new SelectStage(query);
    }

    /**
     * 创建 SELECT 查询，并指定两个可来自不同实体的查询字段。
     */
    public static <A, B> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return selectColumns(first, second);
    }

    /**
     * 创建 SELECT 查询，并指定三个可来自不同实体的查询字段。
     */
    public static <A, B, C> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                               ColFunction<C, ?> third) {
        return selectColumns(first, second, third);
    }

    /**
     * 创建 SELECT 查询，并指定四个可来自不同实体的查询字段。
     */
    public static <A, B, C, D> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                  ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return selectColumns(first, second, third, fourth);
    }

    /**
     * 创建 SELECT 查询，并指定五个可来自不同实体的查询字段。
     */
    public static <A, B, C, D, E> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                     ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                     ColFunction<E, ?> fifth) {
        return selectColumns(first, second, third, fourth, fifth);
    }

    /**
     * 创建 SELECT 查询，并指定六个可来自不同实体的查询字段。
     */
    public static <A, B, C, D, E, F> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                        ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                        ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return selectColumns(first, second, third, fourth, fifth, sixth);
    }

    /**
     * 创建 SELECT 查询，并指定七个可来自不同实体的查询字段。
     */
    public static <A, B, C, D, E, F, G> SelectStage select(ColFunction<A, ?> first,
                                                           ColFunction<B, ?> second,
                                                           ColFunction<C, ?> third,
                                                           ColFunction<D, ?> fourth,
                                                           ColFunction<E, ?> fifth,
                                                           ColFunction<F, ?> sixth,
                                                           ColFunction<G, ?> seventh) {
        return selectColumns(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * 创建 SELECT 查询，并指定通用表达式。
     *
     * <p>示例：{@code SQL.select(Count.of(User::getId).as("total")).from(User.class)}</p>
     *
     * @param expressions 查询表达式
     * @return SELECT 阶段对象
     */
    public static SelectStage select(SqlExpression... expressions) {
        SelectQuery query = new SelectQuery();
        if (expressions != null) {
            for (SqlExpression expression : expressions) {
                if (expression != null) {
                    query.addSelectColumn(expression);
                }
            }
        }
        return new SelectStage(query);
    }

    /**
     * 创建 SELECT 表级通配查询。
     *
     * <p>示例：{@code SQL.select(User.class).from(User.class)} 会翻译为 {@code SELECT u1.* ...}。</p>
     *
     * @param tables 要查询全部字段的实体表
     * @return SELECT 阶段对象
     */
    public static SelectStage select(Class<?>... tables) {
        SelectQuery query = new SelectQuery();
        if (tables != null) {
            for (Class<?> table : tables) {
                if (table != null) {
                    query.addSelectColumn(TableWildcard.of(table));
                }
            }
        }
        return new SelectStage(query);
    }

    /**
     * 创建包含原始查询字段表达式的 SELECT 查询。
     *
     * <p>示例：{@code SQL.selectRaw("COUNT(*) AS total").from(User.class)}。</p>
     *
     * @param expressions 原始查询字段表达式
     * @return SELECT 阶段对象
     */
    public static SelectStage selectRaw(String... expressions) {
        SelectQuery query = new SelectQuery();
        if (expressions != null) {
            for (String expression : expressions) {
                query.addSelectColumn(ColumnRef.raw(expression));
            }
        }
        return new SelectStage(query);
    }

    /**
     * 创建 SELECT * 查询。
     *
     * <p>示例：{@code SQL.select().from(User.class)} 会翻译为查询全字段。</p>
     *
     * @return SELECT 阶段对象；出参可继续调用 {@code from(...)}
     */
    public static SelectStage select() {
        return new SelectStage(new SelectQuery());
    }

    /**
     * 按方法引用写入 SELECT 字段。
     *
     * @param columns 查询字段
     * @return SELECT 阶段对象
     */
    private static SelectStage selectColumns(ColFunction<?, ?>... columns) {
        SelectQuery query = new SelectQuery();
        if (columns != null) {
            for (ColFunction<?, ?> column : columns) {
                if (column != null) {
                    query.addSelectColumn(ColumnRef.from(column));
                }
            }
        }
        return new SelectStage(query);
    }

    /**
     * 将 SELECT 语句包装为子查询。
     *
     * @param query SELECT阶段对象
     * @return 子查询对象
     */
    public static SelectSubQuery subQuery(AbsSelect query) {
        if (query == null) {
            throw new IllegalArgumentException("子查询不能为空");
        }
        return new SelectSubQuery(query.getQuery());
    }

    /**
     * 创建 INSERT 查询。
     *
     * <p>示例：{@code SQL.insert(User.class).into(User::getName).valuesRow("ytora")}。</p>
     *
     * @param table 插入目标表对应的实体类型；入参用于解析表名和字段所属实体
     * @return INSERT 阶段对象；出参可继续调用 {@code into(...)}
     * @param <T> 实体类型泛型
     */
    public static <T> InsertStage<T> insert(Class<T> table) {
        return new InsertStage<>(table);
    }

    /**
     * 创建 UPDATE 查询。
     *
     * <p>示例：{@code SQL.update(User.class).set(User::getName, "ytora").where(w -> w.eq(User::getId, 1))}。</p>
     *
     * @param table 更新目标表对应的实体类型；入参用于解析表名和字段所属实体
     * @return UPDATE 阶段对象；出参可继续调用 {@code set(...)}、{@code where(...)}
     * @param <T> 实体类型泛型
     */
    public static <T> UpdateStage<T> update(Class<T> table) {
        return new UpdateStage<>(table);
    }

    /**
     * 创建 DELETE 查询。
     *
     * <p>参数用于描述多表删除的删除目标；普通单表删除可以不传。</p>
     *
     * <p>示例：{@code SQL.delete().from(User.class).where(w -> w.eq(User::getId, 1))}；
     * MySQL 多表删除可使用 {@code SQL.delete(User.class).from(User.class).join(...)}。</p>
     *
     * @param deleteTargets 多表删除时的删除目标实体类型；入参为空表示普通单表删除
     * @return DELETE 起始阶段对象；出参可继续调用 {@code from(...)}
     */
    public static DeleteStage delete(Class<?>... deleteTargets) {
        return new DeleteStage(deleteTargets);
    }

    /**
     * 创建原生 SELECT 查询。
     *
     * <p>示例：{@code SQL.rawQuery("SELECT * FROM user WHERE id = ?", 1).submit()}。</p>
     *
     * @param sql 原生 SQL
     * @param params 占位符参数
     * @return 原生查询阶段对象
     */
    public static RawQueryStage rawQuery(String sql, Object... params) {
        return new RawQueryStage(sql, params);
    }

    /**
     * 创建带命名参数的原生 SELECT 查询。
     *
     * <p>示例：{@code SQL.rawQuery("SELECT * FROM user WHERE id = :id", map).submit()}。</p>
     *
     * @param sql 原生 SQL
     * @param params 命名参数
     * @return 原生查询阶段对象
     */
    public static RawQueryStage rawQuery(String sql, Map<String, ?> params) {
        return new RawQueryStage(sql, params);
    }

    /**
     * 创建原生更新语句。
     *
     * <p>示例：{@code SQL.rawUpdate("UPDATE user SET name = ? WHERE id = ?", "ytora", 1).submit()}。</p>
     *
     * @param sql 原生 SQL
     * @param params 占位符参数
     * @return 原生更新阶段对象
     */
    public static RawUpdateStage rawUpdate(String sql, Object... params) {
        return new RawUpdateStage(sql, params);
    }

    /**
     * 创建带命名参数的原生更新语句。
     *
     * <p>示例：{@code SQL.rawUpdate("UPDATE user SET name = :name WHERE id = :id", map).submit()}。</p>
     *
     * @param sql 原生 SQL
     * @param params 命名参数
     * @return 原生更新阶段对象
     */
    public static RawUpdateStage rawUpdate(String sql, Map<String, ?> params) {
        return new RawUpdateStage(sql, params);
    }
}
