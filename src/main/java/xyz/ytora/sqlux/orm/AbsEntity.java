package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.enums.ColumnType;

/**
 * 实体类
 *
 * <p>业务端所有实体类，理论上都应该继承该基类</p>
 *
 * @author ytora 
 * @since 1.0
 */
public abstract class AbsEntity {

    /**
     * 主键
     */
    @Column(type = ColumnType.INT8, comment = "数据主键ID")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
