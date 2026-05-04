package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DELETE 查询模型。
 *
 * <p>保存删除目标、FROM 表、JOIN 子句和 WHERE 条件。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DeleteQuery {

    private final StageContextHolder contextHolder = new StageContextHolder();

    private final List<Class<?>> deleteTargets = new ArrayList<>();

    private final List<JoinClause> joins = new ArrayList<>();

    private TableRef from;

    private ExpressionGroup where;

    /**
     * 获取 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * 添加多表删除目标。
     *
     * @param deleteTarget 删除目标实体类型
     */
    public void addDeleteTarget(Class<?> deleteTarget) {
        if (deleteTarget != null) {
            deleteTargets.add(deleteTarget);
        }
    }

    /**
     * 获取多表删除目标。
     *
     * @return 不可变删除目标列表
     */
    public List<Class<?>> getDeleteTargets() {
        return Collections.unmodifiableList(deleteTargets);
    }

    /**
     * 获取 FROM 表。
     *
     * @return FROM 表引用
     */
    public TableRef getFrom() {
        return from;
    }

    /**
     * 设置 FROM 表。
     *
     * @param from FROM 表引用
     */
    public void setFrom(TableRef from) {
        this.from = from;
    }

    /**
     * 添加 JOIN 子句。
     *
     * @param join JOIN 子句
     */
    public void addJoin(JoinClause join) {
        joins.add(join);
    }

    /**
     * 获取 JOIN 子句列表。
     *
     * @return 不可变 JOIN 列表
     */
    public List<JoinClause> getJoins() {
        return Collections.unmodifiableList(joins);
    }

    /**
     * 获取 WHERE 条件。
     *
     * @return WHERE 条件表达式
     */
    public ExpressionGroup getWhere() {
        return where;
    }

    /**
     * 设置 WHERE 条件。
     *
     * @param where WHERE 条件表达式
     */
    public void setWhere(ExpressionGroup where) {
        this.where = where;
    }
}