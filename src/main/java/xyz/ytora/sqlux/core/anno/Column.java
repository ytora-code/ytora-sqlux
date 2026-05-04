package xyz.ytora.sqlux.core.anno;

import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.orm.filler.FillerAdapter;
import xyz.ytora.sqlux.orm.filler.IFiller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示字段列
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface Column {

    /**
     * 字段真实名称
     */
    String value() default "";


    /**
     * 字段排序
     */
    int index() default Integer.MAX_VALUE;

    /**
     *字段是否非空
     */
    boolean notNull() default false;

    /**
     *字段是否唯一
     */
    boolean unique() default false;

    /**
     * 字段类型
     *
     * <p>默认情况下，表字段类型会根据实体字段 Java 类型自动推断。</p>
     *
     * <p>如果显式指定 {@code type}，则自动建表时优先使用该声明。
     * 该值优先按 Sqlux 内置标准类型解析，例如 {@code int8}、{@code varchar64}、{@code text}、
     * {@code datetime}、{@code blob} 等；不同数据库会再翻译为各自的具体字段类型。</p>
     *
     * <p>如果写入的值不是标准类型，则按原生数据库类型透传，
     * 例如历史代码中的 {@code varchar2(64)}、{@code number(19)} 仍然保持兼容。</p>
     */
    ColumnType type() default ColumnType.AUTO;

    /**
     * 字段注释
     */
    String comment() default "";

    /**
     * 字段是否存在
     */
    boolean exist() default true;

    /**
     * 字段自动填充时机。
     */
    FillType fillOn() default FillType.NONE;

    /**
     * 字段自动填充器。
     */
    Class<? extends IFiller> filler() default FillerAdapter.class;


}
