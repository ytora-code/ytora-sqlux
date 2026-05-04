package xyz.ytora.sqlux.core.json;

import java.lang.reflect.Type;

/**
 * sqlux内部使用的 JSON 编解码接口。
 *
 * <p>sqlux 本身不依赖任何 JSON 组件。使用者可以在自己的项目中用 Jackson、Gson、Fastjson2
 * 或其他 JSON 组件实现该接口，并通过 {@code SqluxGlobal.setSqluxJson(...)} 注册给框架使用。</p>
 *
 * @author ytora 
 * @since 1.0
 */
public interface SqluxJson {

    /**
     * 将 JSON 字符串解析为 Java 对象树。
     *
     * @param json JSON字符串；入参不能为 {@code null}
     * @return 解析后的对象树，通常是 {@code Map}、{@code List} 或标量值
     */
    Object parse(String json);

    /**
     * 将 JSON 字符串解析为指定 Java 类型。
     *
     * @param json JSON字符串；入参不能为 {@code null}
     * @param type 目标类型；可以是普通 {@code Class}，也可以是带泛型的 {@code Type}
     * @return 解析后的目标对象
     */
    Object parse(String json, Type type);

    /**
     * 将 Java 对象树序列化为合法 JSON 字符串。
     *
     * @param value Java对象树；可以为 {@code null}
     * @return JSON字符串
     */
    String stringify(Object value);

}
