package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.sql.model.SelectQuery;

/**
 * LIMIT阶段
 *
 * @author ytora 
 * @since 1.0
 */
public class LimitStage extends AbsSelect {

    /**
     * 创建 LIMIT 阶段。
     *
     * @param query SELECT 查询模型
     */
    public LimitStage(SelectQuery query) {
        super(query);
    }

    /**
     * LIMIT 后可能是 OFFSET 子句
     */
    public OffsetStage offset(Integer offset) {
        return offsetStage(offset);
    }

}
