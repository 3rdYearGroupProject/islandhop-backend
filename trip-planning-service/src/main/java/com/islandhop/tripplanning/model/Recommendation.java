package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    
    private String recommendationId;
    private String tripId;
    private PlannedPlace suggestedPlace;
    private Double score; // 0.0 to 1.0
    private List<String> reasons; // Why this was recommended
    private RecommendationType type;
    private Integer suggestedDay;
    private Integer suggestedOrder;
    
    // Scoring breakdown
    private Map<String, Double> scoringFactors;
    private Double userPreferenceMatch;
    private Double locationProximityScore;
    private Double timeConstraintScore;
    private Double popularityScore;
    private Double weatherSuitabilityScore;
    
    public enum RecommendationType {
        ATTRACTION, HOTEL, RESTAURANT, ALTERNATIVE_ROUTE, TIMING_OPTIMIZATION
    }
}
