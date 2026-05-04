package xyz.ytora.sqlux.sql.stage.select;

import xyz.ytora.sqlux.core.SQL;
import xyz.ytora.sqlux.core.enums.SqlType;
import xyz.ytora.sqlux.orm.Page;
import xyz.ytora.sqlux.sql.model.QuerySource;
import xyz.ytora.sqlux.sql.model.SelectQuery;
import xyz.ytora.sqlux.sql.model.TableRef;
import xyz.ytora.sqlux.translate.SqlResult;
import xyz.ytora.toolkit.collection.Colls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * SELECT 分页提交器。
 *
 * <p>负责把一个 SELECT 查询拆分为 count 查询和当前页数据查询，避免阶段基类承载执行细节。</p>
 *
 * @author ytora
 * @since 1.0
 */
final class SelectPageExecutor {

    private SelectPageExecutor() {
    }

    @SuppressWarnings("unchecked")
    static <T> Page<T> submit(AbsSelect select, Page<T> page) {
        if (page == null) {
            throw new IllegalArgumentException("分页对象不能为空");
        }
        int pageNo = requirePositive(page.getPageNo(), "分页页码");
        int pageSize = requirePositive(page.getPageSize(), "分页页尺寸");
        long offset = (pageNo - 1L) * pageSize;
        Integer start = toPageOffset(offset);

        SelectQuery countQuery = preparePageQuery(select);
        SqlResult countSql = toCountSql(countQuery);
        long total = readTotal(SQL.getSqluxGlobal().getExecutor().query(countSql));
        page.setTotal(total);
        page.setPages((total + pageSize - 1L) / pageSize);
        if (total == 0L || offset >= total) {
            page.setRecords(Collections.emptyList());
            return page;
        }

        SelectQuery dataQuery = preparePageQuery(select);
        dataQuery.setLimit(pageSize);
        dataQuery.setOffset(start);
        SqlResult dataSql = SelectSqlSupport.translateDirect(dataQuery, SQL.getSqluxGlobal().getDbType());
        Class<?> resultType = resolveResultType(select, page);
        if (resultType == null) {
            page.setRecords((List<T>) SQL.getSqluxGlobal().getExecutor().query(dataSql));
        } else {
            page.setRecords((List<T>) SQL.getSqluxGlobal().getExecutor().query(dataSql, resultType));
        }
        return page;
    }

    private static SelectQuery preparePageQuery(AbsSelect select) {
        SelectQuery query = select.getQuery().copy();
        SelectSqlSupport.applyBeforeTranslate(query);
        return query;
    }

    private static SqlResult toCountSql(SelectQuery countQuery) {
        countQuery.clearOrderByColumns();
        countQuery.setLimit(null);
        countQuery.setOffset(null);
        SqlResult source = SelectSqlSupport.translateDirect(countQuery, SQL.getSqluxGlobal().getDbType());
        String sql = "SELECT count(*) AS total FROM (" + source.getSql() + ") sqlux_page_count";
        return new SqlResult(sql, new ArrayList<>(source.getParams()), SqlType.SELECT, countQuery);
    }

    private static long readTotal(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty() || rows.get(0).isEmpty()) {
            return 0L;
        }
        Object value = rows.get(0).values().iterator().next();
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 解析分页数据的返回值类型
     *
     * <p>如果分页对象里面指定了类型，则优先使用指定的<p/>
     * <p>否则使用查询语句中from主表的类型<p/>
     *
     * @param select 查询代码
     * @return 分页记录的类型
     */
    private static Class<?> resolveResultType(AbsSelect select, Page<?> page) {
        if (page.getRecordType() != null) {
            return page.getRecordType();
        }
        if (!Colls.isEmpty(select.getQuery().getJoins())) {
            SQL.getSqluxGlobal().getSqlLogger().log(Level.WARNING, "该分页查询涉及多表，却没指定分页记录类型，将使用FROM主表类型作为分页记录类型");
        }
        QuerySource from = select.getQuery().getFrom();
        if (from instanceof TableRef) {
            return ((TableRef) from).getTableClass();
        }
        return null;
    }

    private static int requirePositive(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + "必须大于0");
        }
        return value;
    }

    private static Integer toPageOffset(long value) {
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("分页偏移量不能大于" + Integer.MAX_VALUE);
        }
        return (int) value;
    }
}
