package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripStatistics {
    
    private Integer totalDays;
    private Integer totalPlaces;
    private Double totalDistanceKm;
    private Integer totalTravelTimeMinutes;
    private Integer totalVisitTimeMinutes;
    private String predominantCategory;
    private Double averageDailyBudget;
    private Integer citiesVisited;
    
    // Performance metrics for recommendations
    private Double userSatisfactionScore; // Based on confirmed vs suggested
    private Integer totalSuggestions;
    private Integer acceptedSuggestions;
}
