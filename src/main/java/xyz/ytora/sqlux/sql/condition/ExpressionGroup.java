package xyz.ytora.sqlux.sql.condition;

import xyz.ytora.sqlux.core.enums.Connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一组条件表达式。
 *
 * <p>嵌套的 {@code and(...)} 或 {@code or(...)} 会形成新的表达式组。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class ExpressionGroup implements Expression {

    private final List<ExpressionPart> parts = new ArrayList<>();

    /**
     * 向当前表达式组追加一个表达式片段。
     *
     * <p>连接符描述该片段与前一个片段的关系，首个片段的连接符在渲染时会被忽略。</p>
     *
     * @param connector 与前一个条件之间的连接符
     * @param expression 要追加的条件表达式
     */
    public void add(Connector connector, Expression expression) {
        if (expression != null) {
            parts.add(new ExpressionPart(connector, expression));
        }
    }

    /**
     * 判断当前表达式组是否没有任何有效条件。
     *
     * @return 没有条件时返回 {@code true}
     */
    public boolean isEmpty() {
        return parts.isEmpty();
    }

    /**
     * 获取表达式组中的所有条件片段。
     *
     * @return 不可变条件片段列表
     */
    public List<ExpressionPart> getParts() {
        return Collections.unmodifiableList(parts);
    }
}
