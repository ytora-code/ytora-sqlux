package xyz.ytora.sqlux.orm;

/**
 * 持久层
 *
 * <p>对于遵循MVC设计原则的架构来说，都会有一个持久层，持久层都应该实现该类</p>
 * @param <T> 该持久层对于的实体类型
 *
 * @author ytora
 * @since 1.0
 */
public interface IRepo<T extends AbsEntity> {
}
