package xyz.ytora.sqlux.meta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 外键元数据
 */
public class ForeignKeyMeta {

    /**
     * 外键名称
     */
    private String name;

    /**
     * pkTable
     */
    private String pkTable;

    /**
     * pk catalog
     */
    private String pkCatalog;

    /**
     * pk schema
     */
    private String pkSchema;

    /**
     * fkTable
     */
    private String fkTable;

    /**
     * fk catalog
     */
    private String fkCatalog;

    /**
     * fk schema
     */
    private String fkSchema;

    /**
     * deleteRule
     */
    private short deleteRule;

    /**
     * updateRule
     */
    private short updateRule;

    /**
     * 外键列映射（多列）
     */
    private List<ForeignKeyColumnMeta> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkTable() {
        return pkTable;
    }

    public void setPkTable(String pkTable) {
        this.pkTable = pkTable;
    }

    public String getPkCatalog() {
        return pkCatalog;
    }

    public void setPkCatalog(String pkCatalog) {
        this.pkCatalog = pkCatalog;
    }

    public String getPkSchema() {
        return pkSchema;
    }

    public void setPkSchema(String pkSchema) {
        this.pkSchema = pkSchema;
    }

    public String getFkTable() {
        return fkTable;
    }

    public void setFkTable(String fkTable) {
        this.fkTable = fkTable;
    }

    public String getFkCatalog() {
        return fkCatalog;
    }

    public void setFkCatalog(String fkCatalog) {
        this.fkCatalog = fkCatalog;
    }

    public String getFkSchema() {
        return fkSchema;
    }

    public void setFkSchema(String fkSchema) {
        this.fkSchema = fkSchema;
    }

    public short getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(short deleteRule) {
        this.deleteRule = deleteRule;
    }

    public short getUpdateRule() {
        return updateRule;
    }

    public void setUpdateRule(short updateRule) {
        this.updateRule = updateRule;
    }

    public List<ForeignKeyColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ForeignKeyColumnMeta> columns) {
        this.columns = columns;
    }

    public void addColumn(short seq, String fkColumn, String pkColumn) {
        ForeignKeyColumnMeta c = new ForeignKeyColumnMeta();
        c.setSeq(seq);
        c.setFkColumn(fkColumn);
        c.setPkColumn(pkColumn);
        this.columns.add(c);
    }
}
