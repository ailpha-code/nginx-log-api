package com.init;

import com.entity.LogRecord;
import com.service.ILogRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 初始化解析日志文件
 */
@Component
public class LogFileInit {
    @Resource
    private ILogRecordService logRecordServiceImpl;

    @Value("${accessLogFile}")
    String accessLogFile;

    @PostConstruct
    private void initInsertLog(){

        int count = logRecordServiceImpl.getTotal();
        if(count == 0){
            System.out.println("数据初始化处理中....初始化日志路径是："+accessLogFile);
            List<LogRecord> list = new ArrayList<>();
            String file = "D:\\workspace\\nginx-log-api-master\\1.txt";
            SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss Z]", Locale.ENGLISH);
            Pattern p = Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s\\-\\s\\-\\s(\\[[^\\[\\]]+\\])\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\d{3})\\s(\\d+|-)\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\\"(?:[^\"]|\\\")+|-\\\")");
            Matcher m = null;
            String line = null;
            FileInputStream inputStream = null;
            BufferedReader bufferedReader = null;

            try {
                inputStream = new FileInputStream(accessLogFile);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while((line = bufferedReader.readLine()) != null){
                    m = p.matcher(line);
                    if(m.find()){
                        LogRecord log = new LogRecord();
                        log.setRemoteAddr(m.group(1));
                        log.setRecordTime(sdf.parse(m.group(2)));
                        String str = m.group(3);
                        String method = null, target = null, protocol = null;

                        if (str.length()>0) {
                            try {
                                if (str.contains(" ") && str.indexOf(" ") < 50) {
                                    method = str.substring(0, str.indexOf(" "));
                                    if (str.indexOf(" ") != str.lastIndexOf(" ")
                                            && str.length() - str.lastIndexOf(" ") < 50) {
                                        target = str.substring(str.indexOf(" "), str.lastIndexOf(" "));
                                        protocol = str.substring(str.lastIndexOf(" "), str.length());
                                    } else {
                                        target = str.substring(str.indexOf(" "), str.length());
                                    }
                                } else {
                                    target = str != null && str.length() > 10000 ? str.substring(0, 10000) : str;
                                }

                                if (method != null) {
                                    method = method.trim();
                                    method = method.replace("\"","");
                                }

                                if (target != null) {
                                    target = target.trim();
                                    target = target.replace("\"","");
                                }

                                if (protocol != null) {
                                    protocol = protocol.trim();
                                    protocol = protocol.replace("\"","");
                                }
                                log.setRequestType(method);
                                log.setRequestUrl(target);
                                log.setProtocol(protocol);
                                log.setStatus(m.group(4) == null ? null : Integer.valueOf(m.group(4)));
                                log.setCostTime(m.group(5) == null ? null : Integer.valueOf(m.group(5)));
                                log.setHostUrl(m.group(6).replace("\"",""));
                                log.setClientType(m.group(7).replace("\"",""));

                                logRecordServiceImpl.addLog(log);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(bufferedReader!=null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
