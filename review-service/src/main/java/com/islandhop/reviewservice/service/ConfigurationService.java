package com.islandhop.reviewservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConfigurationService {

    @Value("${review.ai.confidence.threshold:0.9}")
    private double defaultConfidenceThreshold;

    private double currentConfidenceThreshold;

    public ConfigurationService() {
        this.currentConfidenceThreshold = defaultConfidenceThreshold;
    }

    public double getConfidenceThreshold() {
        return currentConfidenceThreshold;
    }

    public void setConfidenceThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Confidence threshold must be between 0.0 and 1.0");
        }
        log.info("Updating confidence threshold from {} to {}", currentConfidenceThreshold, threshold);
        this.currentConfidenceThreshold = threshold;
    }
}
