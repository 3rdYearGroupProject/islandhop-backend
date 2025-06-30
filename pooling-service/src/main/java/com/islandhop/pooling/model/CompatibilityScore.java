package com.islandhop.pooling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityScore {
    
    private String userId1;
    private String userId2;
    private Double overallScore; // 0.0 to 1.0
    
    // Individual scoring components
    private Double timelineCompatibility;    // How well trip dates overlap
    private Double routeCompatibility;       // How similar the routes are
    private Double interestCompatibility;    // How similar preferences are
    private Double demographicCompatibility; // Age, nationality, languages
    private Double pacingCompatibility;      // Travel pace compatibility
    
    // Detailed breakdown
    private Map<String, Double> detailedScores;
    private List<String> compatibilityReasons;
    private List<String> incompatibilityWarnings;
    
    // Weights used in calculation
    private Map<String, Double> scoreWeights;
}
