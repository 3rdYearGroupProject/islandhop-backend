package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for public group listing with enhanced filtering support.
 * Shows groups available for collaboration with detailed preference information.
 */
@Data
public class PublicGroupResponse {
    
    private String groupId; // Internal group ID
    
    private String tripId; // The trip available for collaboration
    
    private String tripName; // Trip display name
    
    private String groupName; // Group name
    
    private Map<String, Object> preferences;
    
    private int collaboratorCount; // Number of people collaborating
    
    private int maxMembers; // Maximum members allowed
    
    private Instant createdAt;
    
    // Enhanced fields for filtering
    private String baseCity;
    
    private String startDate;
    
    private String endDate;
    
    private String budgetLevel;
    
    private List<String> preferredActivities;
    
    private List<String> preferredTerrains;
    
    private String activityPacing;
    
    private String status; // draft, finalized
    
    private Double compatibilityScore; // Optional, set when filtering with user preferences
}
