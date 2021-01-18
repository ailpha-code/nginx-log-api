package com.service.impl;

import com.dao.LogRecordBatchDao;
import com.entity.LogRecord;
import com.service.ILogRecordBatchService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * 日志批量处理service
 */
@Service
public class LogRecordBatchServiceImpl implements ILogRecordBatchService {
    @Resource
    private LogRecordBatchDao logRecordBatchDao;

    //批量提交
    public void addLogList(List<LogRecord> datalist) {
        logRecordBatchDao.batchInsert(datalist);
    }
}
