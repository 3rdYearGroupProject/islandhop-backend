package com.islandhop.pooling.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SaveTripWithSuggestionsResponse {
    private String tripId;
    private String groupId;
    private String message;
    private boolean hasSimilarTrips;
    private int totalSuggestions;
    private List<SimilarTrip> similarTrips;
    
    @Data
    public static class SimilarTrip {
        private String groupId;
        private String tripName;
        private String groupName;
        private double similarityScore;
        private int currentMembers;
        private int maxMembers;
        private String createdBy;
        private String startDate;
        private String endDate;
        private List<Map<String, String>> destinations;
        private List<String> activities;
        private List<String> terrains;
    }
}