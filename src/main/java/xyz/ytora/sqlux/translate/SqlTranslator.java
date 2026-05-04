package xyz.ytora.sqlux.translate;

/**
 * SQL翻译器。
 *
 * <p>翻译器接收结构化查询模型，输出 SQL 文本和有序参数。</p>
 *
 * <p>使用示例：{@code new SelectTranslator(dialect).translate(selectQuery)}。
 * 输入说明：传入查询模型。输出说明：返回可提交给执行器的 {@link SqlResult}。</p>
 *
 * @author ytora
 * @since 1.0
 * @param <Q> 查询模型类型
 */
public interface SqlTranslator<Q> {

    /**
     * 翻译查询模型。
     *
     * @param query 查询模型；入参类型由泛型 {@code Q} 决定，例如 {@code SelectQuery}
     * @return SQL翻译结果；出参包含 SQL 文本、有序参数、SQL类型和源模型
     */
    SqlResult translate(Q query);
}
