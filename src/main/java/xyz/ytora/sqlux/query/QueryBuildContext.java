package xyz.ytora.sqlux.query;

import xyz.ytora.sqlux.orm.EntityFieldMeta;
import xyz.ytora.sqlux.orm.EntityMetas;
import xyz.ytora.sqlux.query.model.QueryField;

import java.util.Map;

/**
 * 查询参数构建上下文。
 *
 * @author ytora
 * @since 1.0
 */
public class QueryBuildContext<T> {

    private final Class<T> entityType;

    private final Map<String, ?> params;

    private final QueryRuleCallback callback;

    private final QuerySpec spec;

    public QueryBuildContext(Class<T> entityType, Map<String, ?> params,
                             QueryRuleCallback callback, QuerySpec spec) {
        if (entityType == null) {
            throw new IllegalArgumentException("查询实体类型不能为空");
        }
        this.entityType = entityType;
        this.params = params;
        this.callback = callback == null ? QueryRuleCallback.NONE : callback;
        this.spec = spec == null ? new QuerySpec() : spec;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public Map<String, ?> getParams() {
        return params;
    }

    public QueryRuleCallback getCallback() {
        return callback;
    }

    public QuerySpec getSpec() {
        return spec;
    }

    /**
     * 按实体字段名或数据库列名解析查询字段。
     *
     * @param name 前端传入的字段名称
     * @return 查询字段元信息
     */
    public QueryField requireField(String name) {
        QueryField field = findField(name);
        if (field == null) {
            throw new IllegalArgumentException("实体类 " + entityType.getName() + " 中找不到查询字段: " + name);
        }
        return field;
    }

    /**
     * 按实体字段名或数据库列名查找查询字段。
     *
     * @param name 前端传入的字段名称
     * @return 查询字段元信息；找不到时返回 {@code null}
     */
    public QueryField findField(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String normalized = name.trim();
        for (EntityFieldMeta field : EntityMetas.get(entityType).getFields()) {
            if (normalized.equals(field.getField().getName()) || normalized.equals(field.getColumnName())) {
                return new QueryField(entityType, field);
            }
        }
        return null;
    }
}
