package xyz.ytora.sqlux.core.enums;

/**
 * 多表连接类型
 *
 * <p>左外连接，右外连接，内连接</p>
 *
 * @author ytora 
 * @since 1.0
 */
public enum JoinType {
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    INNER_JOIN("INNER JOIN");

    final String joinKey;

    JoinType(String joinKey) {
        this.joinKey = joinKey;
    }

    /**
     * 获取 SQL 中使用的 JOIN 关键字。
     *
     * @return JOIN 关键字文本
     */
    public String getJoinKey() {
        return joinKey;
    }
}
