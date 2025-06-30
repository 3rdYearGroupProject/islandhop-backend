package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.model.PlannedPlace;
import com.islandhop.tripplanning.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Enhanced suggestions response with contextual recommendations and travel insights
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextualSuggestionsResponse {
    
    private Integer dayNumber;
    private String contextType; // "initial", "next_place", "search_results"
    
    // Categorized suggestions
    private List<PlaceSuggestion> accommodations;
    private List<PlaceSuggestion> attractions;
    private List<PlaceSuggestion> restaurants;
    private List<PlaceSuggestion> activities;
    
    // Context information
    private PlannedPlace lastAddedPlace; // The place they added most recently
    private TravelContext travelContext;
    
    // Insights and recommendations
    private List<String> insights;
    private List<String> warnings;
    private List<String> tips;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceSuggestion {
        private String placeId;
        private String name;
        private String description;
        private String city;
        private Double latitude;
        private Double longitude;
        private PlannedPlace.PlaceType placeType;
        private Double rating;
        private List<String> categories;
        private String imageUrl;
        
        // Contextual information
        private Double distanceFromLastPlaceKm;
        private Integer travelTimeFromLastPlaceMinutes;
        private String travelMode; // "driving", "walking", "public_transport"
        private Double relevanceScore; // 0-1, how relevant based on preferences
        private String reasonForSuggestion; // Why this place is suggested
        
        // Practical information
        private Integer estimatedVisitDurationMinutes;
        private String bestTimeToVisit;
        private String priceRange; // "budget", "moderate", "expensive"
        private Boolean requiresBooking;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelContext {
        private String currentProvince;
        private String currentCity;
        private Double currentLatitude;
        private Double currentLongitude;
        private Integer totalPlacesToday;
        private Integer remainingTimeToday; // Minutes available
        private String suggestedNextArea; // Next area to explore
    }
}
