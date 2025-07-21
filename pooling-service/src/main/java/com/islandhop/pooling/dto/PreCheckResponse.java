package com.islandhop.pooling.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for pre-check compatible groups.
 * Contains suggestions for compatible public groups.
 */
@Data
public class PreCheckResponse {
    
    private String status;
    
    private String message;
    
    private List<CompatibleGroup> suggestions;
    
    private int totalSuggestions;
    
    private boolean hasCompatibleGroups;
    
    /**
     * Compatible group information for pre-check.
     */
    @Data
    public static class CompatibleGroup {
        private String groupId;
        private String tripName;
        private String groupName;
        private double compatibilityScore;
        private int currentMembers;
        private int maxMembers;
        private String baseCity;
        private String startDate;
        private String endDate;
        private String budgetLevel;
        private List<String> commonActivities;
        private List<String> commonTerrains;
        private String createdBy;
    }
}
