package xyz.ytora.sqlux.translate;

import xyz.ytora.sqlux.core.enums.DbType;

/**
 * 默认SQL方言。
 *
 * <p>该类保留给外部直接引用的兼容场景。当前默认行为继承 MySQL 方言，因为类库默认数据库类型为
 * MySQL；真正运行时建议通过 {@link DialectFactory#getDialect(DbType)} 获取方言。</p>
 *
 * <p>使用示例：{@code new DefaultDialect().selectTranslator().translate(query)}。
 * 输入说明：传入 SQL 模型。输出说明：返回默认 MySQL 风格 SQL。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DefaultDialect extends MysqlDialect {
}
