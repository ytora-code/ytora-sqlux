package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.DbType;
import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.core.enums.OrderType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.Page;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.*;
import xyz.ytora.sqlux.sql.stage.EntityWhereAppender;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;
import xyz.ytora.sqlux.sql.stage.TerminationStage;
import xyz.ytora.sqlux.translate.SqlResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * SELECT 各阶段的公共基类。
 *
 * <p>所有 SELECT 阶段共享同一个 {@link SelectQuery}，阶段方法只负责维护查询模型，
 * {@code submit()} 负责触发翻译和执行。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings({"overloads", "varargs"})
public abstract class AbsSelect implements TerminationStage<List<Map<String, Object>>> {

    private final SelectQuery query;

    /**
     * 创建 SELECT 阶段基类。
     *
     * @param query SELECT 查询模型
     */
    protected AbsSelect(SelectQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("SELECT查询模型不能为空");
        }
        this.query = query;
    }

    /**
     * 获取当前 SELECT 查询模型。
     *
     * @return SELECT 查询模型
     */
    public SelectQuery getQuery() {
        return query;
    }

    /**
     * 获取当前 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return query.getContextHolder();
    }

    /**
     * 执行 SELECT，并返回原始结果集。
     *
     * @return 以字段名和值表示的查询结果集
     */
    @Override
    public List<Map<String, Object>> submit() {
        return SQL.getSqluxGlobal().getExecutor().query(toSql());
    }

    /**
     * 执行 SELECT，并将结果映射为指定实体类型。
     *
     * @param resultType 结果实体类型
     * @return 映射后的实体集合
     * @param <T> 结果实体类型
     */
    public <T> List<T> submit(Class<T> resultType) {
        return SQL.getSqluxGlobal().getExecutor().query(toSql(), resultType);
    }

    /**
     * 执行 SELECT，并返回指定下标的单条实体结果。
     *
     * <p>当下标越界或结果为 {@code null} 时返回 {@link Optional#empty()}。</p>
     *
     * @param resultType 结果实体类型
     * @param index 结果集下标，从 0 开始
     * @return 指定下标的可选实体结果
     * @param <T> 结果实体类型
     */
    public <T> Optional<T> submit(Class<T> resultType, int index) {
        List<T> rows = submit(resultType);
        if (index < 0 || index >= rows.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rows.get(index));
    }

    /**
     * 根据分页条件执行分页 SQL，并返回分页对象
     *
     * @param page 分页数据
     * @return 分页对象
     * @param <T> 分页数据类型
     */
    public <T> Page<T> submit(Page<T> page) {
        return SelectPageExecutor.submit(this, page);
    }

    /**
     * 使用默认数据库类型翻译 SELECT 语句，不执行数据库操作。
     *
     * @return SQL翻译结果
     */
    public SqlResult toSql() {
        return toSql(SQL.getSqluxGlobal().getDbType());
    }

    /**
     * 使用指定数据库类型翻译 SELECT 语句，不执行数据库操作。
     *
     * @param dbType 数据库类型
     * @return SQL翻译结果
     */
    public SqlResult toSql(DbType dbType) {
        return SelectSqlSupport.toSql(query, dbType);
    }

    /**
     * 写入实体表 FROM 子句并进入 FROM 阶段。
     *
     * @param table 主表实体类型
     * @param alias 主表别名；为空时由阶段上下文自动分配
     * @return FROM 阶段对象
     * @param <T> 主表实体类型
     */
    protected <T> FromStage<T> fromStage(Class<T> table, String alias) {
        String tableAlias = alias == null || alias.trim().isEmpty()
                ? getContextHolder().addTable(table)
                : getContextHolder().addTable(table, alias);
        query.setFrom(new TableRef(table, tableAlias));
        return new FromStage<>(query, table);
    }

    /**
     * 写入子查询 FROM 子句并进入 FROM 阶段。
     *
     * @param subQuery 子查询阶段对象
     * @param alias 子查询别名
     * @return FROM 阶段对象
     */
    protected FromStage<Object> fromStage(AbsSelect subQuery, String alias) {
        query.setFrom(toSubQuery(subQuery, alias));
        return new FromStage<>(query, null);
    }

    /**
     * 写入实体表 JOIN 子句并进入 JOIN 阶段。
     *
     * @param joinType JOIN 类型
     * @param joinTable 被关联的实体表
     * @param alias 被关联表别名；为空时由阶段上下文自动分配
     * @param on ON 条件构造回调
     * @return JOIN 阶段对象
     */
    protected JoinStage joinStage(JoinType joinType, Class<?> joinTable, String alias, Consumer<ExpressionBuilder> on) {
        if (on == null) {
            throw new IllegalArgumentException("ON条件不能为空");
        }
        String tableAlias = alias == null || alias.trim().isEmpty()
                ? getContextHolder().addTable(joinTable)
                : getContextHolder().addTable(joinTable, alias);
        ExpressionBuilder builder = new ExpressionBuilder(getContextHolder());
        on.accept(builder);
        query.addJoin(new JoinClause(joinType, new TableRef(joinTable, tableAlias), builder.toExpression()));
        return new JoinStage(query);
    }

    /**
     * 写入子查询 JOIN 子句并进入 JOIN 阶段。
     *
     * @param joinType JOIN 类型
     * @param subQuery 被关联的子查询
     * @param alias 子查询别名
     * @param on ON 条件构造回调
     * @return JOIN 阶段对象
     */
    protected JoinStage joinStage(JoinType joinType, AbsSelect subQuery, String alias, Consumer<ExpressionBuilder> on) {
        if (on == null) {
            throw new IllegalArgumentException("ON条件不能为空");
        }
        ExpressionBuilder builder = new ExpressionBuilder(getContextHolder());
        on.accept(builder);
        query.addJoin(new JoinClause(joinType, toSubQuery(subQuery, alias), builder.toExpression()));
        return new JoinStage(query);
    }

    /**
     * 写入 WHERE 条件并进入 WHERE 阶段。
     *
     * @param whereExpr WHERE 条件构造回调；为空时保留无 WHERE 状态
     * @return WHERE 阶段对象
     */
    protected SelectWhereStage whereStage(Consumer<ExpressionBuilder> whereExpr) {
        if (whereExpr == null) {
            return new SelectWhereStage(query);
        }
        ExpressionBuilder builder = new ExpressionBuilder(getContextHolder());
        whereExpr.accept(builder);
        query.setWhere(builder.toExpression());
        return new SelectWhereStage(query);
    }

    /**
     * 根据实体对象中的非空字段写入 WHERE 条件。
     *
     * @param whereObjs WHERE 实体条件对象
     * @return WHERE 阶段对象
     * @param <T> 实体类型
     */
    @SafeVarargs
    protected final <T extends AbsEntity> SelectWhereStage whereStage(T... whereObjs) {
        ExpressionBuilder builder = new ExpressionBuilder(getContextHolder());
        if (whereObjs != null) {
            for (T whereObj : whereObjs) {
                appendEntityWhere(builder, whereObj);
            }
        }
        query.setWhere(builder.toExpression());
        return new SelectWhereStage(query);
    }

    /**
     * 将单个实体对象的非空字段追加为等值条件。
     *
     * @param builder WHERE 条件构造器
     * @param whereObj WHERE 实体条件对象
     */
    private void appendEntityWhere(ExpressionBuilder builder, AbsEntity whereObj) {
        if (whereObj == null) {
            return;
        }
        EntityWhereAppender.append(builder, whereObj);
    }

    /**
     * 写入方法引用形式的 GROUP BY 字段。
     *
     * @param groupColumn 分组字段
     * @return GROUP BY 阶段对象
     * @param <T> 字段所属实体类型
     */
    protected <T> GroupByStage groupByStage(ColFunction<T, ?> groupColumn) {
        return groupByStage(groupColumn == null ? null : ColumnRef.from(groupColumn));
    }

    /**
     * 写入两个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B> GroupByStage groupByStage(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return addGroupByColumns(first, second);
    }

    /**
     * 写入三个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B, C> GroupByStage groupByStage(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                  ColFunction<C, ?> third) {
        return addGroupByColumns(first, second, third);
    }

    /**
     * 写入四个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B, C, D> GroupByStage groupByStage(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                     ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return addGroupByColumns(first, second, third, fourth);
    }

    /**
     * 写入五个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B, C, D, E> GroupByStage groupByStage(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                        ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                        ColFunction<E, ?> fifth) {
        return addGroupByColumns(first, second, third, fourth, fifth);
    }

    /**
     * 写入六个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B, C, D, E, F> GroupByStage groupByStage(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                           ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                           ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return addGroupByColumns(first, second, third, fourth, fifth, sixth);
    }

    /**
     * 写入七个方法引用形式的 GROUP BY 字段。
     */
    protected <A, B, C, D, E, F, G> GroupByStage groupByStage(ColFunction<A, ?> first,
                                                              ColFunction<B, ?> second,
                                                              ColFunction<C, ?> third,
                                                              ColFunction<D, ?> fourth,
                                                              ColFunction<E, ?> fifth,
                                                              ColFunction<F, ?> sixth,
                                                              ColFunction<G, ?> seventh) {
        return addGroupByColumns(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * 写入多个方法引用形式的 GROUP BY 字段。
     *
     * @param groupColumns 分组字段
     * @return GROUP BY 阶段对象
     */
    @SafeVarargs
    protected final <T> GroupByStage groupByStage(ColFunction<T, ?>... groupColumns) {
        return addGroupByColumns(groupColumns);
    }

    /**
     * 写入方法引用形式的 GROUP BY 字段。
     *
     * @param groupColumns 分组字段
     * @return GROUP BY 阶段对象
     */
    private GroupByStage addGroupByColumns(ColFunction<?, ?>... groupColumns) {
        if (groupColumns != null) {
            for (ColFunction<?, ?> groupColumn : groupColumns) {
                if (groupColumn != null) {
                    query.addGroupByColumn(ColumnRef.from(groupColumn));
                }
            }
        }
        return new GroupByStage(query);
    }

    /**
     * 写入表达式形式的 GROUP BY 字段。
     *
     * @param groupColumn 分组表达式
     * @return GROUP BY 阶段对象
     */
    protected GroupByStage groupByStage(SqlExpression groupColumn) {
        if (groupColumn != null) {
            query.addGroupByColumn(groupColumn);
        }
        return new GroupByStage(query);
    }

    /**
     * 写入多个表达式形式的 GROUP BY 字段。
     *
     * @param groupColumns 分组表达式
     * @return GROUP BY 阶段对象
     */
    protected GroupByStage groupByStage(SqlExpression... groupColumns) {
        if (groupColumns != null) {
            for (SqlExpression groupColumn : groupColumns) {
                if (groupColumn != null) {
                    query.addGroupByColumn(groupColumn);
                }
            }
        }
        return new GroupByStage(query);
    }

    /**
     * 写入 HAVING 条件并进入 HAVING 阶段。
     *
     * @param havingExpr HAVING 条件构造回调；为空时保留无 HAVING 状态
     * @return HAVING 阶段对象
     */
    protected HavingStage havingStage(Consumer<ExpressionBuilder> havingExpr) {
        if (havingExpr == null) {
            return new HavingStage(query);
        }
        ExpressionBuilder builder = new ExpressionBuilder(getContextHolder());
        havingExpr.accept(builder);
        query.setHaving(builder.toExpression());
        return new HavingStage(query);
    }

    /**
     * 写入方法引用形式的 ORDER BY 排序项。
     *
     * @param orderColumn 排序字段
     * @param orderType 排序方向
     * @return ORDER BY 阶段对象
     * @param <T> 字段所属实体类型
     */
    protected <T> OrderByStage orderByStage(ColFunction<T, ?> orderColumn, OrderType orderType) {
        return orderByStage(orderColumn == null ? null : ColumnRef.from(orderColumn), orderType);
    }

    /**
     * 写入表达式形式的 ORDER BY 排序项。
     *
     * @param orderColumn 排序表达式
     * @param orderType 排序方向
     * @return ORDER BY 阶段对象
     */
    protected OrderByStage orderByStage(SqlExpression orderColumn, OrderType orderType) {
        return orderByStage(true, orderColumn, orderType);
    }

    /**
     * 按条件写入方法引用形式的 ORDER BY 排序项。
     *
     * @param condition 是否追加排序项
     * @param orderColumn 排序字段
     * @param orderType 排序方向
     * @return ORDER BY 阶段对象
     * @param <T> 字段所属实体类型
     */
    protected <T> OrderByStage orderByStage(boolean condition, ColFunction<T, ?> orderColumn, OrderType orderType) {
        return orderByStage(condition, orderColumn == null ? null : ColumnRef.from(orderColumn), orderType);
    }

    /**
     * 按条件写入表达式形式的 ORDER BY 排序项。
     *
     * @param condition 是否追加排序项
     * @param orderColumn 排序表达式
     * @param orderType 排序方向
     * @return ORDER BY 阶段对象
     */
    protected OrderByStage orderByStage(boolean condition, SqlExpression orderColumn, OrderType orderType) {
        if (condition && orderColumn != null) {
            query.addOrderByColumn(new OrderClause(orderColumn, orderType));
        }
        return new OrderByStage(query);
    }

    /**
     * 写入 LIMIT 值并进入 LIMIT 阶段。
     *
     * @param limit 最大返回行数；允许为 {@code null}
     * @return LIMIT 阶段对象
     */
    protected LimitStage limitStage(Integer limit) {
        query.setLimit(validateNonNegative(limit, "LIMIT"));
        return new LimitStage(query);
    }

    /**
     * 写入 OFFSET 值并进入 OFFSET 阶段。
     *
     * @param offset 跳过行数；允许为 {@code null}
     * @return OFFSET 阶段对象
     */
    protected OffsetStage offsetStage(Integer offset) {
        query.setOffset(validateNonNegative(offset, "OFFSET"));
        return new OffsetStage(query);
    }

    /**
     * 校验 LIMIT/OFFSET 不能为负数。
     *
     * @param value 待校验的分页数值
     * @param name 参数名称，用于异常提示
     * @return 原始数值
     */
    private Integer validateNonNegative(Integer value, String name) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(name + " 不能小于0");
        }
        return value;
    }

    /**
     * 将 SELECT 阶段对象包装为可作为数据源使用的子查询。
     *
     * @param subQuery 子查询阶段对象
     * @param alias 子查询别名
     * @return 子查询数据源
     */
    private QuerySource toSubQuery(AbsSelect subQuery, String alias) {
        if (subQuery == null) {
            throw new IllegalArgumentException("子查询不能为空");
        }
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("子查询别名不能为空");
        }
        return new SelectSubQuery(subQuery.getQuery(), alias);
    }
}
