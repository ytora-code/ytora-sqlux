package xyz.ytora.sqlux.sql.stage;

import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.OrmMapper;
import xyz.ytora.sqlux.sql.condition.ExpressionBuilder;
import xyz.ytora.sqlux.sql.model.ColumnRef;

import java.util.Map;

/**
 * 实体 WHERE 条件追加工具。
 *
 * <p>把实体对象中的非空字段转换为等值条件，供 SELECT、UPDATE、DELETE 的对象式
 * {@code where(entity)} 复用。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class EntityWhereAppender {

    private EntityWhereAppender() {
    }

    /**
     * 将实体对象中的非空字段追加为等值条件。
     *
     * @param builder WHERE 条件构造器
     * @param whereObj WHERE 实体条件对象
     */
    public static void append(ExpressionBuilder builder, AbsEntity whereObj) {
        if (builder == null || whereObj == null) {
            return;
        }
        Class<?> entityType = whereObj.getClass();
        for (Map.Entry<String, Object> entry : OrmMapper.readNonNullValues(whereObj).entrySet()) {
            builder.eq(ColumnRef.of(entityType, entry.getKey()), entry.getValue());
        }
    }
}
