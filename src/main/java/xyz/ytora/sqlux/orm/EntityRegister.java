package xyz.ytora.sqlux.orm;

/**
 * 实体类注册器
 *
 * @author ytora
 * @since 1.0
 */
public interface EntityRegister {

    /**
     * 注册实体类
     * @param entityClazz 实体类型
     */
    void register(Class<? extends AbsEntity> entityClazz);

    /**
     * 判断指定实体类型是否已经注册
     * @param entityClazz 实体类型
     * @return true：已经注册； false：没有注册
     */
    boolean contains(Class<? extends AbsEntity> entityClazz);

    /**
     * 获取所有实体类
     * @return 实体类列表
     */
    Iterable<Class<? extends AbsEntity>> list();

}
