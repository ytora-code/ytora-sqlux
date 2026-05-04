package xyz.ytora.sqlux.orm.creator;

/**
 * 单个实体自动建表的执行结果。
 *
 * @author ytora
 * @since 1.0
 */
public class TableCreateResult {

    private final Class<?> entityClass;

    private final String tableName;

    private final boolean created;

    private final String ddl;

    public TableCreateResult(Class<?> entityClass, String tableName, boolean created, String ddl) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.created = created;
        this.ddl = ddl;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCreated() {
        return created;
    }

    public String getDdl() {
        return ddl;
    }
}
