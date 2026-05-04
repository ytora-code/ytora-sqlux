package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.sql.model.SelectQuery;

/**
 * OFFSET阶段
 *
 * @author ytora
 * @since 1.0
 */
public class OffsetStage extends AbsSelect {

    /**
     * 创建 OFFSET 阶段。
     *
     * @param query SELECT 查询模型
     */
    public OffsetStage(SelectQuery query) {
        super(query);
    }

}
