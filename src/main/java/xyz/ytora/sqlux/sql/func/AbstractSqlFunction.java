package xyz.ytora.sqlux.sql.func;

import xyz.ytora.sqlux.util.SqlRenderUtil;
import xyz.ytora.sqlux.translate.TranslateContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL 函数基类。
 *
 * <p>内置函数与用户自定义函数都可以复用该基类，实现统一的参数规范和渲染方式。</p>
 *
 * @author ytora
 * @since 1.0
 */
public abstract class AbstractSqlFunction implements SqlExpression {

    private final String name;

    private final List<Object> arguments;

    /**
     * 创建 SQL 函数表达式。
     *
     * @param name 函数名；渲染时会原样作为函数调用名称输出
     * @param arguments 函数参数；字段、表达式会渲染为 SQL，普通对象会作为绑定参数
     */
    protected AbstractSqlFunction(String name, Object... arguments) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("函数名不能为空");
        }
        this.name = name.trim();
        this.arguments = normalizeArguments(arguments);
    }

    /**
     * 获取函数参数列表。
     *
     * @return 不可变函数参数列表
     */
    protected final List<Object> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * 按通用函数调用格式渲染表达式。
     *
     * @param context 翻译上下文，用于渲染表达式参数和收集普通参数
     * @return 形如 {@code name(arg1, arg2)} 的 SQL 片段
     */
    @Override
    public String render(TranslateContext context) {
        List<String> items = new ArrayList<>();
        for (Object argument : arguments) {
            items.add(renderArgument(argument, context));
        }
        return name + "(" + SqlRenderUtil.join(items, ", ") + ")";
    }

    /**
     * 渲染单个函数参数。
     *
     * @param argument 参数
     * @param context 翻译上下文
     * @return 参数SQL片段
     */
    protected String renderArgument(Object argument, TranslateContext context) {
        if (argument == null) {
            return "NULL";
        }
        if (argument instanceof SqlExpression) {
            return SqlRenderUtil.expression((SqlExpression) argument, context);
        }
        return context.addParam(argument);
    }

    /**
     * 将可变参数规范化为可遍历列表。
     *
     * @param arguments 原始函数参数
     * @return 可修改的函数参数列表；入参为空时返回空列表
     */
    private static List<Object> normalizeArguments(Object... arguments) {
        List<Object> items = new ArrayList<>();
        if (arguments == null) {
            return items;
        }
        Collections.addAll(items, arguments);
        return items;
    }
}
