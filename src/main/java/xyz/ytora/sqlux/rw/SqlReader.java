package xyz.ytora.sqlux.rw;

/**
 * 字段读取器
 *
 * <p>指定将数据库字段值读取为实体类字段值的规则</p>
 * <p>实体类字段的类型如果实现了{@code IReader}，则会使用read的返回值作为该字段的值<p/>
 *
 * @author ytora
 * @since 1.0
 */
@FunctionalInterface
public interface SqlReader {

    /**
     * 将数据字段值转为实体类字段类
     * @param value 数据库原始字段值
     * @return 实体类字段
     */
    Object read(Object value);

}
