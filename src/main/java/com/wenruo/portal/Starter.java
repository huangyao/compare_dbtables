package com.wenruo.portal;

import com.wenruo.service.DbCompareService;
import com.wenruo.support.Config;
import com.wenruo.support.SqlSessionFactoryUtil;
import com.wenruo.vo.ComparedDbInfoVO;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by huangyao on 2017/4/7.
 */
public class Starter {

    /**
     * 主要数据库的配置文件前缀
     */
    private static final String DB_MAIN_PREFIX = "db-main";
    /**
     * 标准数据库的配置文件前缀
     */
    private static final String DB_STANDARD_PREFIX = "db-standard";

    private static final String CONFIG_NAME = "name";
    private static final String CONFIG_URL = "url";
    private static final String CONFIG_USERNAME = "username";
    private static final String CONFIG_PASSWORD = "password";

    private static final String COMPARED_DB_NAME = "compared.db.name";

    public static void main(String[] args) {
        ComparedDbInfoVO dbMain = getConfiguredSqlSessionFactory(DB_MAIN_PREFIX);
        ComparedDbInfoVO dbStandard = getConfiguredSqlSessionFactory(DB_STANDARD_PREFIX);

        try {
            DbCompareService.getInstance().compareTwoDbsTableStructure(dbMain, dbStandard);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ComparedDbInfoVO getConfiguredSqlSessionFactory(String prefix) {
        StringBuilder linker = new StringBuilder();

        ComparedDbInfoVO comparedDbInfoVO = new ComparedDbInfoVO();

        // 对象名称，作为日志输出用
        linker.append(prefix).append(".").append(CONFIG_NAME);
        comparedDbInfoVO.setName(Config.getString(toStringAndCleanBuffer(linker), prefix));
        // 比较的数据库名
        comparedDbInfoVO.setComparedDbName(Config.getString(COMPARED_DB_NAME));

        // TODO 驱动，第一版本写死默认mysql，后续考虑增加灵活性
        String driver = "com.mysql.jdbc.Driver";
        // URL
        linker.append(prefix).append(".").append(CONFIG_URL);
        String url = Config.getString(toStringAndCleanBuffer(linker));
        // username
        linker.append(prefix).append(".").append(CONFIG_USERNAME);
        String username = Config.getString(toStringAndCleanBuffer(linker));
        // password
        linker.append(prefix).append(".").append(CONFIG_PASSWORD);
        String password = Config.getString(toStringAndCleanBuffer(linker));

        SqlSessionFactory sqlSessionFactory =
                SqlSessionFactoryUtil.getOrInitCachedSqlSessionFactory(driver, url, username, password);

        // 对应的SqlSessionFactory
        comparedDbInfoVO.setSqlSessionFactory(sqlSessionFactory);

        return comparedDbInfoVO;
    }

    private static String toStringAndCleanBuffer(StringBuilder sb) {
        String str = sb.toString();
        sb.setLength(0);
        return str;
    }
}
