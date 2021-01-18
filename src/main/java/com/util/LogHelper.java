package com.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dao.LogRecordBatchDao;
import com.service.ILogRecordBatchService;
import com.service.ILogRecordService;
import com.service.impl.LogRecordServiceImpl;
import org.apache.commons.lang3.StringUtils;
import com.entity.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 文件操作帮助类
 *
 */
@Component
public class LogHelper {

    private  LogHelper logHelper;
    private  Logger logger = LoggerFactory.getLogger(LogHelper.class);
    // 定义换行符，不同操作系统的有不同
    public  byte endStr = "\n".getBytes()[0];
    public  int readbuffsize = 2048;

    @Resource
    private  ILogRecordBatchService logRecordBatchServiceImpl;

    public void init(){
        logHelper = this;
    }
    // 设置每个线程读取文件的开始和结束位置
    public  Map<Integer, Map<String, Long>> setReadIndex(File file, int threadTotal) {
        Map<Integer, Map<String, Long>> map = new HashMap<Integer, Map<String, Long>>();

        // 源文件总长度
        long totalLength = file.length();
        // 每个线程需要读取的长度
        long perSize = totalLength / threadTotal;
        for (int i = 0; i < threadTotal; i++) {
            long startIndex = i * perSize;
            // 最后一个线程一直读到文件结尾的地方,-1表示读到结尾
            long endIndex = (i == (threadTotal - 1) ? (-1) : (startIndex + perSize - 1));
            // 存储每个线程起始和结束的索引
            Map<String, Long> threadReadIndexMap = new HashMap<String, Long>();
            threadReadIndexMap.put("start", startIndex);
            threadReadIndexMap.put("end", endIndex);
            map.put(i, threadReadIndexMap);
        }
        // 修正每个线程开始的地方，令每个线程读取的都是完整的一行一行的数据
        // 从第2个线程开始修正，每个线程修改自己的开始位置，并修改上一个线程结束的位置
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(file, "r").getChannel();
            for (int i = 1; i < threadTotal; i++) {
                Map<String, Long> curMap = map.get(i);
                long start = curMap.get("start");
                fc.position(start);
                ByteBuffer buffer = ByteBuffer.allocate(readbuffsize);
                int rsize = fc.read(buffer);
                byte isKey;
                for (int j = 0; j < rsize; j++) {
                    isKey = buffer.get(j);
                    // 定位到换行符的位置
                    if (endStr == isKey) {
                        start += j;
                        break;
                    }
                }
                // 清空buffer
                buffer.clear();
                // 修正开始位置
                curMap.put("start", start + 1);
                // 修正上一个的结束位置
                map.get(i - 1).put("end", start);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    // 多线程按行读取,endIndex为-1则读到文件结束的地方
    public  void readLine(String fileName, long startIndex,long endIndex, String threadName) {
        //logger.info(threadName + "读取文件" + fileName +"开始了，startIndex："+startIndex+", endIndex:"+endIndex);
        long lineNumber = 0;
        FileChannel fileChannel = null;

        if ((endIndex > 0) && (startIndex > endIndex)) {
            logger.info(threadName + "读取文件" + fileName + " startIndex位置不正确！");
            return;
        }
        // 当前读取位置
        long posIndex = startIndex;
        try {
            fileChannel = new RandomAccessFile(fileName, "r").getChannel();
            fileChannel.position(startIndex);
            ByteBuffer buffer = ByteBuffer.allocate(readbuffsize);
            //一次批量插入数据库的条数
            int datacapa = 20000;
            byte b;
            // 上一次多余的半行
            byte[] lastExtra = new byte[0];
            //到了1000行批量提交
            List dataList = new ArrayList(datacapa);
            boolean isEnd = false;
            while (true) {
                int fread = fileChannel.read(buffer);
                if (fread == -1) {
                    isEnd = true;
                } else {
                    posIndex += fread;
                    if ((endIndex > 0) && (posIndex >= endIndex)) {
                        isEnd = true;
                    }
                }
                //结束了提交最后一批数据到数据库
                if(isEnd){
                    try {
                        logRecordBatchServiceImpl.addLogList(dataList);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    dataList.clear();
                    break;
                }
                buffer.flip();
                int lineIndex = 0;
                String line = "";
                // 完整的一行数据
                byte[] lineByte = new byte[0];
                byte[] tempByte = new byte[0];
                int rsize = buffer.limit();
                byte[] readcach = new byte[rsize];
                buffer.get(readcach);
                for (int i = 0; i < rsize; i++) {
                    b = readcach[i];
                    // 遇到换行符
                    if (endStr == b) {
                        tempByte = new byte[i - lineIndex + 1];
                        System.arraycopy(readcach, lineIndex, tempByte, 0, tempByte.length);
                        lineByte = tempByte;
                        // 如果是第一次换行，要检测是否需要拼接上次多余的半行数据
                        if (lineIndex == 0 && lastExtra.length > 0) {
                            lineByte = new byte[lastExtra.length + tempByte.length];
                            System.arraycopy(lastExtra, 0, lineByte, 0, lastExtra.length);
                            System.arraycopy(tempByte, 0, lineByte, lastExtra.length, tempByte.length);
                            // 清空上次残余的半行
                            lastExtra = new byte[0];
                        }
                        lineIndex = i + 1;
                        line = new String(lineByte);
                        // 清空lineByte
                        lineByte = new byte[0];

                        //数据解析
                        LogRecord log = parseLine(line);
                        if(null!=log){
                            lineNumber++;
                            dataList.add(log);
                        }
                        //到了1000行批量插入到数据库，
                        if((dataList.size() == datacapa)){
                            //logger.info(threadName+"准备插入"+dataList.size()+"数据");
                            try {
                                logRecordBatchServiceImpl.addLogList(dataList);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            //清空list
                            dataList.clear();
                        }
                    }

                    // 一次缓存读取，最后一位不是换行符，说明有多余的半行数据
                    if ((i == (rsize - 1)) && (endStr != b)) {
                        // 半行存储起来，下一次读取的时候，第一次遇到换行符，要把这个半行拼接起来
                        lastExtra = new byte[i - lineIndex + 1];
                        System.arraycopy(readcach, lineIndex, lastExtra, 0, lastExtra.length);
                    }
                }
                // 清空buffer
                buffer.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fileChannel) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info(threadName + "读取了" + lineNumber + "行数据。");
    }

    // 根据一行的内容解析成对象
    public  LogRecord parseLine(String line) {
        LogRecord log = null;
        line = line.replaceAll("\r|\n", "");
        SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss Z]",Locale.ENGLISH);
        Pattern p = Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s\\-\\s\\-\\s(\\[[^\\[\\]]+\\])\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\d{3})\\s(\\d+|-)\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\\"(?:[^\"]|\\\")+|-\\\")\\s(\\\"(?:[^\"]|\\\")+|-\\\")");
        Matcher m = null;
        m = p.matcher(line);

        try {
            if (m.find()) {
                log = new LogRecord();
                log.setRemoteAddr(m.group(1));
                log.setRecordTime(sdf.parse(m.group(2)));
                String str = m.group(3);
                String method = null, target = null, protocol = null, param = null;
                if(!StringUtils.isBlank(str)){
                    String [] arr = str.split("\\s+");
                    if(arr.length ==1){
                        target = str;
                        method = "-";
                        protocol = "-";
                    }else if(arr.length ==2){
                        method = arr[0];
                        target = arr[1];
                        protocol = "-";
                    }else{
                        method = arr[0];
                        target = arr[1];
                        protocol = arr[2];
                    }
                    if (method != null) {
                        method = method.trim();
                        method = method.replace("\"","");
                    }
                    if (target != null) {
                        target = target.trim();
                        target = target.replace("\"","");
                        String[] tarArr = target.split("\\?");
                        if(tarArr.length>1){
                            target = tarArr[0];
                            param = tarArr[1];
                        }
                    }
                    if (protocol != null) {
                        protocol = protocol.trim();
                        protocol = protocol.replace("\"","");
                    }
                    if(target.length()>1000){
                        target = target.substring(0,1000);
                    }
                    if(method.length()>256){
                        method = method.substring(0,255);
                    }
                    if(protocol.length()>256){
                        protocol = protocol.substring(0,255);
                    }
                    log.setRequestType(method);
                    log.setRequestUrl(target);
                    log.setProtocol(protocol);
                    log.setParam(param);
                    log.setStatus(m.group(4) == null ? null : Integer.valueOf(m.group(4)));
                    log.setCostTime(m.group(5) == null ? null : Integer.valueOf(m.group(5)));
                    log.setHostUrl(m.group(6).replaceAll("\"", ""));
                    log.setClientType(m.group(7));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return log;
    }
}

