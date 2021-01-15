package com.service.impl;

import com.entity.LogRecord;
import com.mapper.LogRecordMapper;
import com.model.APIAnalysisVO;
import com.model.ClientAnalysisVO;
import com.model.ErrorAnalysisVO;
import com.service.ILogRecordService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class LogRecordServiceImpl implements ILogRecordService {
    @Resource
    private LogRecordMapper logRecordMapper;

    @Override
    public Integer addLog(LogRecord log) {
        return logRecordMapper.addLog(log);
    }

    @Override
    public Integer deleteLogById(Long id) {
        return logRecordMapper.deleteLogById(id);
    }

    @Override
    public Integer updateLog(LogRecord log) {
        return logRecordMapper.updateLog(log);
    }

    @Override
    public List<LogRecord> list(int pageNum,int paseSize) {
        int start = (pageNum-1)*paseSize;
        return logRecordMapper.list(start,paseSize);
    }

    @Override
    public LogRecord getById(Long id) { return logRecordMapper.getById(id); }

    @Override
    public Integer getTotal() {return logRecordMapper.getTotal();}

    @Override
    public List<ClientAnalysisVO> countListByRemoteAddr(String requestUrl,int status, int pageNum, int paseSize) {
        int start = (pageNum-1)*paseSize;
        return logRecordMapper.countListByRemoteAddr(requestUrl,status, start,paseSize);
    }
    @Override
    public List<APIAnalysisVO> countListByUrl(String remoteAddr, int status,int pageNum, int paseSize) {
        int start = (pageNum-1)*paseSize;
        return logRecordMapper.countListByUrl(remoteAddr, status, start,paseSize);
    }

    @Override
    public List<ErrorAnalysisVO> countListByStatus(int pageNum, int paseSize) {
        int start = (pageNum-1)*paseSize;
        return logRecordMapper.countListByStatus(start,paseSize);
    }

    @Override
    public ResponseEntity<byte[]> export(String startTime, String endTime) {
        ResponseEntity<byte[]> result = null;
        List<LogRecord> list = logRecordMapper.listByTime(startTime, endTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = sdf.format(new Date())+"-log.xlsx";
        result = creatExcel(list,fileName);
        return result;
    }

    public ResponseEntity<byte[]> creatExcel(List<LogRecord> list, String fileName){

        ByteArrayOutputStream out = null;
        Workbook  workbook = null;
        ResponseEntity<byte[]> filebyte = null;
        try {
            out = new ByteArrayOutputStream();
            //创建工作薄
            workbook= new HSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();  //创建帮助工具
            //创建新的一页
            Sheet sheet = workbook.createSheet();
            Row headRow = sheet.createRow(0);
            String[]  title = {"访问者ip","时间","请求方式","访问路径","协议类型","状态","花费时间","host地址","客户端信息"};
            for (int i=0;i<title.length;i++){  //遍历表头数据
                Cell cell = headRow.createCell(i);  //创建单元格
                cell.setCellValue(createHelper.createRichTextString(title[i]));//设置值
            }

            if(null !=list && list.size()>0){
                //数据填充
                for (int i=0; i< list.size(); i++){
                    LogRecord log = list.get(i);
                    Row dataRow = sheet.createRow(i+1);
                    Cell c0 = dataRow.createCell(0);
                    c0.setCellValue(createHelper.createRichTextString(log.getRemoteAddr()));
                    Cell c1 = dataRow.createCell(1);
                    c1.setCellValue(createHelper.createRichTextString(log.getRecordTime().toString()));
                    Cell c2 = dataRow.createCell(2);
                    c2.setCellValue(createHelper.createRichTextString(log.getRequestType()));
                    Cell c3 = dataRow.createCell(3);
                    c3.setCellValue(createHelper.createRichTextString(log.getRequestUrl()));
                    Cell c4 = dataRow.createCell(4);
                    c4.setCellValue(createHelper.createRichTextString(log.getProtocol()));
                    Cell c5 = dataRow.createCell(5);
                    c5.setCellValue(createHelper.createRichTextString(String.valueOf(log.getStatus())));
                    Cell c6 = dataRow.createCell(6);
                    c6.setCellValue(createHelper.createRichTextString(String.valueOf(log.getCostTime())));
                    Cell c7 = dataRow.createCell(7);
                    c7.setCellValue(createHelper.createRichTextString(log.getHostUrl()));
                    Cell c8 = dataRow.createCell(8);
                    c8.setCellValue(createHelper.createRichTextString(log.getClientType()));
                }
            }

            //把创建的内容写入到输出流中，并关闭输出流
            workbook.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            filebyte = new ResponseEntity<byte[]>(out.toByteArray(),headers, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != workbook){
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != out){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filebyte;
    }
}
