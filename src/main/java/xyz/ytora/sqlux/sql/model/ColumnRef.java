package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.util.SqlRenderUtil;
import xyz.ytora.sqlux.translate.TranslateContext;
import xyz.ytora.sqlux.util.NamedUtil;

/**
 * SQL字段引用。
 *
 * <p>由实体 getter 方法引用解析而来，保留字段所属实体、字段名和原始方法引用。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class ColumnRef implements SqlExpression {

    private final Class<?> tableClass;

    private final String columnName;

    private final ColFunction<?, ?> source;

    private final boolean rawExpression;

    private final String explicitAlias;

    /**
     * 创建字段引用。
     *
     * @param tableClass 字段所属实体类型；原始表达式没有实体类型
     * @param columnName 数据库字段名或原始 SQL 表达式
     * @param source 原始 getter 方法引用；非方法引用构造时为 {@code null}
     * @param rawExpression 是否为原始 SQL 表达式
     * @param explicitAlias 显式绑定的表别名
     */
    private ColumnRef(Class<?> tableClass, String columnName, ColFunction<?, ?> source,
                      boolean rawExpression, String explicitAlias) {
        this.tableClass = tableClass;
        this.columnName = columnName;
        this.source = source;
        this.rawExpression = rawExpression;
        this.explicitAlias = explicitAlias;
    }

    /**
     * 从字段方法引用创建字段引用。
     *
     * @param column 字段 getter 方法引用；入参不能为 {@code null}
     * @return 字段引用
     */
    public static ColumnRef from(ColFunction<?, ?> column) {
        if (column == null) {
            throw new IllegalArgumentException("字段不能为空");
        }
        return new ColumnRef(NamedUtil.parseColumnOwner(column), NamedUtil.parseColumnName(column), column, false, null);
    }

    /**
     * 从实体类型和数据库字段名创建字段引用。
     *
     * @param tableClass 字段所属实体类型；入参不能为 {@code null}
     * @param columnName 数据库字段名；入参不能为空白字符串
     * @return 字段引用
     */
    public static ColumnRef of(Class<?> tableClass, String columnName) {
        return of(tableClass, columnName, null);
    }

    /**
     * 从实体类型、数据库字段名和显式别名创建字段引用。
     *
     * @param tableClass 字段所属实体类型
     * @param columnName 数据库字段名
     * @param explicitAlias 显式表别名
     * @return 字段引用
     */
    public static ColumnRef of(Class<?> tableClass, String columnName, String explicitAlias) {
        if (tableClass == null) {
            throw new IllegalArgumentException("字段所属实体类型不能为空");
        }
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("字段名不能为空");
        }
        return new ColumnRef(tableClass, columnName, null, false, normalizeAlias(explicitAlias));
    }

    /**
     * 创建原始 SQL 字段表达式。
     *
     * @param expression 原始 SQL 表达式
     * @return 原始字段表达式引用
     */
    public static ColumnRef raw(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("原始字段表达式不能为空");
        }
        return new ColumnRef(null, expression, null, true, null);
    }

    /**
     * 将当前字段引用绑定到指定别名。
     *
     * @param alias 表别名
     * @return 新的字段引用
     */
    public ColumnRef bind(String alias) {
        if (rawExpression) {
            return this;
        }
        return new ColumnRef(tableClass, columnName, source, false, normalizeAlias(alias));
    }

    /**
     * 获取字段所属实体类型。
     *
     * @return 实体类型；原始 SQL 表达式返回 {@code null}
     */
    public Class<?> getTableClass() {
        return tableClass;
    }

    /**
     * 获取数据库字段名或原始表达式文本。
     *
     * @return 字段名或原始 SQL 表达式
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 获取创建该字段引用的原始方法引用。
     *
     * @return getter 方法引用；非方法引用创建时返回 {@code null}
     */
    public ColFunction<?, ?> getSource() {
        return source;
    }

    /**
     * 判断当前字段是否是原始 SQL 表达式。
     *
     * @return 原始表达式返回 {@code true}
     */
    public boolean isRawExpression() {
        return rawExpression;
    }

    /**
     * 获取显式绑定的表别名。
     *
     * @return 表别名；未显式绑定时返回 {@code null}
     */
    public String getExplicitAlias() {
        return explicitAlias;
    }

    /**
     * 将字段引用渲染为 SQL 字段片段。
     *
     * @param context 翻译上下文，用于解析表别名和方言转义
     * @return 字段 SQL 片段
     */
    @Override
    public String render(TranslateContext context) {
        return SqlRenderUtil.column(this, context);
    }

    /**
     * 规范化显式表别名。
     *
     * @param alias 原始别名
     * @return 去除空白后的别名；空白别名返回 {@code null}
     */
    private static String normalizeAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return null;
        }
        return alias.trim();
    }
}
