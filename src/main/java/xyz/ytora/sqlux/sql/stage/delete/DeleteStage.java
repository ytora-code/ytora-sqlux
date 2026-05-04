package xyz.ytora.sqlux.sql.stage.delete;

import xyz.ytora.sqlux.sql.model.DeleteQuery;
import xyz.ytora.sqlux.sql.model.TableRef;

/**
 * DELETE 起始阶段。
 *
 * <p>用于创建 DELETE 语句，并进入 FROM 阶段。普通单表删除可不传删除目标。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DeleteStage {

    private final DeleteQuery query = new DeleteQuery();

    /**
     * 创建 DELETE 起始阶段。
     *
     * @param deleteTargets 多表删除时的删除目标实体类型
     */
    public DeleteStage(Class<?>... deleteTargets) {
        if (deleteTargets != null) {
            for (Class<?> deleteTarget : deleteTargets) {
                query.addDeleteTarget(deleteTarget);
            }
        }
    }

    /**
     * 指定 DELETE FROM 表。
     *
     * @param table 删除来源表对应的实体类型
     * @return DELETE FROM 阶段对象
     * @param <T> 实体类型
     */
    public <T> DeleteFromStage from(Class<T> table) {
        return from(table, null);
    }

    /**
     * 指定 DELETE FROM 表，并设置别名。
     *
     * @param table 删除来源表对应的实体类型
     * @param alias 表别名
     * @return DELETE FROM 阶段对象
     * @param <T> 实体类型
     */
    public <T> DeleteFromStage from(Class<T> table, String alias) {
        String tableAlias = alias == null || alias.trim().isEmpty()
                ? query.getContextHolder().addTable(table)
                : query.getContextHolder().addTable(table, alias);
        query.setFrom(new TableRef(table, tableAlias));
        return new DeleteFromStage(query);
    }
}