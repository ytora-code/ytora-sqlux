package xyz.ytora.sqlux.rw;

import java.lang.reflect.Field;

/**
 * 字段类型处理器。
 *
 * <p>用于统一处理实体字段和数据库字段之间的读写转换。相比 {@link SqlReader}
 * 和 {@link SqlWriter}，该接口不要求字段类型自身实现转换逻辑，适合为枚举、值对象、
 * 第三方类型统一注册转换规则。</p>
 *
 * @param <T> 实体字段类型
 * @author ytora
 * @since 1.0
 */
public interface TypeHandler<T> {

    /**
     * 判断当前处理器是否支持指定字段类型。
     *
     * @param type 字段类型
     * @return 支持时返回 {@code true}
     */
    boolean supports(Class<?> type);

    /**
     * 将实体字段值转换为数据库写入值。
     *
     * @param value 实体字段值
     * @param field 实体字段元数据；手工传参场景可能为 {@code null}
     * @return 数据库写入值
     */
    Object write(T value, Field field);

    /**
     * 将数据库字段值转换为实体字段值。
     *
     * @param value 数据库原始值
     * @param field 实体字段元数据
     * @return 实体字段值
     */
    T read(Object value, Field field);
}
