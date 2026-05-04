package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.json.SqluxJson;
import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 内置类型：JSON。
 *
 * <p>该类型作为实体字段时，会在 ORM 写入阶段序列化为合法 JSON 字符串，在 ORM 读取阶段从数据库
 * 字符串还原为 {@code Json} 对象。内部使用通用对象树表达 JSON 根节点，支持对象、数组和标量值。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class Json implements SqlReader, SqlWriter {

    /**
     * JSON根节点。允许 {@code Map}、{@code List}、{@code String}、{@code Number}、
     * {@code Boolean} 和 {@code null}。
     */
    private final Object root;

    /**
     * 创建空 JSON 对象。
     */
    public Json() {
        this(16);
    }

    /**
     * 创建指定初始容量的空 JSON 对象。
     *
     * @param initSize 对象初始容量
     */
    public Json(int initSize) {
        this.root = new LinkedHashMap<String, Object>(initSize);
    }

    /**
     * 根据 Map 创建 JSON 对象。
     *
     * @param map 原始 Map；key 会转成字符串
     */
    public Json(Map<?, ?> map) {
        this.root = JsonTree.copyMap(map);
    }

    /**
     * 根据 List 创建 JSON 数组。
     *
     * @param list 原始 List
     */
    public Json(List<?> list) {
        this.root = JsonTree.copyList(list);
    }

    private Json(Object root, boolean trusted) {
        this.root = trusted ? root : JsonTree.normalize(root);
    }

    /**
     * 创建空 JSON 对象。
     *
     * @return JSON对象
     */
    public static Json object() {
        return new Json();
    }

    /**
     * 创建空 JSON 数组。
     *
     * @return JSON数组
     */
    public static Json array() {
        return new Json(new ArrayList<>(), true);
    }

    /**
     * 基于指定对象创建JSON对象
     * @param value 对象
     * @return JSON对象
     */
    public static Json of(Object value) {
        return new Json(value, false);
    }

    /**
     * 解析 JSON 字符串。
     *
     * @param json JSON字符串；入参不能为 {@code null}
     * @return 解析后的 JSON 对象
     */
    public static Json parse(String json) {
        return new Json(SQL.getSqluxGlobal().getSqluxJson().parse(json), false);
    }

    /**
     * 获取当前 JSON 编解码器。
     *
     * @return JSON编解码器
     */
    public static SqluxJson getSqluxJson() {
        return SQL.getSqluxGlobal().getSqluxJson();
    }

    /**
     * 往 JSON 对象里面添加数据。
     *
     * @param key 键
     * @param value 值
     * @return 写入的值
     * @param <T> 值类型
     */
    public <T> T put(String key, T value) {
        checkJsonObj();
        sourceMap().put(key, JsonTree.normalize(value));
        return value;
    }

    /**
     * 从 JSON 对象里面获取原始值。
     *
     * @param key 键
     * @return 原始值
     */
    public Object get(String key) {
        checkJsonObj();
        return sourceMap().get(key);
    }

    /**
     * 从 JSON 对象里面获取 String 类型的 value。
     *
     * @param key 键
     * @return String 类型的 value 值
     */
    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : value.toString();
    }

    /**
     * 从 JSON 对象里面获取 Byte 类型的 value。
     *
     * @param key 键
     * @return Byte 类型的 value 值
     */
    public Byte getByte(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.byteValue();
    }

    /**
     * 从 JSON 对象里面获取 Short 类型的 value。
     *
     * @param key 键
     * @return Short 类型的 value 值
     */
    public Short getShort(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.shortValue();
    }

    /**
     * 从 JSON 对象里面获取 Integer 类型的 value。
     *
     * @param key 键
     * @return Integer 类型的 value 值
     */
    public Integer getInteger(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.intValue();
    }

    /**
     * 从 JSON 对象里面获取 Long 类型的 value。
     *
     * @param key 键
     * @return Long 类型的 value 值
     */
    public Long getLong(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.longValue();
    }

    /**
     * 从 JSON 对象里面获取 Float 类型的 value。
     *
     * @param key 键
     * @return Float 类型的 value 值
     */
    public Float getFloat(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.floatValue();
    }

    /**
     * 从 JSON 对象里面获取 Double 类型的 value。
     *
     * @param key 键
     * @return Double 类型的 value 值
     */
    public Double getDouble(String key) {
        Number number = JsonScalars.number(get(key));
        return number == null ? null : number.doubleValue();
    }

    /**
     * 从 JSON 对象里面获取 BigDecimal 类型的 value。
     *
     * @param key 键
     * @return BigDecimal 类型的 value 值
     */
    public BigDecimal getBigDecimal(String key) {
        return JsonScalars.bigDecimal(get(key));
    }

    /**
     * 从 JSON 对象里面获取 Boolean 类型的 value。
     *
     * @param key 键
     * @return Boolean 类型的 value 值
     */
    public Boolean getBoolean(String key) {
        return JsonScalars.bool(get(key));
    }

    /**
     * 从 JSON 对象里面获取 Json 类型的 value。
     *
     * @param key 键
     * @return Json 类型的 value 值
     */
    public Json getJson(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return new Json(value, true);
    }

    /**
     * 从 JSON 对象里面获取指定类型的 value。
     *
     * @param key 键
     * @param clazz 指定类型
     * @return 指定类型的 value 值
     * @param <T> 指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("目标类型不能为空");
        }
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
            return (T) getByte(key);
        }
        if (Short.class.equals(clazz) || short.class.equals(clazz)) {
            return (T) getShort(key);
        }
        if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return (T) getInteger(key);
        }
        if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            return (T) getLong(key);
        }
        if (Float.class.equals(clazz) || float.class.equals(clazz)) {
            return (T) getFloat(key);
        }
        if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            return (T) getDouble(key);
        }
        if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return (T) getBoolean(key);
        }
        if (BigDecimal.class.equals(clazz)) {
            return (T) getBigDecimal(key);
        }
        if (String.class.equals(clazz)) {
            return (T) getString(key);
        }
        if (Json.class.equals(clazz)) {
            return (T) getJson(key);
        }
        throw new IllegalArgumentException("无法将[" + value.getClass().getName() + "]类型转为[" + clazz.getName() + "]类型");
    }

    /**
     * 兼容旧命名：从 JSON 对象里面获取指定类型的 value。
     *
     * @param key 键
     * @param clazz 指定类型
     * @return 指定类型的 value 值
     * @param <T> 指定类型
     */
    public <T> T getSourceMap(String key, Class<T> clazz) {
        return get(key, clazz);
    }

    /**
     * 从 JSON 数组里面获取指定下标的数据。
     *
     * @param index 下标
     * @return Json对象
     */
    public Json get(int index) {
        checkJsonArray();
        return new Json(sourceList().get(index), true);
    }

    /**
     * 添加 JSON 元素。
     *
     * @param json JSON元素
     * @return 添加是否成功
     */
    public Boolean add(Json json) {
        if (json == null) {
            return add((Object) null);
        }
        return add(json.root);
    }

    /**
     * 添加任意 JSON 兼容元素。
     *
     * @param value JSON兼容元素
     * @return 添加是否成功
     */
    public Boolean add(Object value) {
        checkJsonArray();
        return sourceList().add(JsonTree.normalize(value));
    }

    /**
     * 移除指定下标的元素。
     *
     * @param index 下标
     * @return 被移除的 JSON
     */
    public Json remove(int index) {
        checkJsonArray();
        return new Json(sourceList().remove(index), true);
    }

    /**
     * 获取 JSON 数组的 stream 流。
     *
     * @return stream流
     */
    public Stream<Json> stream() {
        checkJsonArray();
        return sourceList().stream().map(value -> new Json(value, true));
    }

    /**
     * 当前 JSON 根节点是否为数组。
     *
     * @return 是数组时返回 {@code true}
     */
    public boolean isArray() {
        return root instanceof List<?>;
    }

    /**
     * 当前 JSON 根节点是否为对象。
     *
     * @return 是对象时返回 {@code true}
     */
    public boolean isObject() {
        return root instanceof Map<?, ?>;
    }

    /**
     * 获取数组长度或对象字段数量。
     *
     * @return 数组长度、对象字段数量或 0
     */
    public int size() {
        if (root instanceof Map<?, ?>) {
            return ((Map<?, ?>) root).size();
        }
        if (root instanceof List<?>) {
            return ((List<?>) root).size();
        }
        return root == null ? 0 : 1;
    }

    /**
     * 获取底层 JSON 兼容对象树。
     *
     * @return JSON兼容对象树
     */
    public Object unwrap() {
        return root;
    }

    @Override
    public Json read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Json) {
            return (Json) value;
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return new Json(value, false);
        }
        return parse(String.valueOf(value));
    }

    @Override
    public Object write() {
        return SQL.getSqluxGlobal().getSqluxJson().stringify(root);
    }

    @Override
    public String toString() {
        return (String) write();
    }

    void checkJsonObj() {
        JsonTree.checkJsonObj(root);
    }

    void checkJsonArray() {
        JsonTree.checkJsonArray(root);
    }

    LinkedHashMap<String, Object> getSourceMap(int index) {
        return JsonTree.getSourceMap(root, index);
    }

    List<Object> getSourceList() {
        checkJsonArray();
        return JsonTree.sourceList(root);
    }

    private LinkedHashMap<String, Object> sourceMap() {
        return JsonTree.sourceMap(root);
    }

    private List<Object> sourceList() {
        return JsonTree.sourceList(root);
    }

}
