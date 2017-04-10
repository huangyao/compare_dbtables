package com.wenruo.vo;


import java.util.List;

/**
 * Created by huangyao on 2017/4/7.
 */
public class ComparedTableVO {

    private String tableName;
    private List<ComparedColumnPairVO> comparedColumnPairVOList;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ComparedColumnPairVO> getComparedColumnPairVOList() {
        return comparedColumnPairVOList;
    }

    public void setComparedColumnPairVOList(List<ComparedColumnPairVO> comparedColumnPairVOList) {
        this.comparedColumnPairVOList = comparedColumnPairVOList;
    }
}
