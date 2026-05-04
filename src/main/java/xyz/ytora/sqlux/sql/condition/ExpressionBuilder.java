package xyz.ytora.sqlux.sql.condition;

import xyz.ytora.sqlux.core.enums.Connector;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.sqlux.sql.func.SqlExpression;
import xyz.ytora.sqlux.sql.model.ColumnRef;
import xyz.ytora.sqlux.sql.stage.select.AbsSelect;
import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 条件表达式构造器。
 *
 * <p>用于 WHERE、ON 和 HAVING 子句。多个条件默认使用 {@code AND} 连接；调用 {@link #or()} 后，
 * 下一个条件使用 {@code OR} 连接；调用 {@link #and(Consumer)} 或 {@link #or(Consumer)} 会生成括号表达式。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public class ExpressionBuilder {

    /** 当前SQL的上下文对象。 */
    private final StageContextHolder contextHolder;

    /** 根表达式组。 */
    private final ExpressionGroup root = new ExpressionGroup();

    /** 下一个表达式与前一个表达式之间的连接符。 */
    private Connector nextConnector = Connector.AND;

    /**
     * 创建表达式构造器。
     *
     * @param contextHolder 当前SQL阶段上下文
     */
    public ExpressionBuilder(StageContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    /**
     * 指定下一个条件使用 AND 连接。
     *
     * @return 当前表达式构造器
     */
    public ExpressionBuilder and() {
        this.nextConnector = Connector.AND;
        return this;
    }

    /**
     * 使用 AND 连接一个括号表达式。
     *
     * @param nestedExpr 括号内部的条件表达式
     * @return 当前表达式构造器
     */
    public ExpressionBuilder and(Consumer<ExpressionBuilder> nestedExpr) {
        return nested(Connector.AND, nestedExpr);
    }

    /**
     * 指定下一个条件使用 OR 连接。
     *
     * @return 当前表达式构造器
     */
    public ExpressionBuilder or() {
        this.nextConnector = Connector.OR;
        return this;
    }

    /**
     * 使用 OR 连接一个括号表达式。
     *
     * @param nestedExpr 括号内部的条件表达式
     * @return 当前表达式构造器
     */
    public ExpressionBuilder or(Consumer<ExpressionBuilder> nestedExpr) {
        return nested(Connector.OR, nestedExpr);
    }

    /**
     * 添加等值条件：{@code column = value}。
     */
    public <T> ExpressionBuilder eq(ColFunction<T, ?> column, Object value) {
        return eq(true, column, value);
    }

    /**
     * 添加等值条件：{@code expression = value}。
     */
    public ExpressionBuilder eq(SqlExpression expression, Object value) {
        return eq(true, expression, value);
    }

    /**
     * 按条件添加等值条件：{@code column = value}。
     */
    public <T> ExpressionBuilder eq(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), "=", value);
    }

    /**
     * 按条件添加等值条件：{@code expression = value}。
     */
    public ExpressionBuilder eq(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, "=", value);
    }

    /**
     * 添加字段等值条件：{@code leftColumn = rightColumn}。
     */
    public <L, R> ExpressionBuilder eq(ColFunction<L, ?> leftColumn, ColFunction<R, ?> rightColumn) {
        return eq(true, leftColumn, rightColumn);
    }

    /**
     * 按条件添加字段等值条件：{@code leftColumn = rightColumn}。
     */
    public <L, R> ExpressionBuilder eq(boolean condition, ColFunction<L, ?> leftColumn, ColFunction<R, ?> rightColumn) {
        if (!condition) {
            resetConnector();
            return this;
        }
        return compare(true, ColumnRef.from(leftColumn), "=", ColumnRef.from(rightColumn));
    }

    /**
     * 添加表达式等值条件：{@code left = right}。
     */
    public ExpressionBuilder eq(SqlExpression left, SqlExpression right) {
        return compare(true, left, "=", right);
    }

    /**
     * 添加不等条件：{@code column != value}。
     */
    public <T> ExpressionBuilder ne(ColFunction<T, ?> column, Object value) {
        return ne(true, column, value);
    }

    /**
     * 添加不等条件：{@code expression != value}。
     */
    public ExpressionBuilder ne(SqlExpression expression, Object value) {
        return ne(true, expression, value);
    }

    /**
     * 按条件添加不等条件：{@code column != value}。
     */
    public <T> ExpressionBuilder ne(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), "!=", value);
    }

    /**
     * 按条件添加不等条件：{@code expression != value}。
     */
    public ExpressionBuilder ne(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, "!=", value);
    }

    /**
     * 添加字段不等条件：{@code leftColumn != rightColumn}。
     */
    public <L, R> ExpressionBuilder ne(ColFunction<L, ?> leftColumn, ColFunction<R, ?> rightColumn) {
        return ne(true, leftColumn, rightColumn);
    }

    /**
     * 按条件添加字段不等条件：{@code leftColumn != rightColumn}。
     */
    public <L, R> ExpressionBuilder ne(boolean condition, ColFunction<L, ?> leftColumn, ColFunction<R, ?> rightColumn) {
        if (!condition) {
            resetConnector();
            return this;
        }
        return compare(true, ColumnRef.from(leftColumn), "!=", ColumnRef.from(rightColumn));
    }

    /**
     * 添加表达式不等条件：{@code left != right}。
     */
    public ExpressionBuilder ne(SqlExpression left, SqlExpression right) {
        return compare(true, left, "!=", right);
    }

    /**
     * 添加大于条件：{@code column > value}。
     */
    public <T> ExpressionBuilder gt(ColFunction<T, ?> column, Object value) {
        return gt(true, column, value);
    }

    /**
     * 添加大于条件：{@code expression > value}。
     */
    public ExpressionBuilder gt(SqlExpression expression, Object value) {
        return gt(true, expression, value);
    }

    /**
     * 按条件添加大于条件：{@code column > value}。
     */
    public <T> ExpressionBuilder gt(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), ">", value);
    }

    /**
     * 按条件添加大于条件：{@code expression > value}。
     */
    public ExpressionBuilder gt(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, ">", value);
    }

    /**
     * 添加大于等于条件：{@code column >= value}。
     */
    public <T> ExpressionBuilder ge(ColFunction<T, ?> column, Object value) {
        return ge(true, column, value);
    }

    /**
     * 添加大于等于条件：{@code expression >= value}。
     */
    public ExpressionBuilder ge(SqlExpression expression, Object value) {
        return ge(true, expression, value);
    }

    /**
     * 按条件添加大于等于条件：{@code column >= value}。
     */
    public <T> ExpressionBuilder ge(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), ">=", value);
    }

    /**
     * 按条件添加大于等于条件：{@code expression >= value}。
     */
    public ExpressionBuilder ge(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, ">=", value);
    }

    /**
     * 添加小于条件：{@code column < value}。
     */
    public <T> ExpressionBuilder lt(ColFunction<T, ?> column, Object value) {
        return lt(true, column, value);
    }

    /**
     * 添加小于条件：{@code expression < value}。
     */
    public ExpressionBuilder lt(SqlExpression expression, Object value) {
        return lt(true, expression, value);
    }

    /**
     * 按条件添加小于条件：{@code column < value}。
     */
    public <T> ExpressionBuilder lt(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), "<", value);
    }

    /**
     * 按条件添加小于条件：{@code expression < value}。
     */
    public ExpressionBuilder lt(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, "<", value);
    }

    /**
     * 添加小于等于条件：{@code column <= value}。
     */
    public <T> ExpressionBuilder le(ColFunction<T, ?> column, Object value) {
        return le(true, column, value);
    }

    /**
     * 添加小于等于条件：{@code expression <= value}。
     */
    public ExpressionBuilder le(SqlExpression expression, Object value) {
        return le(true, expression, value);
    }

    /**
     * 按条件添加小于等于条件：{@code column <= value}。
     */
    public <T> ExpressionBuilder le(boolean condition, ColFunction<T, ?> column, Object value) {
        return compare(condition, ColumnRef.from(column), "<=", value);
    }

    /**
     * 按条件添加小于等于条件：{@code expression <= value}。
     */
    public ExpressionBuilder le(boolean condition, SqlExpression expression, Object value) {
        return compare(condition, expression, "<=", value);
    }

    /**
     * 添加模糊匹配条件：{@code column LIKE %value%}。
     */
    public <T> ExpressionBuilder like(ColFunction<T, ?> column, Object value) {
        return like(true, column, value);
    }

    /**
     * 添加模糊匹配条件：{@code expression LIKE %value%}。
     */
    public ExpressionBuilder like(SqlExpression expression, Object value) {
        return like(true, expression, value);
    }

    /**
     * 按条件添加模糊匹配条件：{@code column LIKE %value%}。
     */
    public <T> ExpressionBuilder like(boolean condition, ColFunction<T, ?> column, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, ColumnRef.from(column), "LIKE", "%" + value + "%");
    }

    /**
     * 按条件添加模糊匹配条件：{@code expression LIKE %value%}。
     */
    public ExpressionBuilder like(boolean condition, SqlExpression expression, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, expression, "LIKE", "%" + value + "%");
    }

    /**
     * 添加左模糊匹配条件：{@code column LIKE %value}。
     */
    public <T> ExpressionBuilder likeLeft(ColFunction<T, ?> column, Object value) {
        return likeLeft(true, column, value);
    }

    /**
     * 添加左模糊匹配条件：{@code expression LIKE %value}。
     */
    public ExpressionBuilder likeLeft(SqlExpression expression, Object value) {
        return likeLeft(true, expression, value);
    }

    /**
     * 按条件添加左模糊匹配条件：{@code column LIKE %value}。
     */
    public <T> ExpressionBuilder likeLeft(boolean condition, ColFunction<T, ?> column, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, ColumnRef.from(column), "LIKE", "%" + value);
    }

    /**
     * 按条件添加左模糊匹配条件：{@code expression LIKE %value}。
     */
    public ExpressionBuilder likeLeft(boolean condition, SqlExpression expression, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, expression, "LIKE", "%" + value);
    }

    /**
     * 添加右模糊匹配条件：{@code column LIKE value%}。
     */
    public <T> ExpressionBuilder likeRight(ColFunction<T, ?> column, Object value) {
        return likeRight(true, column, value);
    }

    /**
     * 添加右模糊匹配条件：{@code expression LIKE value%}。
     */
    public ExpressionBuilder likeRight(SqlExpression expression, Object value) {
        return likeRight(true, expression, value);
    }

    /**
     * 按条件添加右模糊匹配条件：{@code column LIKE value%}。
     */
    public <T> ExpressionBuilder likeRight(boolean condition, ColFunction<T, ?> column, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, ColumnRef.from(column), "LIKE", value + "%");
    }

    /**
     * 按条件添加右模糊匹配条件：{@code expression LIKE value%}。
     */
    public ExpressionBuilder likeRight(boolean condition, SqlExpression expression, Object value) {
        if (value == null) {
            return this;
        }
        return compare(condition, expression, "LIKE", value + "%");
    }

    /**
     * 添加 IS NULL 条件。
     */
    public <T> ExpressionBuilder isNull(ColFunction<T, ?> column) {
        return isNull(true, column);
    }

    /**
     * 添加 IS NULL 条件。
     */
    public ExpressionBuilder isNull(SqlExpression expression) {
        return isNull(true, expression);
    }

    /**
     * 按条件添加 IS NULL 条件。
     */
    public <T> ExpressionBuilder isNull(boolean condition, ColFunction<T, ?> column) {
        return unary(condition, ColumnRef.from(column), "IS NULL");
    }

    /**
     * 按条件添加 IS NULL 条件。
     */
    public ExpressionBuilder isNull(boolean condition, SqlExpression expression) {
        return unary(condition, expression, "IS NULL");
    }

    /**
     * 添加 IS NOT NULL 条件。
     */
    public <T> ExpressionBuilder isNotNull(ColFunction<T, ?> column) {
        return isNotNull(true, column);
    }

    /**
     * 添加 IS NOT NULL 条件。
     */
    public ExpressionBuilder isNotNull(SqlExpression expression) {
        return isNotNull(true, expression);
    }

    /**
     * 按条件添加 IS NOT NULL 条件。
     */
    public <T> ExpressionBuilder isNotNull(boolean condition, ColFunction<T, ?> column) {
        return unary(condition, ColumnRef.from(column), "IS NOT NULL");
    }

    /**
     * 按条件添加 IS NOT NULL 条件。
     */
    public ExpressionBuilder isNotNull(boolean condition, SqlExpression expression) {
        return unary(condition, expression, "IS NOT NULL");
    }

    /**
     * 添加 IN 条件。
     */
    public <T> ExpressionBuilder in(ColFunction<T, ?> column, Iterable<?> values) {
        return in(true, column, values);
    }

    /**
     * 添加 IN 条件。
     */
    public ExpressionBuilder in(SqlExpression expression, Iterable<?> values) {
        return in(true, expression, values);
    }

    /**
     * 按条件添加 IN 条件。
     */
    public <T> ExpressionBuilder in(boolean condition, ColFunction<T, ?> column, Iterable<?> values) {
        return collectionCompare(condition, ColumnRef.from(column), "IN", values);
    }

    /**
     * 按条件添加 IN 条件。
     */
    public ExpressionBuilder in(boolean condition, SqlExpression expression, Iterable<?> values) {
        return collectionCompare(condition, expression, "IN", values);
    }

    /**
     * 添加 IN 条件。
     */
    public <T> ExpressionBuilder in(ColFunction<T, ?> column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 添加 IN 条件。
     */
    public ExpressionBuilder in(SqlExpression expression, Object... values) {
        return in(true, expression, values);
    }

    /**
     * 按条件添加 IN 条件。
     */
    public <T> ExpressionBuilder in(boolean condition, ColFunction<T, ?> column, Object... values) {
        return collectionCompare(condition, ColumnRef.from(column), "IN", ExpressionValues.normalizeInValues(values));
    }

    /**
     * 按条件添加 IN 条件。
     */
    public ExpressionBuilder in(boolean condition, SqlExpression expression, Object... values) {
        return collectionCompare(condition, expression, "IN", ExpressionValues.normalizeInValues(values));
    }

    /**
     * 添加 NOT IN 条件。
     */
    public <T> ExpressionBuilder notIn(ColFunction<T, ?> column, Iterable<?> values) {
        return notIn(true, column, values);
    }

    /**
     * 添加 NOT IN 条件。
     */
    public ExpressionBuilder notIn(SqlExpression expression, Iterable<?> values) {
        return notIn(true, expression, values);
    }

    /**
     * 按条件添加 NOT IN 条件。
     */
    public <T> ExpressionBuilder notIn(boolean condition, ColFunction<T, ?> column, Iterable<?> values) {
        return collectionCompare(condition, ColumnRef.from(column), "NOT IN", values);
    }

    /**
     * 按条件添加 NOT IN 条件。
     */
    public ExpressionBuilder notIn(boolean condition, SqlExpression expression, Iterable<?> values) {
        return collectionCompare(condition, expression, "NOT IN", values);
    }

    /**
     * 添加 NOT IN 条件。
     */
    public <T> ExpressionBuilder notIn(ColFunction<T, ?> column, Object... values) {
        return notIn(true, column, values);
    }

    /**
     * 添加 NOT IN 条件。
     */
    public ExpressionBuilder notIn(SqlExpression expression, Object... values) {
        return notIn(true, expression, values);
    }

    /**
     * 按条件添加 NOT IN 条件。
     */
    public <T> ExpressionBuilder notIn(boolean condition, ColFunction<T, ?> column, Object... values) {
        return collectionCompare(condition, ColumnRef.from(column), "NOT IN", ExpressionValues.normalizeInValues(values));
    }

    /**
     * 按条件添加 NOT IN 条件。
     */
    public ExpressionBuilder notIn(boolean condition, SqlExpression expression, Object... values) {
        return collectionCompare(condition, expression, "NOT IN", ExpressionValues.normalizeInValues(values));
    }

    /**
     * 添加 BETWEEN 条件。
     */
    public <T> ExpressionBuilder between(ColFunction<T, ?> column, Object start, Object end) {
        return between(true, column, start, end);
    }

    /**
     * 添加 BETWEEN 条件。
     */
    public ExpressionBuilder between(SqlExpression expression, Object start, Object end) {
        return between(true, expression, start, end);
    }

    /**
     * 按条件添加 BETWEEN 条件。
     */
    public <T> ExpressionBuilder between(boolean condition, ColFunction<T, ?> column, Object start, Object end) {
        return rangeCompare(condition, ColumnRef.from(column), "BETWEEN", start, end);
    }

    /**
     * 按条件添加 BETWEEN 条件。
     */
    public ExpressionBuilder between(boolean condition, SqlExpression expression, Object start, Object end) {
        return rangeCompare(condition, expression, "BETWEEN", start, end);
    }

    /**
     * 添加 NOT BETWEEN 条件。
     */
    public <T> ExpressionBuilder notBetween(ColFunction<T, ?> column, Object start, Object end) {
        return notBetween(true, column, start, end);
    }

    /**
     * 添加 NOT BETWEEN 条件。
     */
    public ExpressionBuilder notBetween(SqlExpression expression, Object start, Object end) {
        return notBetween(true, expression, start, end);
    }

    /**
     * 按条件添加 NOT BETWEEN 条件。
     */
    public <T> ExpressionBuilder notBetween(boolean condition, ColFunction<T, ?> column, Object start, Object end) {
        return rangeCompare(condition, ColumnRef.from(column), "NOT BETWEEN", start, end);
    }

    /**
     * 按条件添加 NOT BETWEEN 条件。
     */
    public ExpressionBuilder notBetween(boolean condition, SqlExpression expression, Object start, Object end) {
        return rangeCompare(condition, expression, "NOT BETWEEN", start, end);
    }

    /**
     * 添加原始 SQL 条件片段。
     */
    public ExpressionBuilder raw(String sql, Object... params) {
        return raw(true, sql, params);
    }

    /**
     * 按条件添加原始 SQL 条件片段。
     */
    public ExpressionBuilder raw(boolean condition, String sql, Object... params) {
        if (!condition) {
            resetConnector();
            return this;
        }
        add(new RawExpression(sql, ExpressionValues.toList(params)));
        return this;
    }

    /**
     * 添加 EXISTS 条件。
     */
    public ExpressionBuilder exists(AbsSelect subQuery) {
        return exists(true, subQuery);
    }

    /**
     * 按条件添加 EXISTS 条件。
     */
    public ExpressionBuilder exists(boolean condition, AbsSelect subQuery) {
        return unary(condition, ExpressionValues.toSubQuery(subQuery), "EXISTS");
    }

    /**
     * 添加 NOT EXISTS 条件。
     */
    public ExpressionBuilder notExists(AbsSelect subQuery) {
        return notExists(true, subQuery);
    }

    /**
     * 按条件添加 NOT EXISTS 条件。
     */
    public ExpressionBuilder notExists(boolean condition, AbsSelect subQuery) {
        return unary(condition, ExpressionValues.toSubQuery(subQuery), "NOT EXISTS");
    }

    /**
     * 获取构造完成的表达式树。
     *
     * @return 表达式组
     */
    public ExpressionGroup toExpression() {
        return root;
    }

    /**
     * 获取当前 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * 添加普通二元比较条件。
     *
     * @param condition 是否真正追加该条件
     * @param left 条件左侧字段或表达式
     * @param operator SQL 比较运算符
     * @param value 条件右侧值或表达式
     * @return 当前表达式构造器
     */
    private ExpressionBuilder compare(boolean condition, Object left, String operator, Object value) {
        Object normalized = ExpressionValues.normalizeValue(value);
        if (!condition || left == null || normalized == null) {
            resetConnector();
            return this;
        }
        add(new ConditionExpression(left, operator, normalized));
        return this;
    }

    /**
     * 添加一元条件。
     *
     * @param condition 是否真正追加该条件
     * @param left 条件左侧字段、表达式或子查询
     * @param operator SQL 一元运算符
     * @return 当前表达式构造器
     */
    private ExpressionBuilder unary(boolean condition, Object left, String operator) {
        if (!condition || left == null) {
            resetConnector();
            return this;
        }
        add(new ConditionExpression(left, operator, null));
        return this;
    }

    /**
     * 添加集合比较条件。
     *
     * @param condition 是否真正追加该条件
     * @param left 条件左侧字段或表达式
     * @param operator SQL 集合运算符，例如 {@code IN}
     * @param values 集合右侧值
     * @return 当前表达式构造器
     */
    private ExpressionBuilder collectionCompare(boolean condition, Object left, String operator, Iterable<?> values) {
        if (!condition || left == null) {
            resetConnector();
            return this;
        }
        List<Object> normalized = ExpressionValues.toList(values);
        add(new ConditionExpression(left, operator, normalized));
        return this;
    }

    /**
     * 添加范围比较条件。
     *
     * @param condition 是否真正追加该条件
     * @param left 条件左侧字段或表达式
     * @param operator SQL 范围运算符，例如 {@code BETWEEN}
     * @param start 起始值
     * @param end 结束值
     * @return 当前表达式构造器
     */
    private ExpressionBuilder rangeCompare(boolean condition, Object left, String operator, Object start, Object end) {
        if (!condition || left == null || start == null || end == null) {
            resetConnector();
            return this;
        }
        List<Object> values = new ArrayList<>(2);
        values.add(start);
        values.add(end);
        add(new ConditionExpression(left, operator, values));
        return this;
    }

    /**
     * 添加嵌套括号表达式。
     *
     * @param connector 嵌套表达式与前一个表达式之间的连接符
     * @param nestedExpr 嵌套表达式构造回调
     * @return 当前表达式构造器
     */
    private ExpressionBuilder nested(Connector connector, Consumer<ExpressionBuilder> nestedExpr) {
        if (nestedExpr == null) {
            resetConnector();
            return this;
        }
        ExpressionBuilder nestedBuilder = new ExpressionBuilder(contextHolder);
        nestedExpr.accept(nestedBuilder);
        ExpressionGroup group = nestedBuilder.toExpression();
        if (!group.isEmpty()) {
            this.nextConnector = connector;
            add(group);
        } else {
            resetConnector();
        }
        return this;
    }

    /**
     * 将表达式按当前连接符追加到根表达式组。
     *
     * @param expression 待追加表达式
     */
    private void add(Expression expression) {
        root.add(nextConnector, expression);
        resetConnector();
    }

    /**
     * 将下一个表达式连接符恢复为默认的 AND。
     */
    private void resetConnector() {
        this.nextConnector = Connector.AND;
    }
}
