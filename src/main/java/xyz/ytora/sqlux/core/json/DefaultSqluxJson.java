package xyz.ytora.sqlux.core.json;

import java.lang.reflect.Type;

/**
 * 默认的 SqluxJson 实现。
 *
 * <p>该实现不做真实 JSON 处理，只用于在用户未注册 JSON 组件时给出明确错误。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class DefaultSqluxJson implements SqluxJson {

    private static final String MESSAGE = "未配置SqluxJson实现，请使用 SqluxGlobal.setSqluxJson(...) 注册你项目中的JSON组件";

    @Override
    public Object parse(String json) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public Object parse(String json, Type type) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public String stringify(Object value) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
