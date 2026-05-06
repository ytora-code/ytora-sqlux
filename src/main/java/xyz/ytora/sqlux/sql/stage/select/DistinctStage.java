package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.TableWildcard;

/**
 * DISTINCT阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class DistinctStage extends AbsSelect {

    /**
     * 创建 DISTINCT 阶段，并在查询模型中标记去重。
     *
     * @param query SELECT 查询模型
     */
    public DistinctStage(SelectQuery query) {
        super(query);
        getQuery().setDistinct(true);
    }

    /**
     * DISTINCT后面可能继续SELECT
     * @param columns 查询列
     * @return DistinctStage
     * @param <T> 实体类型
     */
    @SafeVarargs
    public final <T> DistinctStage select(ColFunction<T, ?>... columns) {
        if (columns != null) {
            for (ColFunction<T, ?> column : columns) {
                getQuery().addSelectColumn(ColumnRef.from(column));
            }
        }
        return this;
    }

    /**
     * DISTINCT 后继续追加两个可来自不同实体的查询列。
     */
    public <A, B> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return selectColumns(first, second);
    }

    /**
     * DISTINCT 后继续追加三个可来自不同实体的查询列。
     */
    public <A, B, C> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                          ColFunction<C, ?> third) {
        return selectColumns(first, second, third);
    }

    /**
     * DISTINCT 后继续追加四个可来自不同实体的查询列。
     */
    public <A, B, C, D> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                             ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return selectColumns(first, second, third, fourth);
    }

    /**
     * DISTINCT 后继续追加五个可来自不同实体的查询列。
     */
    public <A, B, C, D, E> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                ColFunction<E, ?> fifth) {
        return selectColumns(first, second, third, fourth, fifth);
    }

    /**
     * DISTINCT 后继续追加六个可来自不同实体的查询列。
     */
    public <A, B, C, D, E, F> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                   ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                   ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return selectColumns(first, second, third, fourth, fifth, sixth);
    }

    /**
     * DISTINCT 后继续追加七个可来自不同实体的查询列。
     */
    public <A, B, C, D, E, F, G> DistinctStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                      ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                      ColFunction<E, ?> fifth, ColFunction<F, ?> sixth,
                                                      ColFunction<G, ?> seventh) {
        return selectColumns(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * DISTINCT 后继续追加通用表达式。
     *
     * @param expressions 查询表达式
     * @return 当前阶段对象
     */
    public DistinctStage select(SqlExpression... expressions) {
        if (expressions != null) {
            for (SqlExpression expression : expressions) {
                if (expression != null) {
                    getQuery().addSelectColumn(expression);
                }
            }
        }
        return this;
    }

    /**
     * DISTINCT 后继续追加表级通配查询列。
     */
    public DistinctStage select(Class<?>... tables) {
        if (tables != null) {
            for (Class<?> table : tables) {
                if (table != null) {
                    getQuery().addSelectColumn(TableWildcard.of(table));
                }
            }
        }
        return this;
    }

    /**
     * DISTINCT 后可能进入 FROM 阶段
     */
    public <T> FromStage<T> from(Class<T> table) {
        return fromStage(table, null);
    }

    /**
     * DISTINCT 后可能进入 FROM 阶段
     */
    public <T> FromStage<T> from(Class<T> table, String alias) {
        return fromStage(table, alias);
    }

    /**
     * DISTINCT 后可能进入 FROM 阶段
     */
    public FromStage<Object> from(String table) {
        return fromStage(table, null);
    }

    /**
     * DISTINCT 后可能进入 FROM 阶段
     */
    public FromStage<Object> from(String table, String alias) {
        return fromStage(table, alias);
    }

    /**
     * DISTINCT 后可能进入 FROM 子查询阶段
     */
    public FromStage<Object> from(AbsSelect subQuery, String alias) {
        return fromStage(subQuery, alias);
    }

    /**
     * 追加方法引用形式的 SELECT 字段。
     */
    private DistinctStage selectColumns(ColFunction<?, ?>... columns) {
        if (columns != null) {
            for (ColFunction<?, ?> column : columns) {
                if (column != null) {
                    getQuery().addSelectColumn(ColumnRef.from(column));
                }
            }
        }
        return this;
    }
}
