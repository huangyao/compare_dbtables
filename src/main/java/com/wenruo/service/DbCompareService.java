package com.wenruo.service;

import com.wenruo.callable.TableColumnsCallable;
import com.wenruo.callable.TableNamesCallable;
import com.wenruo.dto.SchemaConditionDTO;
import com.wenruo.model.ColumnsDO;
import com.wenruo.support.ExecutorUtil;
import com.wenruo.vo.ComparedColumnPairVO;
import com.wenruo.vo.ComparedDbInfoVO;
import com.wenruo.vo.ComparedTableVO;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by huangyao on 2017/4/8.
 */
public class DbCompareService {

    private static DbCompareService instance = new DbCompareService();

    private static final String TITLE_TEMPLATE = "\n=-=-=-=-=-=  %s  =-=-=-=-=-=";
    private static final String TWO_COLUMNS_TEMPLATE_15W = "%-15s  %-15s";
    private static final String TWO_COLUMNS_TEMPLATE_30W = "%-30s  %-30s";
    private static final String TWO_COLUMNS_TEMPLATE_50W = "%-50s  %-50s";
    private static final String HORIZON = "==============================";

    private DbCompareService() {
    }

    public static DbCompareService getInstance() {
        return instance;
    }

    /**
     * 比较两个数据库的表结构差异
     *
     * @param dbMain
     * @param dbStandard
     */
    public void compareTwoDbsTableStructure(ComparedDbInfoVO dbMain, ComparedDbInfoVO dbStandard) throws ExecutionException, InterruptedException {
        // 比较结果全部输出到终端
        System.out.println("============ Compare Start ============");
        System.out.println(String.format("compared database name: [%s]", dbMain.getComparedDbName()));
        System.out.println(HORIZON);

        Future<List<String>> dbMainTableNamesFuture = ExecutorUtil.submit(createTableNamesCallable(dbMain));
        Future<List<String>> dbStandardTableNamesFuture = ExecutorUtil.submit(createTableNamesCallable(dbStandard));

        List<String> dbMainTableNameList = dbMainTableNamesFuture.get();
        List<String> dbStandardTableNameList = dbStandardTableNamesFuture.get();

        // 输出打印信息
        printDbSizeStat(dbMain.getName(), dbMainTableNameList.size(), dbStandard.getName(), dbStandardTableNameList.size());

        // 统计独有的表名
        // 所有表名全转化为小写的
        Map<String, String> particularTableMap = new HashMap<>();
        // 共有的表名称List
        List<String> commonTableNameList = new ArrayList<>();
        // 先统计dbMain
        for (String tableName : dbMainTableNameList) {
            particularTableMap.put(tableName.toLowerCase(), dbMain.getName());
        }
        // 再统计dbStandard
        for (String tableName : dbStandardTableNameList) {
            String lowerCaseTableName = tableName.toLowerCase();
            if (particularTableMap.get(lowerCaseTableName) != null) {
                // 存在，记录为共有的表，从map中移除
                commonTableNameList.add(lowerCaseTableName);
                particularTableMap.remove(lowerCaseTableName);
            } else {
                particularTableMap.put(lowerCaseTableName, dbStandard.getName());
            }
        }

        // 输出打印信息
        printParticularTableMap(particularTableMap);

        // 比较公有的表表结构差异
        List<ComparedTableVO> coreList = null;
        if (commonTableNameList.size() > 0) {
            // 有公有表，对比表结构
            // 防止查询数据过大，分批量查询，50张表一次
            // 异步查询，统一处理所有Future
            List<Future<List<ColumnsDO>>> dbMainFutureList = new LinkedList<>();
            List<Future<List<ColumnsDO>>> dbStandardFutureList = new LinkedList<>();
            int index = 0;
            for (; index < commonTableNameList.size(); ) {
                int length = Math.min(50, commonTableNameList.size() - index);
                List<String> queryTableNameList = new LinkedList<>();
                for (int i = 0; i < length; i++) {
                    // 查询的表名
                    queryTableNameList.add(commonTableNameList.get(index + i));
                }
                // index增长
                index += length;

                // dbMain查询Future
                dbMainFutureList.add(
                        ExecutorUtil.submit(
                                createTableColumnsCallable(dbMain.getComparedDbName(), queryTableNameList, dbMain.getSqlSessionFactory())
                        ));
                // dbStandard查询Future
                dbStandardFutureList.add(
                        ExecutorUtil.submit(
                                createTableColumnsCallable(dbStandard.getComparedDbName(), queryTableNameList, dbStandard.getSqlSessionFactory())
                        ));
            }

            // dbMain的聚合后的map
            Map<String, List<ColumnsDO>> dbMainColumnsGroupMap = new HashMap<>();
            // dbStandard的聚合后的map
            Map<String, List<ColumnsDO>> dbStandardColumnsGroupMap = new HashMap<>();
            for (int i = 0; i < dbMainFutureList.size(); i++) {
                groupColumnsDOByTableName(dbMainColumnsGroupMap, dbMainFutureList.get(i).get());
                groupColumnsDOByTableName(dbStandardColumnsGroupMap, dbStandardFutureList.get(i).get());
            }

            // 核心方法
            coreList = getDiffBetweenTwoMaps(dbMainColumnsGroupMap, dbStandardColumnsGroupMap);
        }

        // 输出比较信息
        printComparedTableVOList(dbMain.getName(), dbStandard.getName(), coreList);

        System.out.println("============ Compare Finished ============");
    }

    /**
     * 生成查询数据库表名的回调
     *
     * @param comparedDbInfoVO
     * @return
     */
    private TableNamesCallable createTableNamesCallable(ComparedDbInfoVO comparedDbInfoVO) {
        TableNamesCallable tableNamesCallable =
                new TableNamesCallable(comparedDbInfoVO.getSqlSessionFactory(), comparedDbInfoVO.getComparedDbName());
        return tableNamesCallable;
    }

    /**
     * 打印表数量统计信息
     *
     * @param dbMainName
     * @param dbMainTbaleNameListSize
     * @param dbStandardName
     * @param dbStandardTableNameListSize
     */
    private void printDbSizeStat(String dbMainName, int dbMainTbaleNameListSize, String dbStandardName,
                                 int dbStandardTableNameListSize) {
        System.out.println(String.format(TITLE_TEMPLATE, "DBSIZE STAT"));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_15W, "DB NAME", "TABLE QUANTITY"));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_15W, "---------------", "---------------"));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_15W, dbMainName, dbMainTbaleNameListSize));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_15W, dbStandardName, dbStandardTableNameListSize));
        System.out.println(HORIZON);
    }

    /**
     * 打印独有的表名称
     *
     * @param particularTableMap
     */
    private void printParticularTableMap(Map<String, String> particularTableMap) {
        if (particularTableMap.size() == 0) {
            return;
        }

        Set<String> keySet = particularTableMap.keySet();

        System.out.println(String.format(TITLE_TEMPLATE, "PARTICULAR TABLES"));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_30W, "TABLE NAME", "OWNER"));
        System.out.println(String.format(TWO_COLUMNS_TEMPLATE_30W, "---------------", "---------------"));
        for (String key : keySet) {
            System.out.println(String.format(TWO_COLUMNS_TEMPLATE_30W, key, particularTableMap.get(key)));
        }
        System.out.println(HORIZON);
    }

    /**
     * 生成查询Columns的查询
     *
     * @param dbName
     * @param queryTableNameList
     * @param sqlSessionFactory
     * @return
     */
    private TableColumnsCallable createTableColumnsCallable(String dbName, List<String> queryTableNameList,
                                                            SqlSessionFactory sqlSessionFactory) {
        SchemaConditionDTO conditionDTO = new SchemaConditionDTO();
        conditionDTO.setDbName(dbName);
        conditionDTO.setTableNameList(queryTableNameList);
        TableColumnsCallable callable = new TableColumnsCallable(conditionDTO, sqlSessionFactory);
        return callable;
    }

    /**
     * 聚合同一张表下的所有列信息
     *
     * @param map
     * @param columsDOList
     */
    private void groupColumnsDOByTableName(Map<String, List<ColumnsDO>> map, List<ColumnsDO> columsDOList) {
        for (ColumnsDO columnsDO : columsDOList) {
            String tableName = columnsDO.getTableName().toLowerCase();
            List<ColumnsDO> list = map.computeIfAbsent(tableName, k -> new LinkedList<>());
            list.add(columnsDO);
        }
    }

    /**
     * 比较两个map包含的表结构的区别
     *
     * @param dbMainColumnsGroupMap
     * @param dbStandardColumnsGroupMap
     * @return
     */
    private List<ComparedTableVO> getDiffBetweenTwoMaps(
            Map<String, List<ColumnsDO>> dbMainColumnsGroupMap, Map<String, List<ColumnsDO>> dbStandardColumnsGroupMap) {
        if (dbMainColumnsGroupMap == null || dbStandardColumnsGroupMap == null) {
            return null;
        }
        if (dbMainColumnsGroupMap.size() != dbStandardColumnsGroupMap.size()) {
            throw new RuntimeException("Table Structure is Modified.");
        }
        List<ComparedTableVO> result = new LinkedList<>();
        Set<String> tableNames = dbMainColumnsGroupMap.keySet();
        for (String tableName : tableNames) {
            List<ColumnsDO> dbMainColumns = dbMainColumnsGroupMap.get(tableName);
            List<ColumnsDO> dbStandardColumns = dbStandardColumnsGroupMap.get(tableName);
            if (dbStandardColumns == null) {
                throw new RuntimeException("Table Structure is Modified.");
            }
            // 排序两个链表
            Collections.sort(dbMainColumns, new ColumnsDOComparator());
            Collections.sort(dbStandardColumns, new ColumnsDOComparator());

            /* 比较列结构
             *
             * 逻辑如下：
             * 1. 当且仅当列名相同的对象的isNullAble和columnType相等时，判断列结构相同
             * 2. 使用两个指针来标记移动的下标
             */
            int dbMainIndex = 0;
            int dbStandardIndex = 0;
            List<ComparedColumnPairVO> comparedColumnPairVOList = new LinkedList<>();

            ColumnsDO dbMainColumnsDO = dbMainColumns.get(dbMainIndex++);
            ColumnsDO dbStandardColumnsDO = dbStandardColumns.get(dbStandardIndex++);
            while (true) {
                if (dbMainColumnsDO == null && dbStandardColumnsDO == null) {
                    break;
                }

                // 判断列名是否相同
                int comparedInt;
                if (dbMainColumnsDO == null) {
                    // 表示dbMainColumnsDO已经没有数据可以比较了，因此dbStandardColumnsDO还存在的列都是dbMain没有的
                    comparedInt = 1;
                } else if (dbStandardColumnsDO == null) {
                    // 与上个情况相反
                    comparedInt = -1;
                } else {
                    // 需要判断是否同一列
                    comparedInt = dbMainColumnsDO.getColumnName().compareTo(dbStandardColumnsDO.getColumnName());
                }

                ComparedColumnPairVO comparedColumnPairVO = null;
                if (comparedInt == 0) {
                    // 列名相同，判断结构
                    if (!compareColumnsDOStructureIsEqual(dbMainColumnsDO, dbStandardColumnsDO)) {
                        // 结构有差异，记录
                        comparedColumnPairVO = new ComparedColumnPairVO();
                        comparedColumnPairVO.setMain(dbMainColumnsDO);
                        comparedColumnPairVO.setStandard(dbStandardColumnsDO);
                    }
                } else if (comparedInt < 0) {
                    // 小于0，表示dbMainColumnsDO的列名在dbStandardColumnsDO中不存在
                    // 只记录dbMain的信息
                    comparedColumnPairVO = new ComparedColumnPairVO();
                    comparedColumnPairVO.setMain(dbMainColumnsDO);
                } else {
                    // 大于0，表示dbStandardColumnsDO的列名在dbMainColumnsDO中不存在
                    // 只记录dbStandard的信息
                    comparedColumnPairVO = new ComparedColumnPairVO();
                    comparedColumnPairVO.setStandard(dbStandardColumnsDO);
                }

                if (comparedColumnPairVO != null) {
                    comparedColumnPairVOList.add(comparedColumnPairVO);
                }

                if (comparedInt <= 0) {
                    if (dbMainIndex < dbMainColumns.size()) {
                        // 还有数据，取出来
                        dbMainColumnsDO = dbMainColumns.get(dbMainIndex++);
                    } else {
                        dbMainColumnsDO = null;
                    }
                }
                if (comparedInt >= 0) {
                    if (dbStandardIndex < dbStandardColumns.size()) {
                        dbStandardColumnsDO = dbStandardColumns.get(dbStandardIndex++);
                    } else {
                        dbStandardColumnsDO = null;
                    }
                }
            }

            // 列结构有差异的表记录添加
            if (comparedColumnPairVOList.size() > 0) {
                ComparedTableVO comparedTableVO = new ComparedTableVO();
                comparedTableVO.setTableName(tableName);
                comparedTableVO.setComparedColumnPairVOList(comparedColumnPairVOList);
                result.add(comparedTableVO);
            }
        }

        return result;
    }

    /**
     * 比较两个列的结构是否相同
     *
     * @param o1
     * @param o2
     * @return
     */
    private boolean compareColumnsDOStructureIsEqual(ColumnsDO o1, ColumnsDO o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        }

        return (o1.getIsNullable().equals(o2.getIsNullable()) &&
                o1.getColumnType().equals(o2.getColumnType()));
    }

    /**
     * 打印结构差异信息
     *
     * @param comparedTableVOList
     */
    private void printComparedTableVOList(String dbMainName, String dbStandardName,
                                          List<ComparedTableVO> comparedTableVOList) {
        System.out.println(String.format(TITLE_TEMPLATE, "TABLE STRUCTURE DIFFERENTS"));
        if (comparedTableVOList == null || comparedTableVOList.size() == 0) {
            System.out.println(String.format(TWO_COLUMNS_TEMPLATE_50W, "CONGRATUATIONS! NO DIFFERENTS", ""));
        } else {
            System.out.println(
                    String.format(TWO_COLUMNS_TEMPLATE_50W, dbMainName, dbStandardName)
            );
            System.out.println(
                    String.format(TWO_COLUMNS_TEMPLATE_50W, "-------------------------", "-------------------------")
            );

            for (ComparedTableVO comparedTableVO : comparedTableVOList) {
                System.out.println(
                        String.format(TWO_COLUMNS_TEMPLATE_15W, "\nTable Name:", comparedTableVO.getTableName()));

                for (ComparedColumnPairVO comparedColumnPairVO : comparedTableVO.getComparedColumnPairVOList()) {
                    System.out.println(
                            String.format(TWO_COLUMNS_TEMPLATE_50W,
                                    getColumnsDOPrintInfo(comparedColumnPairVO.getMain()),
                                    getColumnsDOPrintInfo(comparedColumnPairVO.getStandard()))
                    );
                }
                System.out.println("\n");
            }
        }

    }

    private String getColumnsDOPrintInfo(ColumnsDO columnsDO) {
        if (columnsDO == null) {
            return "NO COLUMN";
        }
        return columnsDO.getColumnName() + " | " + columnsDO.getColumnType() + " | IsNullable: " + columnsDO.getIsNullable();
    }
}

class ColumnsDOComparator implements Comparator<ColumnsDO> {

    @Override
    public int compare(ColumnsDO o1, ColumnsDO o2) {
        String columnName_1 = o1.getColumnName();
        String columnName_2 = o2.getColumnName();

        return columnName_1.compareTo(columnName_2);
    }
}
