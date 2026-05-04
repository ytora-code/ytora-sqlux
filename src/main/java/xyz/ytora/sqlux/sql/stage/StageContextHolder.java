package xyz.ytora.sqlux.sql.stage;

import xyz.ytora.sqlux.util.NamedUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SQL阶段共用的上下文。
 *
 * <p>它保存“默认别名解析”和“已占用别名”两类信息：
 * 普通 API 仍然通过实体类型解析默认别名；显式句柄 API 则直接把别名绑定到列引用上。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class StageContextHolder {

    /**
     * 每个实体类型在当前 SQL 中的默认别名。
     */
    private final Map<Class<?>, String> aliasMapper = new ConcurrentHashMap<>();

    /**
     * 当前 SQL 中已占用的所有别名。
     */
    private final Set<String> usedAliases = ConcurrentHashMap.newKeySet();

    /**
     * 增加表，并产生该表的别名。
     *
     * @param tableClazz 实体类表
     * @return 自动产生的别名
     */
    public String addTable(Class<?> tableClazz) {
        if (tableClazz == null) {
            throw new IllegalArgumentException("表不能为空");
        }
        String tableName = NamedUtil.parseTableName(tableClazz);
        String aliasPrefix = Arrays.stream(tableName.split("_"))
                .filter(item -> !item.isEmpty())
                .map(item -> item.substring(0, 1))
                .collect(Collectors.joining());
        if (aliasPrefix.isEmpty()) {
            aliasPrefix = "t";
        }
        int index = usedAliases.size() + 1;
        String alias = aliasPrefix + index;
        while (usedAliases.contains(alias)) {
            index++;
            alias = aliasPrefix + index;
        }
        return addTable(tableClazz, alias);
    }

    /**
     * 增加表，并手动为其产生别名。
     *
     * @param tableClazz 实体类表
     * @param alias 别名
     * @return 别名
     */
    public String addTable(Class<?> tableClazz, String alias) {
        if (tableClazz == null) {
            throw new IllegalArgumentException("表不能为空");
        }
        if (alias == null || alias.trim().isEmpty()) {
            return addTable(tableClazz);
        }
        String normalizedAlias = alias.trim();
        if (usedAliases.contains(normalizedAlias)) {
            String existingAlias = aliasMapper.get(tableClazz);
            if (!normalizedAlias.equals(existingAlias)) {
                throw new IllegalStateException("SQL表别名重复: " + normalizedAlias);
            }
            return normalizedAlias;
        }
        usedAliases.add(normalizedAlias);
        aliasMapper.putIfAbsent(tableClazz, normalizedAlias);
        return normalizedAlias;
    }

    /**
     * 获取表在当前SQL中的默认别名。
     *
     * @param tableClazz 实体类型
     * @return 默认别名
     */
    public String getAlias(Class<?> tableClazz) {
        return aliasMapper.get(tableClazz);
    }

    /**
     * 复制另一个阶段上下文中的表别名信息。
     *
     * @param source 被复制的阶段上下文
     */
    public void copyFrom(StageContextHolder source) {
        if (source == null) {
            return;
        }
        aliasMapper.clear();
        aliasMapper.putAll(source.aliasMapper);
        usedAliases.clear();
        usedAliases.addAll(source.usedAliases);
    }
}
