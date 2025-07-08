package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayPlan {
    
    private Integer dayNumber;
    private LocalDate date;
    private String baseCity;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Per-day categorized places
    private List<PlannedActivity> activities;   // Keep original for compatibility
    private PlannedPlace accommodation;         // Accommodation for this day  
    private List<PlannedPlace> dining;          // Restaurants/dining for this day
    
    private List<TravelSegment> travelSegments;
    
    // Day statistics
    private Integer totalTravelTimeMinutes;
    private Integer totalVisitTimeMinutes;
    private Integer totalActivities;
    private String pacingAssessment; // "Relaxed", "Perfect", "Busy", "Overpacked"
    
    // Recommendations for this day
    private List<PlannedPlace> suggestedHotels;
    private List<PlannedPlace> suggestedRestaurants;
    private List<String> dayTips;
    private List<String> warnings; // Weather, crowds, timing issues
}
