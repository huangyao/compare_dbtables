package com.wenruo.callable;

import com.wenruo.dao.ColumnsMapper;
import com.wenruo.dto.SchemaConditionDTO;
import com.wenruo.model.ColumnsDO;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by huangyao on 2017/4/10.
 */
public class TableColumnsCallable implements Callable<List<ColumnsDO>> {

    private SchemaConditionDTO schemaConditionDTO;
    private SqlSessionFactory sqlSessionFactory;

    public TableColumnsCallable(SchemaConditionDTO schemaConditionDTO, SqlSessionFactory sqlSessionFactory) {
        this.schemaConditionDTO = schemaConditionDTO;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public List<ColumnsDO> call() throws Exception {
        SqlSession session = sqlSessionFactory.openSession();
        List<ColumnsDO> columnsDOList;
        try {
            ColumnsMapper mapper = session.getMapper(ColumnsMapper.class);
            columnsDOList = mapper.getColumnsDOList(schemaConditionDTO);
        } finally {
            session.close();
        }
        return columnsDOList;
    }
}
