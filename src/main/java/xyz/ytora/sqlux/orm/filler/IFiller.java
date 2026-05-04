package xyz.ytora.sqlux.orm.filler;

/**
 * 字段的自动填充器
 *
 * <p>新增或编辑数据时，可以对字段设置自动填充规则</p>
 * <p>适用于create_time,create_by,update_time,update_by等字段</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface IFiller {

    /**
     * 执行insert操作时，如果对应字段为空，则回调该方法，并将该方法的返回值写进数据库
     * @return 写进数据库的值
     */
    Object onInsert();

    /**
     * 执行update操作时回调该方法，如果对应字段为空，则回调该方法，并将该方法的返回值写进数据库
     * @return 写进数据库的值
     */
    Object onUpdate();

    /**
     * INSERT 时字段已有值是否仍然覆盖。
     *
     * <p>默认不覆盖，优先尊重使用者手工设置的字段值。</p>
     *
     * @return 需要覆盖已有值时返回 {@code true}
     */
    default boolean overwriteOnInsert() {
        return false;
    }

    /**
     * UPDATE 时字段已有值是否仍然覆盖。
     *
     * <p>默认覆盖，适合 update_time、update_by 等每次更新都应刷新的字段。</p>
     *
     * @return 需要覆盖已有值时返回 {@code true}
     */
    default boolean overwriteOnUpdate() {
        return true;
    }

}
