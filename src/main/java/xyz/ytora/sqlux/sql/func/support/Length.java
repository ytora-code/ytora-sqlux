package xyz.ytora.sqlux.sql.func.support;

import xyz.ytora.sqlux.sql.func.AbstractSqlFunction;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;

/**
 * LENGTH 函数。
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class Length extends AbstractSqlFunction {

    /**
     * 创建 LENGTH 函数表达式。
     *
     * @param argument 要计算长度的字段或表达式
     */
    private Length(Object argument) {
        super("length", argument);
    }

    /**
     * 使用实体字段创建 LENGTH 函数。
     *
     * @param column 要计算长度的实体字段
     * @return LENGTH 函数表达式
     * @param <T> 字段所属实体类型
     * @param <R> 字段值类型
     */
    public static <T, R> Length of(ColFunction<T, R> column) {
        return new Length(ColumnRef.from(column));
    }

    /**
     * 使用 SQL 表达式创建 LENGTH 函数。
     *
     * @param expression 要计算长度的 SQL 表达式
     * @return LENGTH 函数表达式
     */
    public static Length of(SqlExpression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("LENGTH 表达式不能为空");
        }
        return new Length(expression);
    }
}
