package com.service;

import com.entity.LogRecord;

import java.util.List;

public interface ILogRecordBatchService {
    void addLogList(List<LogRecord> datalist);
}
