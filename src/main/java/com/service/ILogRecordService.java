package com.service;

import com.entity.LogRecord;
import com.model.APIAnalysisVO;
import com.model.ClientAnalysisVO;
import com.model.ErrorAnalysisVO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

public interface ILogRecordService {
    Integer addLog(LogRecord log);

    Integer deleteLogById(Long id);

    Integer updateLog(LogRecord log);

    List<LogRecord> list(int pageNum,int paseSize);

    LogRecord getById(Long id);

    Integer getTotal();

    ResponseEntity<byte[]> export(String startTime, String endTime);

    List<ClientAnalysisVO> countListByRemoteAddr(String requestUrl,int status, int pageNum, int paseSize);

    List<APIAnalysisVO> countListByUrl(String remoteAddr, int status, int pageNum, int paseSize);

    List<ErrorAnalysisVO> countListByStatus(int pageNum, int paseSize);

    //void addLogList(List<LogRecord> datalist);
}
