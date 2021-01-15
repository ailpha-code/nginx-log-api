package com.model;

/**
 * 错误分析VO，页面统计用
 */
public class ErrorAnalysisVO {

    private Long errCount;

    private Integer status;

    public Long getErrCount() {
        return errCount;
    }

    public void setErrCount(Long errCount) {
        this.errCount = errCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

