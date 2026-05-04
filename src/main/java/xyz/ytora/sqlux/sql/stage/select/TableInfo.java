package xyz.ytora.sqlux.sql.stage.select;

/**
 * 封装了{@code SELECT}查询中涉及的表
 *
 * @author ytora
 * @since 1.0
 */
public class TableInfo {

    /**
     * 表类型：1-物理表(class实体类) / 2-物理表(字符串直接指定表名称) / 3-虚拟表（子查询）
     */
    private final Integer tableType;

    /**
     * 实体性形式的表
     */
    private final Class<?> tableCls;

    /**
     * 字符串形式的表
     */
    private final String tableStr;

    /**
     * 子查询形式的表
     */
    private final AbsSelect subSelect;

    /**
     * 创建表信息对象。
     *
     * @param tableType 表类型标识
     * @param tableCls 实体类形式的表
     * @param tableStr 字符串形式的表名
     * @param subSelect 子查询形式的表
     */
    private TableInfo(Integer tableType, Class<?> tableCls, String tableStr, AbsSelect subSelect) {
        this.tableType = tableType;
        this.tableCls = tableCls;
        this.tableStr = tableStr;
        this.subSelect = subSelect;
    }

    /**
     * 使用实体类型创建表信息。
     *
     * @param tableCls 表实体类型
     * @return 表信息对象
     */
    public static TableInfo fromTableClazz(Class<?> tableCls) {
        return new TableInfo(1, tableCls, null, null);
    }

    /**
     * 使用字符串表名创建表信息。
     *
     * @param tableStr 数据库表名
     * @return 表信息对象
     */
    public static TableInfo fromTableStr(String tableStr) {
        return new TableInfo(2, null, tableStr, null);
    }

    /**
     * 使用子查询创建表信息。
     *
     * @param subSelect 子查询阶段对象
     * @return 表信息对象
     */
    public static TableInfo fromSubSelect(AbsSelect subSelect) {
        return new TableInfo(3, null, null, subSelect);
    }

    /**
     * 获取表类型标识。
     *
     * @return 1 表示实体表，2 表示字符串表名，3 表示子查询
     */
    public Integer tableType() {
        return tableType;
    }

    /**
     * 获取实体类形式的表。
     *
     * @return 表实体类型
     */
    public Class<?> tableCls() {
        return tableCls;
    }

    /**
     * 获取字符串形式的表名。
     *
     * @return 数据库表名
     */
    public String tableStr() {
        return tableStr;
    }

    /**
     * 获取子查询形式的表。
     *
     * @return 子查询阶段对象
     */
    public AbsSelect subSelect() {
        return subSelect;
    }

}
