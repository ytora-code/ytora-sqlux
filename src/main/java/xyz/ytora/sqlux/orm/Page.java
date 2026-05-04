package xyz.ytora.sqlux.orm;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页对象
 *
 * <p>封装了必要的分页信息</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class Page<T> {

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 页尺寸
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 总数据量
     */
    private Long total;

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    /**
     * 数据类型
     */
    private Class<T> recordType;

    public Page() {
        this(1, 10);
    }

    public Page(Class<T> recordType) {
        this(recordType, 1, 10);
    }

    public Page(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Page(Class<T> recordType, Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.recordType = recordType;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public static <R> Page<R> of(Integer pageNo, Integer pageSize) {
        return new Page<>(pageNo, pageSize);
    }

    public static <R> Page<R> of(Class<R> recordType, Integer pageNo, Integer pageSize) {
        return new Page<>(recordType, pageNo, pageSize);
    }

    public Page<T> setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Page<T> setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Long getPages() {
        return pages;
    }

    public Page<T> setPages(Long pages) {
        this.pages = pages;
        return this;
    }

    public Long getTotal() {
        return total;
    }

    public Page<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public List<T> getRecords() {
        return records;
    }

    public Page<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    public Class<T> getRecordType() {
        return recordType;
    }

    /**
     * 在不影响分页对象结构的同时，转换记录数据的类型，并返回新的分页对象
     * @param transFunc 转换函数
     * @return 警告转换函数转换后的分页对象
     * @param <R> 新的数据类型
     */
    public <R> Page<R> trans(Function<T, R> transFunc) {
        Page<R> newPage = new Page<>(getPageNo(), getPageSize());
        newPage.setPages(getPages());
        newPage.setTotal(getTotal());
        List<R> newRecord = getRecords().stream().map(transFunc).collect(Collectors.toList());
        newPage.setRecords(newRecord);
        return newPage;
    }

}
