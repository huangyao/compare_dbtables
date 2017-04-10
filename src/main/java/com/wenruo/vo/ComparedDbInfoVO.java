package com.wenruo.vo;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Created by huangyao on 2017/4/7.
 */
public class ComparedDbInfoVO {

    private String name;
    private String comparedDbName;
    private SqlSessionFactory sqlSessionFactory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComparedDbName() {
        return comparedDbName;
    }

    public void setComparedDbName(String comparedDbName) {
        this.comparedDbName = comparedDbName;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
