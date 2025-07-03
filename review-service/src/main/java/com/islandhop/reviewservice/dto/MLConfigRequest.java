package com.islandhop.reviewservice.dto;

import javax.validation.constraints.NotNull;

public class MLConfigRequest {

    @NotNull
    private Integer detectionRange;

    public MLConfigRequest() {
    }

    public MLConfigRequest(Integer detectionRange) {
        this.detectionRange = detectionRange;
    }

    public Integer getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(Integer detectionRange) {
        this.detectionRange = detectionRange;
    }
}