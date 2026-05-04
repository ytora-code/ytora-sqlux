package xyz.ytora.sqlux.rw;

import java.lang.reflect.Field;

/**
 * 带实体字段上下文的字段读取器。
 *
 * <p>当字段类型需要读取字段声明上的泛型、注解等元数据时，实现该接口。ORM 读取时会优先使用
 * 该接口，再回退到 {@link SqlReader}。</p>
 *
 * @author ytora
 * @since 1.0
 */
@FunctionalInterface
public interface SqlFieldReader {

    /**
     * 将数据库字段值转换为实体类字段值。
     *
     * @param value 数据库原始字段值
     * @param field 实体字段元数据
     * @return 实体字段值
     */
    Object read(Object value, Field field);
}
