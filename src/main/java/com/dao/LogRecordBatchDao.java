package com.dao;

import com.entity.LogRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * 考虑到大数据插入性能问题，改用jdbcTempalte
 */
@Repository
public class LogRecordBatchDao{

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 批量插入
     * @param datalist
     * @return
     */
    public  void batchInsert(List<LogRecord> datalist){
        if(null == datalist || datalist.size()==0){
            return ;
        }
        try {
            //namedParameterJdbcTemplate.getJdbcTemplate().getDataSource().getConnection().setAutoCommit(false);
            //关闭事务自动提交
            String sql = "INSERT INTO LogRecord (remoteAddr,recordTime,requestType,requestUrl,protocol,status,costTime,hostUrl,clientType,param)"
                    +"  VALUES  (:remoteAddr,:recordTime,:requestType,:requestUrl,:protocol,:status,:costTime,:hostUrl,:clientType,:param)";
            //批量转数组
            SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(datalist.toArray());
            namedParameterJdbcTemplate.batchUpdate(sql, beanSources);
        }catch (Exception e){
           e.printStackTrace();
        }finally{
//            try {
//                namedParameterJdbcTemplate.getJdbcTemplate().getDataSource().getConnection().setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }
    }
}
