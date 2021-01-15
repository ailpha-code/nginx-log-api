package com.mapper;

import com.entity.LogRecord;
import com.model.APIAnalysisVO;
import com.model.ClientAnalysisVO;
import com.model.ErrorAnalysisVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface LogRecordMapper {
    Integer addLog(LogRecord log);

    Integer deleteLogById(Long id);

    Integer updateLog(LogRecord log);

    List<LogRecord> list(int start,int paseSize);

    LogRecord getById(Long id);

    Integer getTotal();

    List<LogRecord> listByTime(String startTime, String endTime);

    List<ClientAnalysisVO> countListByRemoteAddr(@Param("requestUrl") String requestUrl, @Param("status") int status, int start, int paseSize);

    List<APIAnalysisVO> countListByUrl(@Param("remoteAddr") String remoteAddr, @Param("status") int status, int start, int paseSize);

    List<ErrorAnalysisVO> countListByStatus(int start, int paseSize);
}
