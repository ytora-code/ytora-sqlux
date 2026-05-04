package xyz.ytora.sqlux.sql.model;

import xyz.ytora.sqlux.sql.stage.StageContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * INSERT 查询模型。
 *
 * <p>保存插入目标表、插入字段和多行插入值。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class InsertQuery {

    private final StageContextHolder contextHolder = new StageContextHolder();

    private final TableRef table;

    private final List<ColumnRef> columns = new ArrayList<>();

    private final List<List<Object>> rows = new ArrayList<>();

    private SelectQuery selectQuery;

    /**
     * 创建 INSERT 查询模型。
     *
     * @param tableClass 插入目标表对应的实体类型
     */
    public InsertQuery(Class<?> tableClass) {
        this.table = new TableRef(tableClass, null);
    }

    /**
     * 获取 SQL 阶段上下文。
     *
     * @return SQL阶段上下文
     */
    public StageContextHolder getContextHolder() {
        return contextHolder;
    }

    /**
     * 获取插入目标表。
     *
     * @return 表引用
     */
    public TableRef getTable() {
        return table;
    }

    /**
     * 添加插入字段。
     *
     * @param column 字段引用
     */
    public void addColumn(ColumnRef column) {
        columns.add(column);
    }

    /**
     * 获取插入字段列表。
     *
     * @return 不可变字段列表
     */
    public List<ColumnRef> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * 添加一行插入值。
     *
     * @param row 插入值列表
     */
    public void addRow(List<Object> row) {
        validateRow(row);
        rows.add(row);
    }

    /**
     * 获取所有插入值行。
     *
     * @return 不可变插入值行列表
     */
    public List<List<Object>> getRows() {
        return Collections.unmodifiableList(rows);
    }

    /**
     * 获取 INSERT SELECT 子查询。
     *
     * @return SELECT子查询；未设置时返回 {@code null}
     */
    public SelectQuery getSelectQuery() {
        return selectQuery;
    }

    /**
     * 设置 INSERT SELECT 子查询。
     *
     * @param selectQuery SELECT子查询
     */
    public void setSelectQuery(SelectQuery selectQuery) {
        if (selectQuery == null) {
            throw new IllegalArgumentException("INSERT SELECT 子查询不能为空");
        }
        if (!rows.isEmpty()) {
            throw new IllegalStateException("INSERT VALUES 与 INSERT SELECT 不能同时使用");
        }
        this.selectQuery = selectQuery;
    }

    /**
     * 校验 INSERT VALUES 的单行数据。
     *
     * <p>该校验保证 VALUES 模式与 INSERT SELECT 模式互斥，并保证显式字段数量与每行值数量一致。</p>
     *
     * @param row 待插入的一行值
     */
    private void validateRow(List<Object> row) {
        if (selectQuery != null) {
            throw new IllegalStateException("INSERT VALUES 与 INSERT SELECT 不能同时使用");
        }
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("INSERT每一行VALUES不能为空");
        }
        if (!columns.isEmpty() && row.size() != columns.size()) {
            throw new IllegalArgumentException("INSERT行值数量必须与into字段数量一致");
        }
        if (columns.isEmpty() && !rows.isEmpty() && row.size() != rows.get(0).size()) {
            throw new IllegalArgumentException("INSERT多行VALUES的字段数量必须一致");
        }
    }
}
