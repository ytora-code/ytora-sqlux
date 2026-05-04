package xyz.ytora.sqlux.sql.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 原始 SQL 条件片段。
 *
 * <p>用于承载暂未结构化建模的条件语法，例如 EXISTS、子查询或数据库专属函数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class RawExpression implements Expression {

    private final String sql;

    private final List<Object> params;

    /**
     * 创建原始 SQL 条件表达式。
     *
     * <p>原始片段允许调用方直接提供 SQL 文本，同时仍然通过参数列表参与统一的占位符绑定。</p>
     *
     * @param sql 原始 SQL 片段，使用 {@code ?} 表示待绑定参数
     * @param params 与 {@code ?} 占位符顺序对应的参数列表
     */
    public RawExpression(String sql, List<Object> params) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("原始条件SQL不能为空");
        }
        this.sql = sql;
        if (params == null || params.isEmpty()) {
            this.params = Collections.emptyList();
        } else {
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
        }
    }

    /**
     * 获取原始 SQL 条件片段。
     *
     * @return 原始 SQL 文本
     */
    public String getSql() {
        return sql;
    }

    /**
     * 获取原始 SQL 条件需要绑定的参数。
     *
     * @return 不可变参数列表
     */
    public List<Object> getParams() {
        return params;
    }
}
