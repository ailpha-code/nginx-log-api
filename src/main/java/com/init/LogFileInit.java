package com.init;

import com.entity.LogRecord;
import com.listener.FileListener;
import com.service.ILogRecordBatchService;
import com.service.ILogRecordService;
import com.util.LogHelper;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 初始化解析日志文件
 */
@Component
public class LogFileInit {
    private Logger logger = LoggerFactory.getLogger(LogFileInit.class);
    @Resource
    private ILogRecordBatchService logRecordBatchServiceImpl;
    @Resource
    private ILogRecordService logRecordServiceImpl;
    @Resource
    private LogHelper logHelper;

    @Value("${accessLogFile}")
    String accessLogFile;

    @Value("${monitorDir}")
    String monitorDir;

    //轮询间隔时间
    @Value("${monitorDirInterval}")
    long monitorDirInterval;
    //总线程数
    int threadTotal = 50;

    /**
     * 启动目录监听器
     */
    public void startFileMonitor(){
        logger.info("目录监听任务开始了，监控的目录是："+monitorDir);
        FileAlterationObserver observer = new FileAlterationObserver(monitorDir);
        observer.addListener(new FileListener());
        FileAlterationMonitor monitor = new FileAlterationMonitor(monitorDirInterval,observer);
        // 开始监控
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void initInsertLog(){
        int count = logRecordServiceImpl.getTotal();
        if(count == 0){
            logger.info("数据初始化处理中....初始化日志路径是："+accessLogFile);
            List<LogRecord> list = new ArrayList<>();
            File file = new File(accessLogFile);
            if(!file.exists()){
                logger.info(accessLogFile+" file does not exist!");
                return ;
            }
            //存储每个线程的读取起始和结束位置
            Map<Integer, Map<String, Long>> map = logHelper.setReadIndex(file,threadTotal);
            //开始多线程读取解析
            for (int i = 0; i < map.size(); i++) {
                Map<String,Long> tmap = map.get(i);
                int thIndex = i;
                new Thread(){
                    public void run() {
                        logHelper.readLine(accessLogFile,tmap.get("start"),tmap.get("end"),"线程"+thIndex);
                    }
                }.start();
            }
        }
        startFileMonitor();
    }
}
