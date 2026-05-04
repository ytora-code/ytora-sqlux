package xyz.ytora.sqlux.sql.stage.insert;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.model.InsertQuery;

/**
 * INSERT 起始阶段。
 *
 * <p>用于指定插入目标表，并进入 {@code INTO} 阶段。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class InsertStage<T> {

    private final InsertQuery query;

    /**
     * 创建 INSERT 阶段。
     *
     * @param table 插入目标表对应的实体类型
     */
    public InsertStage(Class<T> table) {
        this.query = new InsertQuery(table);
    }

    /**
     * 指定 INSERT 字段，并进入 VALUES 阶段。
     *
     * <p>不传字段时表示插入全字段，调用者需要保证 {@code valuesRow} 的值与表字段顺序一致。</p>
     *
     * @param columns 插入字段方法引用
     * @return INSERT VALUES 阶段对象
     */
    @SafeVarargs
    public final InsertValuesStage into(ColFunction<T, ?>... columns) {
        if (columns != null) {
            for (ColFunction<T, ?> column : columns) {
                query.addColumn(ColumnRef.from(column));
            }
        }
        return new InsertValuesStage(query);
    }
}