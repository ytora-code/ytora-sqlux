package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.core.enums.JoinType;
import xyz.ytora.sqlux.sql.condition.ExpressionGroup;

/**
 * JOIN 子句模型。
 *
 * @author ytora
 * @since 1.0
 */
public class JoinClause {

    private final JoinType joinType;

    private final QuerySource table;

    private final ExpressionGroup on;

    /**
     * 创建 JOIN 子句。
     *
     * @param joinType JOIN 类型
     * @param table 被连接表
     * @param on ON 条件表达式
     */
    public JoinClause(JoinType joinType, QuerySource table, ExpressionGroup on) {
        if (joinType == null) {
            throw new IllegalArgumentException("JOIN类型不能为空");
        }
        if (table == null) {
            throw new IllegalArgumentException("JOIN表不能为空");
        }
        if (on == null || on.isEmpty()) {
            throw new IllegalArgumentException("ON条件不能为空");
        }
        this.joinType = joinType;
        this.table = table;
        this.on = on;
    }

    /**
     * 获取 JOIN 类型。
     *
     * @return JOIN 类型
     */
    public JoinType getJoinType() {
        return joinType;
    }

    /**
     * 获取被连接表。
     *
     * @return 表引用
     */
    public QuerySource getTable() {
        return table;
    }

    /**
     * 获取 ON 条件表达式。
     *
     * @return ON 条件表达式
     */
    public ExpressionGroup getOn() {
        return on;
    }
}
