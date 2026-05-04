# ytora-sqlux

`ytora-sqlux` 是一个基于 JDK 8 的数据库操作框架，设计哲学是`代码即SQL`。它尽量贴近 SQL 的表达方式，同时保留 ORM 的实体映射、自动填充、逻辑删除、乐观锁、自动建表、元数据读取等能力。

🍉例如：

```sql
List<User> users = SQL.select(User::getId, User::getUserName, Person::getAge)
        .from(User.class)
        .where(w -> w.eq(User::getUserName, "alice"))
        leftJoin(Person.class, on -> on.eq(User::getPid, Person::getId))
        .submit(User.class);
```

等价于下面SQL：

```sql
select 
	u1.id, u1.user_name, p2.age 
from 
	user u1 
left join 
	person p2 
on 
	u1.pid = p2.id 
where 
	u1.user_name = 'alice'
```



`submit`将翻译出SQL，并将SQL交给数据库执行，然后将查询结果集封装为List集合返回。

------



## 1. 依赖与初始化

### 1.1 Maven 依赖

```xml
<dependency>
    <groupId>xyz.ytora</groupId>
    <artifactId>ytora-sqlux</artifactId>
    <version>1.0</version>
</dependency>
```

### 1.2 初始化全局配置

框架所有入口都从 `SQL` 开始。在真正执行 SQL 之前，需要先注册全局配置。

```java
import xyz.ytora.sqlux.core.IConnectionProvider;
import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.SqluxGlobal;
import xyz.ytora.sqlux.core.enums.DbType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

SqluxGlobal global = new SqluxGlobal();
// 设置数据库类型
global.setDefaultDbType(DbType.POSTGRESQL);
// 注册数据库连接提供器，告诉Sqlux如何获取连接和关闭连接
global.registerConnectionProvider(new IConnectionProvider() {
    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/app",
                    "postgres",
                    "postgres"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
});
```

输入示例：

```java
SQL.rawQuery("select 1 as value").submit();
```

输出示例：

```java
[{value=1}]
```

------



## 2. 实体映射

负责把 Java 实体和数据库表对应起来。

### 2.1 定义实体

核心注解：

- `@Table`：声明表名、主键、主键策略、数据源
- `@Column`：声明列名、列类型、非空、唯一、自动填充、是否持久化
- `AbsEntity`：推荐所有实体继承，默认带 `id` 字段

```java
import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.core.enums.ColumnType;
import xyz.ytora.sqlux.core.enums.FillType;
import xyz.ytora.sqlux.core.enums.IdType;
import xyz.ytora.sqlux.orm.AbsEntity;
import xyz.ytora.sqlux.orm.type.Json;
import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Uuid;
import xyz.ytora.sqlux.orm.type.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(value = "sys_user", key = {"id"}, idType = IdType.SNOWFLAKE, comment = "用户表")
public class User extends AbsEntity {

    @Column(value = "login_name", type = ColumnType.VARCHAR64, notNull = true)
    private String loginName;

    @Column(type = ColumnType.INT4)
    private Integer age;

    @Column(type = ColumnType.INT8)
    private String deptId;

    @Column(type = ColumnType.DECIMAL_19_4)
    private BigDecimal salary;

    @Column(type = ColumnType.JSON)
    private Json profile;

    @Column(type = ColumnType.UUID)
    private Uuid externalId;

    @Column(type = ColumnType.TEXT)
    private String remark;

    @Column(type = ColumnType.DATETIME, fillOn = FillType.INSERT)
    private LocalDateTime createdAt;

    @Column
    private Version version;

    @Column
    private LogicDelete deleted;

    @Column(exist = false)
    private String transientNote;

    // getter / setter
}
```

```java
@Table(value = "sys_dept", key = {"id"}, idType = IdType.SNOWFLAKE)
public class Dept extends AbsEntity {

    @Column(value = "dept_name", type = ColumnType.VARCHAR64, notNull = true)
    private String deptName;

    // getter / setter
}
```

```java
@Table(value = "sys_user_archive", key = {"id"}, idType = IdType.SNOWFLAKE)
public class UserArchive extends AbsEntity {

    @Column(value = "login_name", type = ColumnType.VARCHAR64)
    private String loginName;

    @Column(type = ColumnType.INT4)
    private Integer age;

    // getter / setter
}
```

输入示例：

```java
User user = new User();
user.setLoginName("alice");
user.setAge(18);
user.setProfile(Json.object());
user.getProfile().put("team", "core");
```

输出示例：

```java
user.getLoginName() == "alice"
user.getProfile().getString("team") == "core"
```

### 2.2 主键策略

`@Table.idType()` 支持：

- `NONE`：主键由数据库生成，插入后回填 generated key
- `SNOWFLAKE`：框架插入前生成雪花 ID
- `UUID`：框架插入前生成 UUID
- `ULID`：框架插入前生成 ULID

输入示例：

```java
@Table(value = "order_info", key = {"id"}, idType = IdType.UUID)
public class Order extends AbsEntity {
}
```

输出示例：

```java
插入前自动生成主键，无需手动 setId(...)
```

### 2.3 常用内置字段语义

- `Version`：乐观锁字段，更新时自动生成 `version = version + 1`
- `LogicDelete`：逻辑删除字段，`delete` 会翻译成 `update deleted = 1`
- `Json`：自动在数据库字符串与 JSON 对象之间转换
- `Uuid`：自动在 UUID 对象与数据库字符串之间转换

输入示例：

```java
user.setVersion(Version.initial());
user.setDeleted(LogicDelete.normal());
user.setExternalId(Uuid.of("123e4567-e89b-12d3-a456-426614174000"));
```

输出示例：

```java
version = 1
deleted = 0
external_id = "123e4567-e89b-12d3-a456-426614174000"
```

------



## 3. 查询 SELECT

🍎Sqlux最核心的能力，它用接近 SQL 的链式 API 描述查询。

### 3.1 基础查询

作用：查询单表数据，并映射为实体或 `Map`。

```java
List<User> users = SQL.select()
        .from(User.class)
        .where(w -> w.eq(User::getLoginName, "alice"))
        .submit(User.class);
```

输入示例：

```java
loginName = "alice"
```

输出示例：

```java
[
  User{id="1", loginName="alice", age=18}
]
```

### 3.2 指定查询列

作用：只查需要的字段，减少返回数据。

```java
List<java.util.Map<String, Object>> rows = SQL.select(User::getLoginName, User::getAge)
        .from(User.class)
        .submit();
```

输入示例：

```java
select columns = loginName, age
```

输出示例：

```java
[
  {login_name=alice, age=18},
  {login_name=bob, age=28}
]
```

### 3.3 条件表达式

作用：构造 `where / on / having` 条件。

支持的常用操作：

- `eq / ne / gt / ge / lt / le`
- `like / likeLeft / likeRight`
- `isNull / isNotNull`
- `in / notIn`
- `between / notBetween`
- `exists / notExists`
- `raw`
- `and(...) / or(...)` 分组

```java
List<User> users = SQL.select()
        .from(User.class)
        .where(w -> {
            w.ge(User::getAge, 18);
            w.and(g -> {
                g.likeRight(User::getLoginName, "a");
                g.or().likeRight(User::getLoginName, "b");
            });
        })
        .submit(User.class);
```

输入示例：

```java
age >= 18
loginName like "a%" or "b%"
```

输出示例：

```java
[
  User{loginName="alice", age=18},
  User{loginName="bob", age=28}
]
```

### 3.4 联表查询

作用：写结构化 join，不再手写表名和列名。

```java
List<java.util.Map<String, Object>> rows = SQL.select(User::getLoginName, Dept::getDeptName)
        .from(User.class)
        .leftJoin(Dept.class, on -> on.eq(User::getDeptId, Dept::getId))
        .where(w -> w.eq(Dept::getDeptName, "engineering"))
        .submit();
```

输入示例：

```java
user.dept_id = dept.id
deptName = "engineering"
```

输出示例：

```java
[
  {login_name=alice, dept_name=engineering},
  {login_name=bob, dept_name=engineering}
]
```

### 3.5 排序、分组、分页

作用：补齐 `group by / having / order by / limit / offset`。

```java
Page<User> page = SQL.select()
        .from(User.class)
        .where(w -> w.ge(User::getAge, 18))
        .orderByDesc(User::getAge)
        .submit(Page.of(1, 2));
```

输入示例：

```java
pageNo = 1
pageSize = 2
```

输出示例：

```java
page.getTotal() == 5
page.getPages() == 3
page.getRecords().size() == 2
```

### 3.6 子查询与 SQL 函数

作用：在 DSL 内表达子查询、聚合函数、别名函数。

```java
import static xyz.ytora.sqlux.sql.func.support.Count.of;

java.util.List<java.util.Map<String, Object>> rows = SQL.select(
                of(User::getId).as("total")
        )
        .from(User.class)
        .where(w -> w.exists(
                SQL.select()
                        .from(Dept.class)
                        .where(x -> x.eq(Dept::getId, User::getDeptId))
        ))
        .submit();
```

输入示例：

```java
统计存在部门的用户数量
```

输出示例：

```java
[
  {total=12}
]
```

------



## 4. 插入 INSERT

🍍负责新增数据，支持单条、批量、实体插入、`insert ... select`。

### 4.1 按实体插入

作用：最常用。自动读取实体字段、自动填充主键和填充字段。

```java
User user = new User();
user.setLoginName("alice");
user.setAge(18);
user.setVersion(Version.initial());
user.setDeleted(LogicDelete.normal());

Integer affected = SQL.insert(User.class)
        .into()
        .values(user)
        .submit();
```

输入示例：

```java
user.loginName = "alice"
user.age = 18
```

输出示例：

```java
affected == 1
user.getId() != null
```

### 4.2 指定列插入

作用：只插入明确字段，适合局部写入或数据库默认值参与计算。

```java
Integer affected = SQL.insert(User.class)
        .into(User::getLoginName, User::getAge)
        .valuesRow("bob", 28)
        .submit();
```

输入示例：

```java
("bob", 28)
```

输出示例：

```java
affected == 1
```

### 4.3 批量插入

作用：批量写入多条实体，减少往返次数。

```java
List<User> users = java.util.Arrays.asList(user1, user2, user3);

int[] affected = SQL.insert(User.class)
        .into()
        .values(users)
        .submitBatch();
```

输入示例：

```java
3 个 User 实体
```

输出示例：

```java
affected.length == 3
```

### 4.4 Insert Select

作用：从查询结果直接写入目标表。

```java
SQL.insert(UserArchive.class)
        .into(UserArchive::getLoginName, UserArchive::getAge)
        .select(
                SQL.select(User::getLoginName, User::getAge)
                        .from(User.class)
                        .where(w -> w.ge(User::getAge, 18))
        )
        .submit();
```

输入示例：

```java
把 age >= 18 的用户归档
```

输出示例：

```java
受影响行数 = 满足条件的用户数
```

------



## 5. 更新 UPDATE

🍌负责更新数据，支持字段赋值、实体局部更新、批量更新、乐观锁。

### 5.1 按字段更新

作用：最直接，适合简单更新。

```java
Integer affected = SQL.update(User.class)
        .set(User::getAge, 20)
        .where(w -> w.eq(User::getId, "1"))
        .submit();
```

输入示例：

```java
id = "1"
age = 20
```

输出示例：

```java
affected == 1
```

### 5.2 按实体局部更新

作用：只更新实体中非空字段，`null` 默认不参与更新。

```java
User patch = new User();
patch.setId("1");
patch.setLoginName("alice-v2");
patch.setVersion(Version.of(1L));

Integer affected = SQL.update(User.class)
        .set(patch)
        .where(w -> w.eq(User::getId, patch.getId()))
        .submit();
```

输入示例：

```java
id = "1"
loginName = "alice-v2"
version = 1
```

输出示例：

```java
affected == 1
数据库中的 version 自动递增为 2
```

### 5.3 显式更新为 null

作用：当你确实需要把某个字段清空时使用。

```java
SQL.update(User.class)
        .set(User::getRemark, null)
        .where(w -> w.eq(User::getId, "1"))
        .submit();
```

输入示例：

```java
remark = null
```

输出示例：

```java
数据库中的 remark 变为 null
```

### 5.4 批量更新

作用：按实体主键逐条批处理更新。

```java
List<User> users = java.util.Arrays.asList(user1, user2);

int[] affected = SQL.update(User.class)
        .set(users)
        .submitBatch();
```

输入示例：

```java
2 个带主键的 User 实体
```

输出示例：

```java
affected.length == 2
```

------



## 6. 删除 DELETE

🍓这一层负责物理删除或逻辑删除。

### 6.1 普通删除

作用：删除满足条件的数据。

```java
Integer affected = SQL.delete()
        .from(User.class)
        .where(w -> w.eq(User::getId, "1"))
        .submit();
```

输入示例：

```java
id = "1"
```

输出示例：

```java
affected == 1
```

### 6.2 逻辑删除

作用：如果实体里存在 `LogicDelete` 字段，`delete` 会自动转为更新删除标记。

```java
SQL.delete()
        .from(User.class)
        .where(w -> w.eq(User::getId, "1"))
        .submit();
```

输入示例：

```java
User.deleted 字段类型为 LogicDelete
```

输出示例：

```java
数据库不物理删行
deleted 从 0 变为 1
```

### 6.3 内置安全保护

作用：框架默认阻止危险更新和删除。

会被拦截的典型情况：

- 没有 `where` 的 `update`
- 永真条件，如 `where 1 = 1`
- 明显等价的字段比较，如 `id = id`

输入示例：

```java
SQL.update(User.class).set(User::getAge, 18).submit();
```

输出示例：

```java
抛出 IllegalStateException
```

------



## 7. 原生 SQL

Sqlux的设计哲学就是`代码即SQL`，理论上不能保留原生SQL提交入口的。但是考虑到Sqlux还不完善，强行`代码即SQL`会限制功能，所以还是将这一功能开放出来。

### 7.1 占位符查询

作用：直接写原生 SQL，并映射成实体或 `Map`。

```java
List<User> users = SQL.rawQuery(
        "select * from sys_user where id in (?, ?)",
        java.util.Arrays.asList("1", "2")
).submit(User.class);
```

输入示例：

```java
ids = ["1", "2"]
```

输出示例：

```java
[
  User{id="1", loginName="alice"},
  User{id="2", loginName="bob"}
]
```

### 7.2 命名参数

作用：参数更多时更清晰，也更适合动态组装。

```java
Map<String, Object> params = new java.util.LinkedHashMap<String, Object>();
params.put("status", "ENABLED");
params.put("ids", java.util.Arrays.asList("1", "2"));

List<User> users = SQL.rawQuery(
        "select * from sys_user where status = :status and id in :ids",
        params
).submit(User.class);
```

输入示例：

```java
status = "ENABLED"
ids = ["1", "2"]
```

输出示例：

```java
返回状态为 ENABLED 且 id 在集合内的用户
```

### 7.3 原生更新

作用：执行 `update / delete / insert` 原生语句。

```java
Map<String, Object> params = new java.util.LinkedHashMap<String, Object>();
params.put("remark", "patched by raw sql");
params.put("id", "1");

Integer affected = SQL.rawUpdate(
        "update sys_user set remark = :remark where id = :id",
        params
).submit();
```

输入示例：

```java
remark = "patched by raw sql"
id = "1"
```

输出示例：

```java
affected == 1
```

### 7.4 集合参数自动展开

作用：`in` 查询不需要手工拼接占位符个数。

规则：

- `in` 空集合会安全改写为查不出结果的条件
- `not in` 空集合会直接抛错，避免误查全表

输入示例：

```java
SQL.rawQuery(
    "select * from sys_user where id in :ids",
    java.util.Collections.singletonMap("ids", java.util.Collections.emptyList())
).submit(User.class);
```

输出示例：

```java
[]
```

------



## 8. 参数查询构建器

将Map参数转为DSL SQL，通常配合前端参数，实现前端可以自定义查询逻辑

### 8.1 基础用法

作用：把 `Map<String, Object>` 翻译成结构化查询。

```java
Map<String, Object> params = new java.util.LinkedHashMap<String, Object>();
params.put("sql_distinct_enable", "true");
params.put("sql_query_col", "loginName, age");
params.put("age_ge", "18");
params.put("loginName_like", "%a%");
params.put("sql_group_col", "age");
params.put("sql_order_col", "age:desc, loginName:asc");

List<User> users = new SqluxQueryBuilder<User>(User.class)
        .params(params)
        .build()
        .submit(User.class);
```

输入示例：

```java
age_ge = 18
loginName_like = %a%
sql_order_col = age:desc, loginName:asc
```

输出示例：

```java
自动生成带 where / group by / order by 的查询
```

### 8.2 支持的查询操作

字段后缀语义：

- `_eq` 等于
- `_ne` 不等于
- `_gt / _ge / _lt / _le`
- `_like`
- `_in`
- `_between`

系统参数语义：

- `sql_distinct_enable`
- `sql_query_col`
- `sql_group_col`
- `sql_order_col`

输入示例：

```java
age_in = [18, 20, 22]
createdAt_between = [2024-01-01T00:00:00, 2024-01-31T00:00:00]
```

输出示例：

```java
where age in (...) and created_at between ? and ?
```

------



## 9. 元数据

读取数据库结构，适合后台工具、代码生成、管理页、巡检脚本。

### 9.1 推断数据库类型

```java
DefaultMetaService metaService = new DefaultMetaService();
DbType dbType = metaService.inferDbType();
```

输入示例：

```java
当前连接到 PostgreSQL
```

输出示例：

```java
DbType.POSTGRESQL
```

### 9.2 读取表、列、主键、索引、外键、视图

```java
DefaultMetaService metaService = new DefaultMetaService();

List<String> schemas = metaService.listSchemas(null);
List<xyz.ytora.sqlux.meta.model.TableMeta> tables = metaService.listTables(null, "public", "sys_user");
List<xyz.ytora.sqlux.meta.model.IndexMeta> indexes = metaService.listIndexes(null, "public", "sys_user");
List<xyz.ytora.sqlux.meta.model.ForeignKeyMeta> foreignKeys = metaService.listForeignKeys(null, "public", "sys_user");
List<xyz.ytora.sqlux.meta.model.ViewMeta> views = metaService.listViews(null, "public", "user_view");
```

输入示例：

```java
schema = "public"
table = "sys_user"
```

输出示例：

```java
schemas = ["public"]
tables[0].getName() == "sys_user"
tables[0].getPrimaryKeys() == ["id"]
```

------



## 10. 自动建表

根据实体定义自动创建缺失表，谨慎使用！

### 10.1 扫描包建表

作用：启动时扫描实体包，为数据库中不存在的表自动执行 DDL。

```java
SqluxGlobal global = SQL.getSqluxGlobal();
// 指定实体类所在路径
global.setEntityPath("com.demo.entity.**");

Connection connection = global.getConnectionProvider().getConnection();
try {
    xyz.ytora.sqlux.orm.creator.TableCreators.createMissingTables(connection);
} finally {
    global.getConnectionProvider().closeConnection(connection);
}
```

输入示例：

```java
entityPath = "com.demo.entity.**"
```

输出示例：

```java
缺失表被创建
已存在表被跳过
```

### 10.2 直接指定实体列表建表

作用：显式控制只创建某几张表。

```java
List<Class<? extends xyz.ytora.sqlux.orm.AbsEntity>> entities =
        java.util.Arrays.<Class<? extends xyz.ytora.sqlux.orm.AbsEntity>>asList(User.class, Dept.class);

List<xyz.ytora.sqlux.orm.creator.TableCreateResult> results =
        xyz.ytora.sqlux.orm.creator.TableCreators.createMissingTables(
                connection,
                DbType.POSTGRESQL,
                entities
        );
```

输入示例：

```java
[User.class, Dept.class]
```

输出示例：

```java
[
  {tableName="sys_user", created=true},
  {tableName="sys_dept", created=false}
]
```

### 10.3 当前支持的自动建表方言

- MySQL
- MariaDB
- PostgreSQL
- Oracle
- 达梦
- SQL Server

------



## 11. 拦截器与日志模块

拦截器，SQL翻译前、SQL执行前、SQL执行后存在拦截器扩展点，内置的日志功能就是基于拦截器实现。

### 11.1 结构化改写拦截器

作用：在 SQL 翻译前改写查询模型，最适合做租户、数据权限、默认过滤条件。

```java
SQL.getSqluxGlobal().registerInterceptor(new xyz.ytora.sqlux.interceptor.Interceptor() {
    @Override
    public void beforeTranslate(xyz.ytora.sqlux.interceptor.SqlRewriteContext context) {
        if (context.getSqlType() == xyz.ytora.sqlux.core.enums.SqlType.SELECT) {
            context.andWhere(w -> w.eq(User::getDeleted, LogicDelete.normal()));
        }
    }
});
```

输入示例：

```java
任意 User 查询
```

输出示例：

```java
自动追加 deleted = 0 条件
```

### 11.2 执行前后拦截器

作用：改写 SQL、做审计、统计耗时、异常告警。

```java
SQL.getSqluxGlobal().registerInterceptor(new xyz.ytora.sqlux.interceptor.Interceptor() {
    @Override
    public void beforeExecute(xyz.ytora.sqlux.interceptor.SqlExecutionContext context) {
        System.out.println(context.getSqlResult().getSql());
    }
});
```

输入示例：

```java
SQL.select().from(User.class).submit();
```

输出示例：

```java
控制台打印最终 SQL
```

### 11.3 SQL 日志器

作用：分别接收执行前、成功、失败三个事件。

```java
SQL.getSqluxGlobal().registerSqlLogger(new xyz.ytora.sqlux.interceptor.log.SqlLogger() {
    @Override
    public void beforeExecute(xyz.ytora.sqlux.interceptor.log.SqlLogEvent event) {
        System.out.println("before: " + event.getSql());
    }

    @Override
    public void afterSuccess(xyz.ytora.sqlux.interceptor.log.SqlLogEvent event) {
        System.out.println("success: " + event.getCostMs() + "ms");
    }

    @Override
    public void afterFailure(xyz.ytora.sqlux.interceptor.log.SqlLogEvent event) {
        System.out.println("failure: " + event.getException().getMessage());
    }
});
```

输入示例：

```java
SQL.rawQuery("select 1").submit();
```

输出示例：

```java
before: select 1
success: 3ms
```

------



## 12. 自动填充与类型扩展

处理时间戳、操作人、自定义值对象等扩展需求。

### 12.1 自动填充字段

作用：在 `insert / update` 时自动写入字段值。

```java
import xyz.ytora.sqlux.orm.filler.IFiller;

public class CurrentTimeFiller implements IFiller {
    @Override
    public Object onInsert() {
        return java.time.LocalDateTime.now();
    }

    @Override
    public Object onUpdate() {
        return java.time.LocalDateTime.now();
    }
}
```

```java
@Column(type = ColumnType.DATETIME, fillOn = FillType.INSERT_UPDATE, filler = CurrentTimeFiller.class)
private LocalDateTime updatedAt;
```

输入示例：

```java
更新 user 但不手动设置 updatedAt
```

输出示例：

```java
updatedAt 自动写入当前时间
```

### 12.2 自定义 TypeHandler

作用：把自定义 Java 类型转换为数据库值，再从数据库值还原回来。

```java
import xyz.ytora.sqlux.rw.TypeHandler;

import java.lang.reflect.Field;

public class MoneyHandler implements TypeHandler<Money> {
    @Override
    public boolean supports(Class<?> type) {
        return Money.class.equals(type);
    }

    @Override
    public Object write(Money value, Field field) {
        return value == null ? null : value.getAmount();
    }

    @Override
    public Money read(Object value, Field field) {
        return value == null ? null : new Money(new java.math.BigDecimal(String.valueOf(value)));
    }
}
```

```java
SqluxGlobal.registerTypeHandler(new MoneyHandler());
```

输入示例：

```java
Money(123.45)
```

输出示例：

```java
数据库中写入 123.45
查询时还原为 Money 对象
```

------



## 13. 数据库支持概览

SQL 方言支持：

- MySQL
- MariaDB
- PostgreSQL
- Oracle
- 达梦
- SQL Server

元数据探测兼容枚举还包括：

- SQLite
- DB2
- H2
- Derby
- Sybase
- Informix

------



## 14. 常用入口

```java
SQL.select(...)
SQL.insert(...)
SQL.update(...)
SQL.delete(...)
SQL.rawQuery(...)
SQL.rawUpdate(...)
```

------



## 15. 一段完整示例

```java
User user = new User();
user.setLoginName("alice");
user.setAge(18);
user.setVersion(Version.initial());
user.setDeleted(LogicDelete.normal());
user.setProfile(Json.object());
user.getProfile().put("team", "core");

SQL.insert(User.class).into().values(user).submit();

List<User> users = SQL.select()
        .from(User.class)
        .where(w -> w.eq(User::getLoginName, "alice"))
        .submit(User.class);

SQL.update(User.class)
        .set(User::getAge, 19)
        .where(w -> w.eq(User::getId, user.getId()))
        .submit();

SQL.delete()
        .from(User.class)
        .where(w -> w.eq(User::getId, user.getId()))
        .submit();
```

输入示例：

```java
新增 alice -> 查询 alice -> 更新年龄 -> 删除 alice
```

输出示例：

```java
insert affected = 1
query result size = 1
update affected = 1
delete affected = 1
```
