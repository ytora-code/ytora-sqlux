package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.TableWildcard;

/**
 * SELECT阶段
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class SelectStage extends AbsSelect {

    /**
     * 创建 SELECT 起始阶段。
     *
     * @param query SELECT 查询模型
     */
    public SelectStage(SelectQuery query) {
        super(query);
    }

    /**
     * SELECT后面可能继续SELECT。
     *
     * @param columns 查询列
     * @return SelectStage
     * @param <T> 实体类型
     */
    @SafeVarargs
    public final <T> SelectStage select(ColFunction<T, ?>... columns) {
        if (columns != null) {
            for (ColFunction<T, ?> column : columns) {
                getQuery().addSelectColumn(ColumnRef.from(column));
            }
        }
        return this;
    }

    /**
     * SELECT 后继续追加两个可来自不同实体的查询列。
     */
    public <A, B> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second) {
        return selectColumns(first, second);
    }

    /**
     * SELECT 后继续追加三个可来自不同实体的查询列。
     */
    public <A, B, C> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                        ColFunction<C, ?> third) {
        return selectColumns(first, second, third);
    }

    /**
     * SELECT 后继续追加四个可来自不同实体的查询列。
     */
    public <A, B, C, D> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                           ColFunction<C, ?> third, ColFunction<D, ?> fourth) {
        return selectColumns(first, second, third, fourth);
    }

    /**
     * SELECT 后继续追加五个可来自不同实体的查询列。
     */
    public <A, B, C, D, E> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                              ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                              ColFunction<E, ?> fifth) {
        return selectColumns(first, second, third, fourth, fifth);
    }

    /**
     * SELECT 后继续追加六个可来自不同实体的查询列。
     */
    public <A, B, C, D, E, F> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                 ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                 ColFunction<E, ?> fifth, ColFunction<F, ?> sixth) {
        return selectColumns(first, second, third, fourth, fifth, sixth);
    }

    /**
     * SELECT 后继续追加七个可来自不同实体的查询列。
     */
    public <A, B, C, D, E, F, G> SelectStage select(ColFunction<A, ?> first, ColFunction<B, ?> second,
                                                    ColFunction<C, ?> third, ColFunction<D, ?> fourth,
                                                    ColFunction<E, ?> fifth, ColFunction<F, ?> sixth,
                                                    ColFunction<G, ?> seventh) {
        return selectColumns(first, second, third, fourth, fifth, sixth, seventh);
    }

    /**
     * SELECT 后继续追加通用表达式。
     *
     * @param expressions 查询表达式
     * @return 当前阶段对象
     */
    public SelectStage select(SqlExpression... expressions) {
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
     * SELECT 后继续追加表级通配查询列。
     */
    public SelectStage select(Class<?>... tables) {
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
     * SELECT 后继续追加原始字段表达式。
     *
     * @param expressions 原始字段表达式
     * @return 当前阶段对象
     */
    public SelectStage selectRaw(String... expressions) {
        if (expressions != null) {
            for (String expression : expressions) {
                getQuery().addSelectColumn(ColumnRef.raw(expression));
            }
        }
        return this;
    }

    /**
     * SELECT 后可能进入 DISTINCT 阶段。
     */
    public DistinctStage distinct() {
        getQuery().setDistinct(true);
        return new DistinctStage(getQuery());
    }

    /**
     * SELECT 后可能进入 FROM 阶段。
     */
    public <T> FromStage<T> from(Class<T> table) {
        return fromStage(table, null);
    }

    /**
     * SELECT 后可能进入 FROM 阶段。
     */
    public <T> FromStage<T> from(Class<T> table, String alias) {
        return fromStage(table, alias);
    }

    /**
     * SELECT 后可能进入 FROM 阶段。
     */
    public FromStage<Object> from(String table) {
        return fromStage(table, null);
    }

    /**
     * SELECT 后可能进入 FROM 阶段。
     */
    public FromStage<Object> from(String table, String alias) {
        return fromStage(table, alias);
    }

    /**
     * SELECT 后可能进入 FROM 子查询阶段。
     */
    public FromStage<Object> from(AbsSelect subQuery, String alias) {
        return fromStage(subQuery, alias);
    }

    /**
     * 追加方法引用形式的 SELECT 字段。
     */
    private SelectStage selectColumns(ColFunction<?, ?>... columns) {
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
