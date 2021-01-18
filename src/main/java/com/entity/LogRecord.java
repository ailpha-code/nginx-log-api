package com.entity;

import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

public class LogRecord {

    private Long id;
    private String remoteAddr;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;
    private String requestType;
    private String requestUrl;
    private String protocol;
    private Integer status;
    private Integer costTime;
    private String hostUrl;
    private String clientType;
    private String param;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRemoteAddr() {
        return remoteAddr;
    }
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }
    public Date getRecordTime() {
        return recordTime;
    }
    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }
    public String getRequestType() {
        return requestType;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public String getRequestUrl() {
        return requestUrl;
    }
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getCostTime() {
        return costTime;
    }
    public void setCostTime(Integer costTime) {
        this.costTime = costTime;
    }
    public String getHostUrl() {
        return hostUrl;
    }
    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }
    public String getClientType() {
        return clientType;
    }
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    public String getParam() { return param; }
    public void setParam(String param) { this.param = param; }
    @Override
    public String toString() {
        return "LogRecord [id=" + id + ", remoteAddr=" + remoteAddr
                + ", recordTime=" + recordTime + ", requestType=" + requestType
                + ", requestUrl=" + requestUrl + ", protocol=" + protocol
                + ", status=" + status + ", costTime=" + costTime
                + ", hostUrl=" + hostUrl + ", clientType=" + clientType
                + ", param=" + param + "]";
    }

}