package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.rw.SqlFieldReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.lang.reflect.*;

/**
 * 内置类型：自定义对象 JSON 字段。
 *
 * <p>用于将实体字段中的对象直接保存为数据库 JSON 字段。字段声明必须写明泛型，例如
 * {@code CustBean<SysUser>}。读取时框架会根据字段泛型反序列化数据库值，写入时会将内部对象序列化为 JSON。</p>
 *
 * @param <T> 自定义对象类型
 * @author ytora
 * @since 1.0
 */
public final class CustBean<T> implements SqlFieldReader, SqlWriter {

    private T value;

    private CustBean() {
    }

    private CustBean(T value) {
        this.value = value;
    }

    /**
     * 根据自定义对象创建 JSON 字段包装。
     *
     * @param value 自定义对象；可以为 {@code null}
     * @return JSON字段包装对象
     * @param <T> 自定义对象类型
     */
    public static <T> CustBean<T> of(T value) {
        return new CustBean<>(value);
    }

    /**
     * 创建空 JSON 字段包装。
     *
     * @return JSON字段包装对象
     * @param <T> 自定义对象类型
     */
    public static <T> CustBean<T> empty() {
        return new CustBean<>(null);
    }

    /**
     * 获取内部对象。
     *
     * @return 内部对象
     */
    public T getValue() {
        return value;
    }

    @Override
    public Object read(Object value, Field field) {
        if (value == null) {
            return null;
        }
        Type valueType = resolveValueType(field);
        Object bean = SQL.getSqluxGlobal().getSqluxJson().parse(String.valueOf(value), valueType);
        return CustBean.of(bean);
    }

    @Override
    public Object write() {
        return SQL.getSqluxGlobal().getSqluxJson().stringify(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private static Type resolveValueType(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("CustBean字段必须声明明确泛型: " + field.getDeclaringClass().getName()
                    + "." + field.getName());
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>) || !CustBean.class.equals(rawType)) {
            throw new IllegalArgumentException("字段不是CustBean类型: " + field.getDeclaringClass().getName()
                    + "." + field.getName());
        }
        Type actualType = parameterizedType.getActualTypeArguments()[0];
        if (actualType instanceof TypeVariable<?> || actualType instanceof WildcardType) {
            throw new IllegalArgumentException("CustBean字段泛型必须是明确类型: " + field.getDeclaringClass().getName()
                    + "." + field.getName());
        }
        return actualType;
    }
}
