package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL翻译上下文。
 *
 * <p>保存当前方言、阶段上下文和翻译过程中收集到的有序参数。</p>
 *
 * <p>使用示例：翻译器调用 {@code context.addParam("ytora")} 后，会得到占位符 {@code ?}，
 * 同时参数列表变为 {@code ["ytora"]}。输入说明：传入方言和阶段上下文。输出说明：提供占位符和有序参数。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class TranslateContext {

    private final Dialect dialect;

    private final StageContextHolder stageContextHolder;

    private final List<Object> params = new ArrayList<>();

    /**
     * 创建翻译上下文。
     *
     * @param dialect SQL方言；入参决定标识符引用和参数占位符格式
     * @param stageContextHolder SQL阶段上下文；入参用于根据实体类型查找表别名
     */
    public TranslateContext(Dialect dialect, StageContextHolder stageContextHolder) {
        this.dialect = dialect;
        this.stageContextHolder = stageContextHolder;
    }

    /**
     * 获取 SQL 方言。
     *
     * @return SQL方言；出参用于渲染表名、字段名和占位符
     */
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * 获取 SQL 阶段上下文。
     *
     * @return SQL阶段上下文；出参用于字段所属实体到表别名的解析
     */
    public StageContextHolder getStageContextHolder() {
        return stageContextHolder;
    }

    /**
     * 添加参数并返回对应占位符。
     *
     * <p>示例：第一次调用 {@code addParam(1)} 返回 {@code ?}，参数列表追加 {@code 1}。</p>
     *
     * @param value 参数值；入参会按调用顺序追加到参数列表
     * @return 参数占位符；出参由当前方言决定
     */
    public String addParam(Object value) {
        params.add(value);
        return dialect.placeholder(params.size());
    }

    /**
     * 批量追加参数。
     *
     * @param values 参数列表
     */
    public void addParams(List<Object> values) {
        if (values == null) {
            return;
        }
        params.addAll(values);
    }

    /**
     * 获取翻译过程中收集到的有序参数。
     *
     * @return 参数列表；出参顺序与 SQL 中占位符顺序一致
     */
    public List<Object> getParams() {
        return params;
    }
}
