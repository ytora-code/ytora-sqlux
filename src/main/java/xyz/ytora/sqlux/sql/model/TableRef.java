package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.NamedUtil;

/**
 * SQL表引用。
 *
 * <p>保存实体类型、解析后的表名和当前 SQL 中使用的表别名。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class TableRef implements QuerySource {

    private final Class<?> tableClass;

    private final String tableName;

    private final String alias;

    /**
     * 创建表引用。
     *
     * @param tableClass 表对应的实体类型
     * @param alias 当前 SQL 中的表别名
     */
    public TableRef(Class<?> tableClass, String alias) {
        if (tableClass == null) {
            throw new IllegalArgumentException("表不能为空");
        }
        this.tableClass = tableClass;
        this.tableName = NamedUtil.parseTableName(tableClass);
        this.alias = alias;
    }

    /**
     * 获取表对应的实体类型。
     *
     * @return 实体类型
     */
    public Class<?> getTableClass() {
        return tableClass;
    }

    /**
     * 获取数据库表名。
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取当前 SQL 中的表别名。
     *
     * @return 表别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 将表引用渲染为 FROM/JOIN 数据源片段。
     *
     * @param context 翻译上下文，用于读取方言的标识符转义规则
     * @return 带方言转义和可选别名的表 SQL 片段
     */
    @Override
    public String renderSource(TranslateContext context) {
        String quotedTable = context.getDialect().quoteIdentifier(tableName);
        if (alias == null || alias.isEmpty()) {
            return quotedTable;
        }
        return quotedTable + " " + alias;
    }
}
