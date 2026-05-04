package xyz.ytora.sqlux.sql;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.util.NamedUtil;

/**
 * 显式 SQL 表句柄。
 *
 * <p>该对象用于在同一个 SQL 中多次引用同一实体类时，显式绑定每个表别名，
 * 避免列引用只能通过 {@code Class<?> -> alias} 隐式解析。</p>
 *
 * <p>典型用法：</p>
 *
 * <pre>{@code
 * SqlTable<User> u = SQL.table(User.class, "u");
 * SqlTable<User> manager = SQL.table(User.class, "m");
 * SQL.select(u.col(User::getName), manager.col(User::getName))
 *     .from(u)
 *     .join(manager, on -> on.eq(u.col(User::getId), manager.col(User::getId)));
 * }</pre>
 *
 * @param <T> 实体类型
 */
public final class SqlTable<T> {

    private final Class<T> tableClass;

    private final String alias;

    SqlTable(Class<T> tableClass, String alias) {
        if (tableClass == null) {
            throw new IllegalArgumentException("表实体类型不能为空");
        }
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("表别名不能为空");
        }
        this.tableClass = tableClass;
        this.alias = alias.trim();
    }

    /**
     * 获取实体类型。
     *
     * @return 实体类型
     */
    public Class<T> getTableClass() {
        return tableClass;
    }

    /**
     * 获取表别名。
     *
     * @return 表别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 获取数据库表名。
     *
     * @return 数据库表名
     */
    public String getTableName() {
        return NamedUtil.parseTableName(tableClass);
    }

    /**
     * 基于当前表句柄创建绑定到该别名的列引用。
     *
     * @param column 实体 getter 方法引用
     * @param <R> 字段类型
     * @return 已绑定别名的列引用
     */
    public <R> ColumnRef col(ColFunction<T, R> column) {
        return ColumnRef.from(column).bind(alias);
    }
}