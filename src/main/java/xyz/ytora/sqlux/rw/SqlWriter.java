package xyz.ytora.sqlux.rw;

/**
 * 字段写入器
 *
 * <p>指定将实体类字段值写入数据库的规则</p>
 * <p>实体类字段的类型如果实现了{@code IWriter}，则真正写入数据库的是write的返回值<p/>
 *
 * @author ytora
 * @since 1.0
 */
@FunctionalInterface
public interface SqlWriter {

    /**
     * 将当前对象转换后写入数据库
     * @return 真正写入数据库的值
     */
    Object write();

}
