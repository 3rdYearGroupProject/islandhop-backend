package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for day-specific trip view with inline place management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayPlanResponse {
    
    private String tripId;
    private Integer dayNumber;
    private String dayDate; // e.g., "2025-07-15"
    private String dayName; // e.g., "Day 1", "Monday"
    
    // Current places for this day
    private List<DayPlace> places;
    
    // Quick suggestions for adding (without search)
    private List<QuickSuggestion> quickSuggestions;
    
    // Context for the day
    private DayContext context;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPlace {
        private String placeId;
        private String name;
        private String city;
        private PlannedPlace.PlaceType placeType;
        private Double latitude;
        private Double longitude;
        private String formattedAddress;
        private Integer visitDurationMinutes;
        private String timeSlot; // "morning", "afternoon", "evening"
        private Integer orderInDay;
        
        // Travel context from previous place
        private Integer travelTimeFromPrevious; // minutes
        private Double distanceFromPrevious; // km
        private String travelMode; // "driving", "walking"
        
        // Quick actions
        private boolean canEdit;
        private boolean canRemove;
        private boolean canReorder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickSuggestion {
        private String placeId;
        private String name;
        private String city;
        private PlannedPlace.PlaceType placeType;
        private String category; // "Accommodation", "Attraction", "Restaurant", "Activity"
        private Double rating;
        private String reasonForSuggestion; // "Near your last selection", "Based on preferences"
        private Integer travelTimeMinutes;
        private Double distanceKm;
        private String quickAddUrl; // Direct API endpoint to add this place
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayContext {
        private Integer totalPlaces;
        private Integer totalDurationMinutes;
        private Integer remainingTimeMinutes;
        private String currentArea; // "Colombo", "Kandy", etc.
        private String suggestedNextCategory; // What type of place to add next
        private List<String> dayInsights;
        private List<String> dayWarnings;
    }
}
