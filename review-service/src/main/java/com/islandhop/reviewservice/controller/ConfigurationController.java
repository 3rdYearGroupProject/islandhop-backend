package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.dto.ConfidenceThresholdDTO;
import com.islandhop.reviewservice.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @GetMapping("/confidence-threshold")
    public ResponseEntity<ConfidenceThresholdDTO> getConfidenceThreshold() {
        double threshold = configurationService.getConfidenceThreshold();
        return ResponseEntity.ok(ConfidenceThresholdDTO.builder()
                .confidenceThreshold(threshold)
                .build());
    }

    @PutMapping("/confidence-threshold")
    public ResponseEntity<ConfidenceThresholdDTO> updateConfidenceThreshold(
            @Valid @RequestBody ConfidenceThresholdDTO thresholdDTO) {
        log.info("Updating confidence threshold to: {}", thresholdDTO.getConfidenceThreshold());
        
        configurationService.setConfidenceThreshold(thresholdDTO.getConfidenceThreshold());
        
        return ResponseEntity.ok(ConfidenceThresholdDTO.builder()
                .confidenceThreshold(configurationService.getConfidenceThreshold())
                .build());
    }
}
