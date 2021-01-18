package com.listener;

import com.util.LogHelper;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.Properties;

/**
 * 目录事件监听
 */
@Component
public class FileListener implements FileAlterationListener {
    private Logger logger = LoggerFactory.getLogger(FileListener.class);
    //属性文件存储每个文件的读取位置
    private String posProp = "D:/workspace/idea_space/nginx-log/posProp.properties";

    @Resource
    private LogHelper logHelper;

    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(File file) {
        logger.info("[目录创建]:" + file.getAbsolutePath());
    }

    @Override
    public void onDirectoryChange(File file) {
        logger.info("[目录被修改]:" + file.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File file) {
        logger.info("[目录被删除]:" + file.getAbsolutePath());
    }

    @Override
    public void onFileCreate(File file) {
        logger.info("[文件创建]:" + file.getAbsolutePath());
    }

    @Override
    public void onFileChange(File file) {
        logger.info("[文件被修改]:" + file.getAbsolutePath());
        long startIndex = 0;
        InputStream in = null;
        OutputStream out = null;
        try {
            File f = new File(posProp);
            if(!f.exists()){
                f.createNewFile();
            }
            in = new BufferedInputStream(new FileInputStream(posProp));
            Properties p = new Properties();
            p.load(in);
            String key = file.getName()+"-index";
            String keyValue = p.getProperty(key);
            if(!StringUtils.isBlank(keyValue)){
                try {
                    startIndex = Long.parseLong(keyValue);
                }catch(Exception e){
                    startIndex = 0;
                }
            }
            logHelper.readLine(file.getAbsolutePath(), startIndex, -1, "FileListener-thread");
            p.setProperty(key,String.valueOf(file.length()));
            out = new FileOutputStream(file);
            p.store(out,"");
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null!=out){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



    }

    @Override
    public void onFileDelete(File file) {
        logger.info("[文件被删除]:" + file.getAbsolutePath());
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }

}