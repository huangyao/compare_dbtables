package com.wenruo.dao;

import com.wenruo.dto.SchemaConditionDTO;
import com.wenruo.model.ColumnsDO;

import java.util.List;

/**
 * Created by huangyao on 2017/4/7.
 */
public interface ColumnsMapper {

    /**
     * 获取列信息
     *
     * @param schemaConditionDTO
     * @return
     */
    List<ColumnsDO> getColumnsDOList(SchemaConditionDTO schemaConditionDTO);
}
