package xyz.ytora.sqlux.translate;

/**
 * 默认DDL方言实现。
 *
 * <p>该实现暂不生成完整 DDL，只提供类型映射能力。后续真正引入自动建表、删表、索引同步时，
 * 可以为不同数据库替换为完整实现。</p>
 *
 * <p>使用示例：{@code new UnsupportedDdlDialect(typeMapper).supportsDdl()}。
 * 输入说明：构造时传入类型映射器。输出说明：当前返回 {@code false} 表示尚未支持 DDL 翻译。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class UnsupportedDdlDialect implements DdlDialect {

    private final TypeMapper typeMapper;

    /**
     * 创建默认 DDL 方言。
     *
     * @param typeMapper 类型映射器；入参不能为 {@code null}
     */
    public UnsupportedDdlDialect(TypeMapper typeMapper) {
        if (typeMapper == null) {
            throw new IllegalArgumentException("类型映射器不能为空");
        }
        this.typeMapper = typeMapper;
    }

    /**
     * 判断是否支持完整 DDL 翻译。
     *
     * @return 固定返回 {@code false}
     */
    @Override
    public boolean supportsDdl() {
        return false;
    }

    /**
     * 获取当前数据库的类型映射器。
     *
     * @return 类型映射器；出参不会为 {@code null}
     */
    @Override
    public TypeMapper typeMapper() {
        return typeMapper;
    }
}
