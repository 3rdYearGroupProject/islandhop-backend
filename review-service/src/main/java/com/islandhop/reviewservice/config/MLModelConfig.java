package com.islandhop.reviewservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MLModelConfig {

    private double detectionRange;

    public MLModelConfig() {
        this.detectionRange = 0.5; // Default detection range
    }

    @Bean
    public double getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(double detectionRange) {
        this.detectionRange = detectionRange;
    }
}