package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.condition.ExpressionGroup;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UPDATE 查询模型。
 *
 * <p>保存更新目标表、JOIN 子句、SET 赋值项和 WHERE 条件。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class UpdateQuery {

    private final StageContextHolder contextHolder = new StageContextHolder();

    private final TableRef table;

    private final List<JoinClause> joins = new ArrayList<>();

    private final List<Assignment> assignments = new ArrayList<>();

    private ExpressionGroup where;

    private ColumnRef versionColumn;

    private Object versionValue;

    /**
     * 创建 UPDATE 查询模型。
     *
     * @param tableClass 更新目标表对应的实体类型
     */
    public UpdateQuery(Class<?> tableClass) {
        String alias = contextHolder.addTable(tableClass);
        this.table = new TableRef(tableClass, alias);
    }

    /**
     * 获取 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * 获取更新目标表。
     *
     * @return 表引用
     */
    public TableRef getTable() {
        return table;
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
     * 添加 SET 赋值项。
     *
     * @param assignment SET 赋值项
     */
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    /**
     * 获取 SET 赋值项列表。
     *
     * @return 不可变赋值项列表
     */
    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    /**
     * 设置乐观锁版本条件。
     *
     * @param column 版本字段
     * @param value 旧版本值
     */
    public void setVersionLock(ColumnRef column, Object value) {
        this.versionColumn = column;
        this.versionValue = value;
    }

    /**
     * 是否存在乐观锁版本条件。
     *
     * @return 存在时返回 {@code true}
     */
    public boolean hasVersionLock() {
        return versionColumn != null;
    }

    /**
     * 获取乐观锁版本字段。
     *
     * @return 版本字段
     */
    public ColumnRef getVersionColumn() {
        return versionColumn;
    }

    /**
     * 获取旧版本值。
     *
     * @return 旧版本值
     */
    public Object getVersionValue() {
        return versionValue;
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
