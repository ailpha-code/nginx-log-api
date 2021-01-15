package com.model;

/**
 * 根据api接口生成的Model对象，页面统计用
 */
public class APIAnalysisVO {

    private Long apiCount;

    private String api;

    public Long getApiCount() {
        return apiCount;
    }

    public void setApiCount(Long apiCount) {
        this.apiCount = apiCount;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }
}
