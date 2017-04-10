package com.wenruo.callable;

import com.wenruo.dao.InformationSchemaMapper;
import com.wenruo.dto.SchemaConditionDTO;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by huangyao on 2017/4/8.
 */
public class TableNamesCallable implements Callable<List<String>> {

    private SqlSessionFactory sqlSessionFactory;
    private String dbName;

    public TableNamesCallable(SqlSessionFactory sqlSessionFactory, String dbName) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.dbName = dbName;
    }

    @Override
    public List<String> call() throws Exception {
        SchemaConditionDTO schemaConditionDTO = new SchemaConditionDTO();
        schemaConditionDTO.setDbName(dbName);
        SqlSession session = sqlSessionFactory.openSession();
        List<String> tableNames;
        try {
            InformationSchemaMapper mapper = session.getMapper(InformationSchemaMapper.class);
            tableNames = mapper.getTableNames(schemaConditionDTO);
        } finally {
            session.close();
        }
        return tableNames;
    }
}
