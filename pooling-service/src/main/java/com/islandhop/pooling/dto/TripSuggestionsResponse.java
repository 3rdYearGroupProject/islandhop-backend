package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for trip suggestions with compatibility scores.
 */
@Data
public class TripSuggestionsResponse {
    
    private String status;
    private String groupId;
    private String tripId;
    private List<CompatibleGroup> suggestions;
    private String message;
    
    @Data
    public static class CompatibleGroup {
        private String groupId;
        private String groupName;
        private String tripId;
        private String tripName;
        private double compatibilityScore;
        private int currentMembers;
        private int maxMembers;
        private List<String> commonDestinations;
        private List<String> commonPreferences;
        private String createdBy;
        private String startDate;
        private String endDate;
        private String baseCity;
    }
}
