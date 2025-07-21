package com.example.apiusageservice.dto;

public class UsageMetricsDTO {

    private String apiName;
    private int requestsToday;
    private int quotaUsed;
    private int quotaLimit;
    private int errorCount;
    private int averageResponseLatency;

    // Getters and Setters

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public int getRequestsToday() {
        return requestsToday;
    }

    public void setRequestsToday(int requestsToday) {
        this.requestsToday = requestsToday;
    }

    public int getQuotaUsed() {
        return quotaUsed;
    }

    public void setQuotaUsed(int quotaUsed) {
        this.quotaUsed = quotaUsed;
    }

    public int getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(int quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getAverageResponseLatency() {
        return averageResponseLatency;
    }

    public void setAverageResponseLatency(int averageResponseLatency) {
        this.averageResponseLatency = averageResponseLatency;
    }
}
