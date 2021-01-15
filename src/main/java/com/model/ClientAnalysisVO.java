package com.model;

/**
 * 访问者分析的对象，分析统计用
 */
public class ClientAnalysisVO {
    //访问次数
    private Long visitCount;

    private String clientIP;

    public Long getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }
}
