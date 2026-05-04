package xyz.ytora.sqlux.orm;

import java.util.HashSet;
import java.util.Set;

/**
 * 默认的实体类注册器
 *
 * @author ytora
 * @since 1.0
 */
public class DefaultEntityRegister implements EntityRegister {

    private final Set<Class<? extends AbsEntity>> set = new HashSet<>();

    @Override
    public void register(Class<? extends AbsEntity> entityClazz) {
        set.add(entityClazz);
    }

    @Override
    public boolean contains(Class<? extends AbsEntity> entityClazz) {
        return set.contains(entityClazz);
    }

    @Override
    public Iterable<Class<? extends AbsEntity>> list() {
        return set;
    }
}
