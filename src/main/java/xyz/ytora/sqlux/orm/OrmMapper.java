package xyz.ytora.sqlux.orm;

import xyz.ytora.sqlux.orm.type.LogicDelete;
import xyz.ytora.sqlux.orm.type.Version;
import xyz.ytora.sqlux.rw.SqlWriter;
import xyz.ytora.sqlux.sql.model.ColumnRef;

import java.lang.reflect.*;
import java.util.*;

/**
 * 实体对象与数据库原始行之间的映射工具。
 *
 * <p>该工具只通过 getter/setter 读写实体值；字段反射仅用于读取映射元数据。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class OrmMapper {

    /**
     * 工具类构造方法。
     *
     * <p>示例：调用方应使用 {@link #mapRows(List, Class)} 等静态方法，不需要创建实例。</p>
     */
    private OrmMapper() {
    }

    /**
     * 将原始查询结果映射为实体集合。
     *
     * <p>示例：原始行 {@code [{user_name=ytora}]} 映射到 {@code User.class} 时，
     * 如果实体存在 {@code userName} 字段和 {@code setUserName(String)}，则返回包含一个
     * {@code User} 对象的集合。</p>
     *
     * @param rows 原始查询结果；入参通常来自 {@code JDBCExecutor.query(sqlResult)}
     * @param resultType 目标实体类型；入参需要有无参构造方法，字段绑定依赖 setter
     * @return 映射后的实体集合；如果 {@code rows} 为 {@code null}，则返回空集合
     * @param <T> 结果实体泛型，例如 {@code User}
     */
    public static <T> List<T> mapRows(List<Map<String, Object>> rows, Class<T> resultType) {
        List<T> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            result.add(mapRow(row, resultType));
        }
        return result;
    }

    /**
     * 从实体对象中抽取 INSERT 的一行参数。
     *
     * <p>示例：{@code columns} 为 {@code [name, age]}，实体 getter 返回 {@code "ytora"} 和
     * {@code 18}，则出参为 {@code ["ytora", 18]}。如果字段值实现 {@link SqlWriter}，
     * 会先执行 {@link SqlWriter#write()} 后再放入结果。</p>
     *
     * @param bean 待插入实体对象；入参不能为 {@code null}
     * @param columns INSERT 指定的字段列表；为空时读取实体中所有存在映射且有 getter 的字段
     * @return 一行 INSERT 参数；出参顺序与 {@code columns} 顺序一致
     */
    public static List<Object> readInsertRow(Object bean, List<ColumnRef> columns) {
        if (bean == null) {
            throw new IllegalArgumentException("INSERT实体对象不能为空");
        }
        if (columns == null || columns.isEmpty()) {
            return readAllExistingValues(bean, true);
        }
        List<Object> row = new ArrayList<>();
        EntityMeta meta = EntityMetas.get(bean.getClass());
        for (ColumnRef column : columns) {
            EntityFieldMeta field = meta.getFieldByColumn(column.getColumnName());
            if (field == null) {
                throw new IllegalArgumentException("实体类 " + bean.getClass().getName()
                        + " 中找不到字段映射到数据库列: " + column.getColumnName());
            }
            row.add(readValue(bean, field, true));
        }
        return row;
    }

    /**
     * 从实体对象中抽取 UPDATE SET 参数；只返回 getter 值不为 null 的字段。
     *
     * <p>示例：实体中 {@code name="ytora"}、{@code age=null}，则返回
     * {@code {name=ytora}}，不会生成 {@code age = null} 的 SET 项。</p>
     *
     * @param bean 待更新实体对象；入参不能为 {@code null}
     * @return 字段名到写入值的映射；key 是数据库列名，value 是 getter 值或 {@link SqlWriter} 转换后的值
     */
    public static Map<String, Object> readNonNullValues(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException("UPDATE实体对象不能为空");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (EntityFieldMeta field : EntityMetas.get(bean.getClass()).getFields()) {
            if (field.getGetter() == null) {
                continue;
            }
            Object value = OrmReflection.invoke(field.getGetter(), bean);
            if (value == null) {
                continue;
            }
            values.put(field.getColumnName(), OrmValueConverter.write(field, value));
        }
        return values;
    }

    /**
     * 从实体对象中抽取 UPDATE SET 参数和乐观锁版本条件。
     *
     * <p>实体存在 {@link Version} 字段时，版本字段不会作为普通 SET 参数返回，而是生成版本锁信息；
     * 调用方应据此生成 {@code version = version + 1} 和 {@code WHERE version = oldVersion}。</p>
     *
     * @param bean 待更新实体对象；入参不能为 {@code null}
     * @return UPDATE 值规划结果
     */
    public static OrmUpdateValues readUpdateValues(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException("UPDATE实体对象不能为空");
        }
        OrmUpdateValues result = new OrmUpdateValues();
        for (EntityFieldMeta field : EntityMetas.get(bean.getClass()).getFields()) {
            if (field.getGetter() == null) {
                continue;
            }
            Object value = OrmReflection.invoke(field.getGetter(), bean);
            String columnName = field.getColumnName();
            if (field.getField().getType() == Version.class) {
                if (value == null) {
                    throw new IllegalArgumentException("实体类 " + bean.getClass().getName()
                            + " 启用了Version乐观锁，但字段 " + field.getField().getName() + " 的值为空");
                }
                result.setVersionLock(columnName, OrmValueConverter.write(field, value));
                continue;
            }
            if (value != null) {
                result.putValue(columnName, OrmValueConverter.write(field, value));
            }
        }
        return result;
    }

    /**
     * 按数据库列名读取实体字段值，并执行写转换。
     *
     * <p>示例：{@code readColumnValue(user, "tenant_id")} 会先根据列名找到实体字段，
     * 再调用对应 getter 读取值，并按 {@link SqlWriter} 或类型处理器规则转换为数据库写入值。</p>
     *
     * @param bean 实体对象；入参不能为 {@code null}
     * @param columnName 数据库列名；入参不能为空白字符串
     * @return 字段值；如果 getter 返回 {@code null}，则出参也为 {@code null}
     */
    public static Object readColumnValue(Object bean, String columnName) {
        if (bean == null) {
            throw new IllegalArgumentException("实体对象不能为空");
        }
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("数据库列名不能为空");
        }
        EntityFieldMeta field = EntityMetas.get(bean.getClass()).getFieldByColumn(columnName);
        if (field == null) {
            throw new IllegalArgumentException("实体类 " + bean.getClass().getName()
                    + " 中找不到字段映射到数据库列: " + columnName);
        }
        return readValue(bean, field, true);
    }

    /**
     * 将 generated keys 回填到实体主键 setter。
     *
     * <p>示例：实体表注解为 {@code @Table(key={"id"})}，generated key 行为 {@code {id=10}}，
     * 且实体存在 {@code setId(Integer)}，则会调用 setter 回填 {@code 10}。</p>
     *
     * @param bean 需要回填主键的实体对象；为 {@code null} 时直接跳过
     * @param generatedKeyRow JDBC 返回的一行 generated keys；为空时直接跳过
     */
    public static void backfillGeneratedKeys(Object bean, Map<String, Object> generatedKeyRow) {
        if (bean == null || generatedKeyRow == null || generatedKeyRow.isEmpty()) {
            return;
        }
        String[] keyColumns = EntityMetas.get(bean.getClass()).getKeyColumns();
        int index = 0;
        for (String keyColumn : keyColumns) {
            EntityFieldMeta field = EntityMetas.get(bean.getClass()).getFieldByColumn(keyColumn);
            if (field == null) {
                continue;
            }
            Object value = OrmRowAccess.getValue(generatedKeyRow, keyColumn);
            if (value == null && keyColumns.length == 1) {
                value = generatedKeyRow.values().iterator().next();
            } else if (value == null && index < generatedKeyRow.size()) {
                value = new ArrayList<>(generatedKeyRow.values()).get(index);
            }
            setValue(bean, field, value);
            index++;
        }
    }

    /**
     * 查找实体中指定字段类型对应的数据库列名。
     *
     * @param entityType 实体类型
     * @param fieldType 字段类型
     * @return 数据库列名；找不到时返回 {@code null}
     */
    public static String findColumnByFieldType(Class<?> entityType, Class<?> fieldType) {
        if (entityType == null || fieldType == null) {
            return null;
        }
        for (EntityFieldMeta field : EntityMetas.get(entityType).getFields()) {
            if (field.getField().getType() == fieldType) {
                return field.getColumnName();
            }
        }
        return null;
    }

    /**
     * 查找实体逻辑删除字段对应的数据库列名。
     *
     * @param entityType 实体类型
     * @return 逻辑删除列名；找不到时返回 {@code null}
     */
    public static String findLogicDeleteColumn(Class<?> entityType) {
        return findColumnByFieldType(entityType, LogicDelete.class);
    }

    /**
     * 将单行原始数据映射为一个实体对象。
     *
     * <p>示例：{@code row} 中有 key {@code user_name}，实体字段 {@code userName}
     * 有 setter，则会把该 key 对应的值写入实体。</p>
     *
     * @param row 单行原始数据；入参 key 是数据库列名或 SQL 别名
     * @param resultType 目标实体类型；入参需要无参构造方法
     * @return 映射后的实体对象；即使 {@code row} 为空，也会返回一个新实体实例
     * @param <T> 结果实体泛型
     */
    @SuppressWarnings("unchecked")
    private static <T> T mapRow(Map<String, Object> row, Class<T> resultType) {
        if (resultType.equals(Character.class) ||
                CharSequence.class.isAssignableFrom(resultType) ||
                Number.class.isAssignableFrom(resultType)) {
            Object value = row.values().iterator().next();
            return (T) OrmValueConverter.convert(value, resultType);
        }

        T bean = OrmReflection.newInstance(resultType);
        if (row == null || row.isEmpty()) {
            return bean;
        }
        for (EntityFieldMeta field : EntityMetas.get(resultType).getFields()) {
            String columnName = field.getColumnName();
            if (!OrmRowAccess.containsKey(row, columnName)) {
                continue;
            }
            Object value = OrmRowAccess.getValue(row, columnName);
            setValue(bean, field, OrmValueConverter.read(field, value));
        }
        return bean;
    }

    /**
     * 按实体字段声明顺序读取所有参与映射的字段值。
     *
     * <p>示例：实体字段为 {@code id/name/age} 且都有 getter 时，返回
     * {@code [idValue, nameValue, ageValue]}。</p>
     *
     * @param bean 待读取实体对象；入参不能为 {@code null}
     * @param requireGetter 是否强制要求每个参与映射字段都有 getter；为 {@code true} 时缺少 getter 会抛异常
     * @return 字段值列表；出参只包含 {@code @Column(exist=true)} 或未标注 {@code @Column} 的字段
     */
    private static List<Object> readAllExistingValues(Object bean, boolean requireGetter) {
        List<Object> values = new ArrayList<>();
        for (EntityFieldMeta field : EntityMetas.get(bean.getClass()).getFields()) {
            Method getter = field.getGetter();
            if (getter == null) {
                if (requireGetter) {
                    throw new IllegalArgumentException("实体字段缺少getter: " + field.getField().getName());
                }
                continue;
            }
            values.add(OrmValueConverter.write(field, OrmReflection.invoke(getter, bean)));
        }
        return values;
    }

    /**
     * 通过实体 getter 读取字段值，并执行写转换。
     *
     * <p>示例：字段 {@code status} 的 getter 返回实现 {@link SqlWriter} 的对象，
     * 则本方法返回 {@code status.write(status)} 的结果。</p>
     *
     * @param bean 待读取实体对象；入参不能为 {@code null}
     * @param field 实体字段元数据；入参用于定位 getter 和判断 {@link SqlWriter}
     * @param requireGetter 是否强制要求存在 getter；为 {@code false} 时缺少 getter 返回 {@code null}
     * @return 数据库写入值；可能为 {@code null}
     */
    private static Object readValue(Object bean, EntityFieldMeta field, boolean requireGetter) {
        Method getter = field.getGetter();
        if (getter == null) {
            if (requireGetter) {
                throw new IllegalArgumentException("实体字段缺少getter: " + field.getField().getName());
            }
            return null;
        }
        return OrmValueConverter.write(field, OrmReflection.invoke(getter, bean));
    }

    /**
     * 通过 setter 将值写入实体字段。
     *
     * <p>示例：字段为 {@code userName} 时，只会寻找 {@code setUserName(字段类型)}；
     * 如果 setter 不存在，则跳过该字段。</p>
     *
     * @param bean 目标实体对象；入参不能为 {@code null}
     * @param field 实体字段元数据；入参用于定位 setter 和做类型转换
     * @param value 待写入值；入参会先转换为字段类型
     */
    private static void setValue(Object bean, EntityFieldMeta field, Object value) {
        Method setter = field.getSetter();
        if (setter == null) {
            return;
        }
        OrmReflection.invoke(setter, bean, OrmValueConverter.convert(value, field.getField().getType()));
    }
}
