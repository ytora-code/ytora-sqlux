package xyz.ytora.sqlux.sql.condition;

/**
 * 一个原子条件表达式。
 *
 * <p>例如：{@code u.name = ?}</p>
 *
 * @author ytora
 * @since 1.0
 */
public class ConditionExpression implements Expression {

    private final Object left;

    private final String operator;

    private final Object right;

    /**
     * 创建一个二元条件表达式。
     *
     * <p>该对象只保存条件的左值、运算符和右值，真正的字段名转义、参数占位符生成由翻译器完成。</p>
     *
     * @param left 条件左侧表达式；可以是字段引用、函数表达式或原始值
     * @param operator SQL 运算符，例如 {@code =}、{@code IN}、{@code IS NULL}
     * @param right 条件右侧表达式或参数值；一元运算符场景下可以为 {@code null}
     */
    public ConditionExpression(Object left, String operator, Object right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * 获取条件左侧表达式。
     *
     * @return 条件左侧表达式或参数值
     */
    public Object getLeft() {
        return left;
    }

    /**
     * 获取 SQL 条件运算符。
     *
     * @return 运算符文本
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 获取条件右侧表达式。
     *
     * @return 条件右侧表达式、参数值或 {@code null}
     */
    public Object getRight() {
        return right;
    }
}
