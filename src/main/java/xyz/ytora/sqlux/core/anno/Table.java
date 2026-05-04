package xyz.ytora.sqlux.core.anno;

import xyz.ytora.sqlux.core.enums.IdType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识实体类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Table {

    /**
     * 表名称
     */
    String value() default "";

    /**
     * 主键列，默认id
     */
    String[] key() default {"id"};

    /**
     * 数据库表的主键策略
     */
    IdType idType() default IdType.NONE;

    /**
     * 表注释
     */
    String comment() default "";

    /**
     * 该表所属的数据源
     */
    String ds() default "";

}
