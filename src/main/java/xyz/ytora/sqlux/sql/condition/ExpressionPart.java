package xyz.ytora.sqlux.sql.condition;

import xyz.ytora.sqlux.core.enums.Connector;

/**
 * 条件表达式片段。
 *
 * <p>连接符表示当前表达式和前一个表达式之间的关系；第一个表达式的连接符会被翻译器忽略。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class ExpressionPart {

    private final Connector connector;

    private final Expression expression;

    /**
     * 创建一个带连接符的表达式片段。
     *
     * @param connector 当前表达式与前一个表达式之间的连接符
     * @param expression 当前表达式内容
     */
    public ExpressionPart(Connector connector, Expression expression) {
        this.connector = connector;
        this.expression = expression;
    }

    /**
     * 获取当前片段使用的连接符。
     *
     * @return {@code AND} 或 {@code OR}
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * 获取当前片段承载的表达式。
     *
     * @return 条件表达式或嵌套表达式组
     */
    public Expression getExpression() {
        return expression;
    }
}
