package com.example.apiusageservice.controller;

import com.example.apiusageservice.dto.UsageMetricsDTO;
import com.example.apiusageservice.service.ApiUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiUsageController {

    private static final Logger logger = LoggerFactory.getLogger(ApiUsageController.class);

    @Autowired
    private ApiUsageService apiUsageService;

    @GetMapping("/usage/{apiName}")
    public ResponseEntity<UsageMetricsDTO> getApiUsage(@PathVariable String apiName) {
        logger.info("Fetching usage metrics for API: {}", apiName);
        UsageMetricsDTO usageMetrics = apiUsageService.getUsageMetrics(apiName);
        return ResponseEntity.ok(usageMetrics);
    }
}
