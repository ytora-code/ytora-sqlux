package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.NamedUtil;

/**
 * SELECT 中的表级通配字段，例如 {@code u.*}。
 *
 * @author ytora
 * @since 1.0
 */
public class TableWildcard implements SqlExpression {

    private final Class<?> tableClass;

    /**
     * 创建表级通配字段。
     *
     * @param tableClass 表实体类型
     */
    private TableWildcard(Class<?> tableClass) {
        if (tableClass == null) {
            throw new IllegalArgumentException("表实体类型不能为空");
        }
        this.tableClass = tableClass;
    }

    /**
     * 从实体类型创建表级通配字段。
     *
     * @param tableClass 表实体类型
     * @return 表级通配字段
     */
    public static TableWildcard of(Class<?> tableClass) {
        return new TableWildcard(tableClass);
    }

    /**
     * 渲染为当前查询上下文中的表别名通配字段。
     *
     * @param context 翻译上下文
     * @return 表级通配 SQL 片段
     */
    @Override
    public String render(TranslateContext context) {
        String alias = context.getStageContextHolder().getAlias(tableClass);
        if (alias == null || alias.isEmpty()) {
            alias = context.getDialect().quoteIdentifier(NamedUtil.parseTableName(tableClass));
        }
        return alias + ".*";
    }
}
