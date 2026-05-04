package xyz.ytora.sqlux.sql.condition;

import xyz.ytora.sqlux.sql.model.SelectSubQuery;
import xyz.ytora.sqlux.sql.stage.select.AbsSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表达式参数规范化工具。
 *
 * @author ytora
 * @since 1.0
 */
final class ExpressionValues {

    private ExpressionValues() {
    }

    /**
     * 将可变参数转换为列表。
     *
     * @param values 原始可变参数
     * @return 参数列表
     */
    static List<Object> toList(Object... values) {
        List<Object> list = new ArrayList<>();
        if (values == null) {
            return list;
        }
        list.addAll(
                Arrays.stream(values).flatMap(value -> {
                    // 集合
                    if (value instanceof Collection) {
                        return ((Collection<?>) value).stream();
                    }
                    // 数组
                    else if (value.getClass().isArray()) {
                        return Arrays.stream((Object[]) value);
                    }
                    return Stream.of(value);
                }).collect(Collectors.toList())
        );
        return list;
    }

    /**
     * 将 Iterable 转换为列表。
     *
     * @param values 原始 Iterable
     * @return 值列表
     */
    static List<Object> toList(Iterable<?> values) {
        List<Object> list = new ArrayList<>();
        if (values == null) {
            return list;
        }
        for (Object value : values) {
            list.add(value);
        }
        return list;
    }

    /**
     * 规范化 IN/NOT IN 的参数。
     *
     * @param values 原始 IN 参数；单个 SELECT 阶段会被转换为子查询表达式
     * @return 可迭代的 IN 参数集合
     */
    static Iterable<?> normalizeInValues(Object... values) {
        if (values != null && values.length == 1 && values[0] instanceof AbsSelect) {
            List<Object> list = new ArrayList<>(1);
            list.add(toSubQuery((AbsSelect) values[0]));
            return list;
        }
        return toList(values);
    }

    /**
     * 规范化普通比较值。
     *
     * @param value 原始比较值
     * @return 子查询会转换为 {@link SelectSubQuery}，其他值原样返回
     */
    static Object normalizeValue(Object value) {
        if (value instanceof AbsSelect) {
            return toSubQuery((AbsSelect) value);
        }
        return value;
    }

    /**
     * 将 SELECT 阶段包装为表达式位置使用的子查询。
     *
     * @param subQuery SELECT 阶段对象
     * @return 子查询表达式；入参为空时返回 {@code null}
     */
    static SelectSubQuery toSubQuery(AbsSelect subQuery) {
        if (subQuery == null) {
            return null;
        }
        return new SelectSubQuery(subQuery.getQuery());
    }
}
