package com.wenruo.dto;

import java.util.List;

/**
 * Created by huangyao on 2017/4/7.
 */
public class SchemaConditionDTO {

    private String dbName;
    private String tableName;
    private List<String> tableNameList;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getTableNameList() {
        return tableNameList;
    }

    public void setTableNameList(List<String> tableNameList) {
        this.tableNameList = tableNameList;
    }
}
