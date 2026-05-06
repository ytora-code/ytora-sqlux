package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.translate.TranslateContext;

/**
 * 原始表数据源。
 *
 * <p>用于直接承载用户传入的 FROM/JOIN 表片段，例如 {@code schema.table}、
 * 视图名或数据库特有的表表达式。该数据源不会再做实体表名解析或方言标识符转义。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class RawTableSource implements QuerySource {

    private final String source;

    private final String alias;

    /**
     * 创建原始表数据源。
     *
     * @param source FROM/JOIN 后面的原始表片段
     * @param alias 数据源别名；为空时表示不追加别名
     */
    public RawTableSource(String source, String alias) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("原始表数据源不能为空");
        }
        this.source = source.trim();
        this.alias = normalizeAlias(alias);
    }

    /**
     * 获取原始表片段。
     *
     * @return 原始表片段
     */
    public String getSource() {
        return source;
    }

    /**
     * 获取数据源别名。
     *
     * @return 数据源别名；未指定时返回 {@code null}
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * 将原始表数据源渲染为 FROM/JOIN 片段。
     *
     * @param context 翻译上下文；该类型数据源不依赖上下文，仅为保持接口一致
     * @return 原始表片段和可选别名
     */
    @Override
    public String renderSource(TranslateContext context) {
        if (alias == null) {
            return source;
        }
        return source + " " + alias;
    }

    /**
     * 规范化数据源别名。
     *
     * @param alias 原始别名
     * @return 去除空白后的别名；空白别名返回 {@code null}
     */
    private static String normalizeAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return null;
        }
        return alias.trim();
    }
}
