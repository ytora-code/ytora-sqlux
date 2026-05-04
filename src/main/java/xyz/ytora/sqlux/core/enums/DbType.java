package xyz.ytora.sqlux.core.enums;

/**
 * 常见数据库产品枚举
 *
 * <p>该枚举用于选择 SQL 方言，也用于 JDBC metadata 自动探测结果归一化。</p>
 *
 * <p>使用示例：{@code SQL.select(...).from(User.class).toSql(DbType.POSTGRESQL)}。
 * 输入说明：作为方言选择参数传入。输出说明：翻译器生成对应数据库风格的 SQL。</p>
 *
 * @author ytora
 * @since 1.0
 */
public enum DbType {

    MYSQL("MySQL"),
    MARIADB("MariaDB"),
    POSTGRESQL("PostgreSQL"),
    ORACLE("Oracle"),
    DM("DM DBMS"),
    SQLSERVER("Microsoft SQL Server"),
    SQLite("SQLite"),
    DB2("DB2"),
    H2("H2"),
    DERBY("Apache Derby"),
    SYBASE("Sybase SQL Server"),
    INFORMIX("Informix Dynamic Server");

    DbType(String productName) {
        this.productName = productName;
    }

    private final String productName;

    /**
     * 获取 JDBC 元数据中对应的数据库产品名称。
     *
     * @return 数据库产品名称
     */
    public String getProductName() {
        return productName;
    }

    /**
     * 根据数据库产品名称获取对应的枚举值
     *
     * <p>示例：JDBC 返回 {@code PostgreSQL} 时，该方法返回 {@link #POSTGRESQL}。</p>
     *
     * @param databaseProductName 数据库产品名称，例如：MySQL、PostgreSQL、Oracle、DM DBMS、SQL Server
     * @return 对应的数据库类型枚举；无法识别时抛出异常
     */
    public static DbType fromString(String databaseProductName) {
        if (databaseProductName == null) {
            throw new IllegalArgumentException("Database product name cannot be null");
        }

        for (DbType dbType : DbType.values()) {
            if (dbType.productName.equalsIgnoreCase(databaseProductName)) {
                return dbType;
            }
        }
        if ("DM".equalsIgnoreCase(databaseProductName) || "Dameng".equalsIgnoreCase(databaseProductName)) {
            return DM;
        }
        if ("SQL Server".equalsIgnoreCase(databaseProductName)) {
            return SQLSERVER;
        }
        throw new IllegalArgumentException("未知的数据库: " + databaseProductName);
    }
}
