package xyz.ytora.sqlux.sql.func;

import java.io.Serializable;

/**
 * 接收方法引用类型的参数
 *
 * <p>数据库表中列字段的抽象表示</p>
 *
 * @author ytora 
 * @since 1.0
 */
@FunctionalInterface
public interface ColFunction<T, R> extends Serializable {
    R apply(T t);
}
