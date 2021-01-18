package com.controller;

import com.entity.LogRecord;
import com.model.APIAnalysisVO;
import com.model.ClientAnalysisVO;
import com.model.ErrorAnalysisVO;
import com.service.ILogRecordService;
import com.service.impl.LogRecordBatchServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class NginxLogController {

    @Resource
    private ILogRecordService logRecordServiceImpl;

    /**
     * 获取列表
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value = "/log",method = RequestMethod.GET)
    public List<LogRecord> list(Integer pageNum,Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<LogRecord> list = logRecordServiceImpl.list(pageNum,paseSize);
        return list;
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/log/{id}",method = RequestMethod.DELETE)
    public Integer delete(@PathVariable Long id){
        return logRecordServiceImpl.deleteLogById(id);
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    @RequestMapping(value="/log/{id}", method = RequestMethod.GET)
    public LogRecord get(@PathVariable Long id){
        LogRecord log = logRecordServiceImpl.getById(id);
        return log;
    }

    /**
     * 新增
     * @param logRecord
     * @return
     */
    @RequestMapping(value="/log", method = RequestMethod.POST)
    public Integer add(LogRecord logRecord){
        return logRecordServiceImpl.addLog(logRecord);
    }

    /**
     * 修改
     * @param logRecord
     * @return
     */
    @RequestMapping(value="/log", method = RequestMethod.PUT)
    public Integer update(LogRecord logRecord){
        int ret = 0;
        if(null != logRecord && null != logRecord.getId()){
            ret= logRecordServiceImpl.updateLog(logRecord);
        }
        return ret;
    }
    /**
     * 根据开始结束时间，查询然后导出到excel
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value="/log/export",method = RequestMethod.GET)
    public ResponseEntity<byte[]> export(String startTime, String endTime){
        return logRecordServiceImpl.export(startTime,endTime);
    }


    /**
     * 访问者分析，根据访问者IP分析，获取到每个IP访问次数
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value="/analysis/client", method = RequestMethod.GET)
    public List<ClientAnalysisVO> clientAnalysis(Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<ClientAnalysisVO> list = logRecordServiceImpl.countListByRemoteAddr(null, -1,pageNum,paseSize);
        return list;
    }

    /**
     * 访问者分析，根据指定的访问者IP，获取访问者访问各个接口URL的情况
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value="/analysis/client/{clientIP}", method = RequestMethod.GET)
    public List<APIAnalysisVO> clientDetailAnalysis(@PathVariable String clientIP, Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<APIAnalysisVO> list = logRecordServiceImpl.countListByUrl(clientIP,-1,pageNum,paseSize);
        return list;
    }

    /**
     * 被访问者分析，根据API接口，查看各API访问次数
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value="/analysis/api",method = RequestMethod.GET)
    public List<APIAnalysisVO> apiAnalysis(Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<APIAnalysisVO> list = logRecordServiceImpl.countListByUrl(null,-1,pageNum,paseSize);
        return list;
    }

    /**
     * 被访问者分析，根据指定的接口，获取接口下各个客户端访问次数
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value="/analysis/apiDetail",method = RequestMethod.GET)
    public List<ClientAnalysisVO> apiDetailAnalysis(@Param("apiUrl") String apiUrl, Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<ClientAnalysisVO> list = logRecordServiceImpl.countListByRemoteAddr(apiUrl,-1,pageNum,paseSize);
        return list;
    }

    /**
     * 根据错误码统计的错误分析
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value = "/analysis/error",method = RequestMethod.GET)
    public List<ErrorAnalysisVO> errorAnalysis(Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        List<ErrorAnalysisVO> list = logRecordServiceImpl.countListByStatus(pageNum,paseSize);
        return list;
    }

    /**
     * 获取指定的错误码中，访问接口发生错误的次数
     * @param pageNum
     * @param paseSize
     * @return
     */
    @RequestMapping(value = "/analysis/error/{status}",method = RequestMethod.GET)
    public List<APIAnalysisVO> errorAnalysis(@PathVariable Integer status,Integer pageNum, Integer paseSize){
        if(null == pageNum ){
            pageNum = 1;
        }
        if(null == paseSize ){
            paseSize = 20;
        }
        if(null == status ){
            status = HttpStatus.BAD_REQUEST.value();
        }
        List<APIAnalysisVO> list = logRecordServiceImpl.countListByUrl(null,status,pageNum,paseSize);
        return list;
    }
}
