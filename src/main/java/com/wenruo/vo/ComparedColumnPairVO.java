package com.wenruo.vo;

import com.wenruo.model.ColumnsDO;

/**
 * Created by huangyao on 2017/4/10.
 */
public class ComparedColumnPairVO {

    private ColumnsDO main;
    private ColumnsDO standard;

    public ColumnsDO getMain() {
        return main;
    }

    public void setMain(ColumnsDO main) {
        this.main = main;
    }

    public ColumnsDO getStandard() {
        return standard;
    }

    public void setStandard(ColumnsDO standard) {
        this.standard = standard;
    }
}
