package xyz.ytora.sqlux.util;

import xyz.ytora.sqlux.core.anno.Column;
import xyz.ytora.sqlux.core.anno.Table;
import xyz.ytora.sqlux.sql.func.ColFunction;
import xyz.ytora.toolkit.text.Strs;

import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 表、列的名称工具类
 *
 * <p>提供了表名、列名的名称解析工具类</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class NamedUtil {

    /**
     * 解析实体类对应的数据库表名称
     * <p>如果实体类标注了{@link Table}注解，则使用注解内部指定的表名称<p/>
     * <p>否则默认根据实体类的类名，以下划线分割的小写形式来确认表名称<p/>
     * <p>比如类名是{@code SysUser}，则其表名称是{@code user_name}</p>
     *
     * <p>示例：{@code User.class} 解析为 {@code user}；
     * {@code @Table("sys_user")} 标注的实体解析为 {@code sys_user}。</p>
     *
     * @param tableClazz 实体类；入参用于读取 {@link Table} 注解或类名
     * @return 数据库表名称；出参不包含 SQL 引号
     * @param <T> 实体类型
     */
    public static <T> String parseTableName(Class<T> tableClazz) {
        Table anno = tableClazz.getAnnotation(Table.class);
        String tableName;
        if (anno != null && anno.value() != null && !anno.value().trim().isEmpty()) {
            tableName = anno.value();
        } else {
            String clazzSimpleName = tableClazz.getSimpleName();
            tableName = Strs.toSnakeCase(clazzSimpleName);
        }
        return tableName;
    }

    /**
     * 从方法引用解析字段名称。
     *
     * <p>例如 {@code User::getUserName} 会解析为 {@code user_name}。</p>
     *
     * <p>示例：字段 {@code userName} 未标注注解时，{@code User::getUserName}
     * 返回 {@code user_name}；字段标注 {@code @Column("user_name123")} 时返回
     * {@code user_name123}。</p>
     *
     * @param column 字段 getter 方法引用；入参必须是实体类方法引用
     * @return 数据库字段名称；出参优先使用 {@link Column#value()}
     */
    public static String parseColumnName(ColFunction<?, ?> column) {
        SerializedLambda lambda = resolveLambda(column);
        String propertyName = methodNameToPropertyName(lambda.getImplMethodName());
        Field field = findField(parseColumnOwner(column), propertyName);
        if (field != null) {
            return parseColumnName(field);
        }
        return propertyNameToColumnName(propertyName);
    }

    /**
     * 解析实体字段对应的数据库字段名称。
     *
     * <p>字段标注 {@link Column} 且指定了 {@code value} 时优先使用注解值；
     * 否则使用字段名的小驼峰转下划线小写形式。</p>
     *
     * <p>示例：字段 {@code userName} 返回 {@code user_name}；
     * 字段 {@code @Column("user_name123") private String name;} 返回 {@code user_name123}。</p>
     *
     * @param field 实体字段元数据；入参用于读取字段名和 {@link Column} 注解
     * @return 数据库字段名称；出参不包含 SQL 引号
     */
    public static String parseColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && column.value() != null && !column.value().trim().isEmpty()) {
            return column.value();
        }
        return propertyNameToColumnName(field.getName());
    }

    /**
     * 判断字段是否参与数据库映射。
     *
     * <p>示例：未标注 {@link Column} 的字段返回 {@code true}；
     * 标注 {@code @Column(exist=false)} 的字段返回 {@code false}。</p>
     *
     * @param field 实体字段元数据；入参用于读取 {@link Column#exist()}
     * @return 字段参与映射时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isColumnExists(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null || column.exist();
    }

    /**
     * 将 Java 属性名转为数据库字段名。
     *
     * <p>示例：入参 {@code userName} 返回 {@code user_name}；
     * 入参 {@code id} 返回 {@code id}。</p>
     *
     * @param propertyName Java属性名；入参通常来自字段名或 getter 名
     * @return 下划线分割的小写数据库字段名
     */
    public static String propertyNameToColumnName(String propertyName) {
        return Strs.toSnakeCase(propertyName);
    }

    /**
     * 从方法引用解析字段所属实体类。
     *
     * <p>示例：{@code User::getName} 返回 {@code User.class}。</p>
     *
     * @param column 字段 getter 方法引用；入参必须是实体类方法引用
     * @return 字段所属实体类型
     */
    public static Class<?> parseColumnOwner(ColFunction<?, ?> column) {
        SerializedLambda lambda = resolveLambda(column);
        Class<?> instantiatedOwner = resolveInstantiatedOwner(lambda, column.getClass().getClassLoader());
        if (instantiatedOwner != null) {
            return instantiatedOwner;
        }
        String className = lambda.getImplClass().replace('/', '.');
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("无法解析字段所属实体类: " + className, e);
        }
    }

    /**
     * 解析序列化 lambda，获取方法引用的底层信息。
     *
     * <p>示例：{@code User::getName} 解析后可以得到实现类 {@code User} 和方法名
     * {@code getName}。</p>
     *
     * @param column 字段 getter 方法引用；入参不能为 {@code null}
     * @return 序列化 lambda 信息；出参包含实现类、方法名等元数据
     */
    private static SerializedLambda resolveLambda(ColFunction<?, ?> column) {
        if (column == null) {
            throw new IllegalArgumentException("字段不能为空");
        }
        try {
            Method method = column.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serialized = method.invoke(column);
            if (!(serialized instanceof SerializedLambda)) {
                throw new IllegalArgumentException("无法解析字段方法引用");
            }
            return (SerializedLambda) serialized;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("字段必须使用实体类getter方法引用", e);
        }
    }

    /**
     * 从 lambda 的实例化方法签名中解析实际接收者类型。
     *
     * <p>当 getter 定义在父类上、但方法引用写成 {@code SubEntity::getId} 时，
     * {@link SerializedLambda#getImplClass()} 会指向父类，这会让 SQL 别名解析丢失
     * 当前查询里实际使用的子类表。此处优先使用实例化后的函数签名首参作为字段所属实体。</p>
     *
     * @param lambda 序列化 lambda
     * @param classLoader 解析签名类型时使用的类加载器
     * @return 实际方法引用接收者类型；无法解析时返回 {@code null}
     */
    private static Class<?> resolveInstantiatedOwner(SerializedLambda lambda, ClassLoader classLoader) {
        try {
            MethodType methodType = MethodType.fromMethodDescriptorString(lambda.getInstantiatedMethodType(), classLoader);
            if (methodType.parameterCount() == 0) {
                return null;
            }
            Class<?> owner = methodType.parameterType(0);
            return owner == Object.class ? null : owner;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将 getter 方法名转换为 Java 属性名。
     *
     * <p>示例：{@code getUserName} 返回 {@code userName}；
     * {@code isEnabled} 返回 {@code enabled}；普通方法名 {@code name} 原样返回。</p>
     *
     * @param methodName 方法名；入参通常来自 {@link SerializedLambda#getImplMethodName()}
     * @return Java属性名
     */
    private static String methodNameToPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return decapitalize(methodName.substring(2));
        } else {
            return methodName;
        }
    }

    /**
     * 将字符串首字母转为小写。
     *
     * <p>示例：{@code UserName} 返回 {@code userName}；
     * {@code X} 返回 {@code x}。</p>
     *
     * @param propertyName 首字母可能为大写的属性名；入参通常来自 getter 去前缀后的名称
     * @return 首字母小写后的属性名
     */
    private static String decapitalize(String propertyName) {
        if (propertyName.length() == 1) {
            return propertyName.toLowerCase();
        }
        return Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    /**
     * 在实体类及其父类中查找指定字段。
     *
     * <p>示例：{@code AdminUser extends User}，在 {@code AdminUser.class} 上查找
     * {@code id} 时，如果字段定义在 {@code User} 中也可以找到。</p>
     *
     * @param type 起始查找类型；入参通常是方法引用所属实体类
     * @param name 字段名；入参必须是 Java 字段名，不是数据库列名
     * @return 匹配到的字段；找不到时返回 {@code null}
     */
    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

}
