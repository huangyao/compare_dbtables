package com.wenruo.dao;

import com.wenruo.dto.SchemaConditionDTO;

import java.util.List;

/**
 * Created by huangyao on 2017/4/7.
 */
public interface InformationSchemaMapper {

    /**
     * 根据条件获取表名
     *
     * @param schemaConditionDTO
     * @return
     */
    List<String> getTableNames(SchemaConditionDTO schemaConditionDTO);
}
