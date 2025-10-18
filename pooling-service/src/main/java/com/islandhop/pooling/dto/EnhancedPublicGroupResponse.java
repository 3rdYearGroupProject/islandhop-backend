package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Enhanced response DTO for public group listing with detailed trip information.
 * Provides comprehensive details including creator info, cities, and trip highlights.
 */
@Data
public class EnhancedPublicGroupResponse {
    
    private String groupId;
    private String tripId;
    private String tripName;
    private String groupName;
    
    // Creator information
    private String creatorUserId;
    private String creatorName; // "John Doe"
    
    // Member information  
    private List<ComprehensiveTripResponse.MemberSummary> members;
    
    // Trip details
    private String baseCity;
    private List<String> cities; // ["Kandy", "Nuwara Eliya", "Ella"]
    private String startDate; // "2025-08-15"
    private String endDate; // "2025-08-17" 
    private String formattedDateRange; // "Aug 15-17, 2025"
    private int tripDurationDays; // 3
    
    // Group details
    private String visibility; // "public" or "private"
    private int memberCount; // 3
    private int maxMembers; // 5
    private String memberCountText; // "3 participants / 5"
    
    // Trip highlights/activities
    private List<String> topAttractions; // ["Tea Plantations", "Nine Arch Bridge", "Little Adams Peak"]
    
    // Additional preferences
    private String budgetLevel;
    private String activityPacing;
    private List<String> preferredActivities;
    private List<String> preferredTerrains;
    
    private String status;
    private Instant createdAt;
    private Double compatibilityScore;
}
