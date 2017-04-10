package com.wenruo.support;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangyao on 2017/4/7.
 */
public class SqlSessionFactoryUtil {

    private static final Map<String, SqlSessionFactory> CACHED_SQL_SESSION_FACTORY_POOL = new HashMap<>();

    private SqlSessionFactoryUtil() {
    }

    /**
     * 获取或初始化一个可缓存的SqlSessionFactory对象
     *
     * @param driver
     * @param url
     * @param username
     * @param password
     * @return
     */
    public static SqlSessionFactory getOrInitCachedSqlSessionFactory(String driver, String url, String username, String password) {
        String key = url + username + password;
        SqlSessionFactory sqlSessionFactory = CACHED_SQL_SESSION_FACTORY_POOL.get(key);
        if (sqlSessionFactory == null) {
            synchronized (SqlSessionFactoryUtil.class) {
                sqlSessionFactory = CACHED_SQL_SESSION_FACTORY_POOL.get(key);
                if (sqlSessionFactory == null) {
                    sqlSessionFactory = getNewSqlSessionFactory(driver, url, username, password);
                    CACHED_SQL_SESSION_FACTORY_POOL.put(key, sqlSessionFactory);
                }
            }
        }
        return sqlSessionFactory;
    }

    public static SqlSessionFactory getNewSqlSessionFactory(String driver, String url, String username, String password) {
        DataSource dataSource = new PooledDataSource(driver, url, username, password);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        // hardcode
        configuration.addMappers("com.wenruo.dao");
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
