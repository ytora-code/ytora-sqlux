package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * UPPER 函数。
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class Upper extends AbstractSqlFunction {

    /**
     * 创建 UPPER 函数表达式。
     *
     * @param argument 要转换为大写的字段或表达式
     */
    private Upper(Object argument) {
        super("upper", argument);
    }

    /**
     * 使用实体字段创建 UPPER 函数。
     *
     * @param column 要转换为大写的实体字段
     * @return UPPER 函数表达式
     * @param <T> 字段所属实体类型
     * @param <R> 字段值类型
     */
    public static <T, R> Upper of(ColFunction<T, R> column) {
        return new Upper(ColumnRef.from(column));
    }

    /**
     * 使用 SQL 表达式创建 UPPER 函数。
     *
     * @param expression 要转换为大写的 SQL 表达式
     * @return UPPER 函数表达式
     */
    public static Upper of(SqlExpression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("UPPER 表达式不能为空");
        }
        return new Upper(expression);
    }
}
