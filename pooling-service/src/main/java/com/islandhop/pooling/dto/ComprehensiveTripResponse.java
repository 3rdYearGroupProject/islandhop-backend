package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive response DTO that includes trip itinerary and joined group members.
 * This is a public endpoint accessible to both logged-in and anonymous users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveTripResponse {
    
    // Trip Information
    private TripDetails tripDetails;
    
    // Group/Pooling Information
    private GroupInfo groupInfo;
    
    // Member Information
    private List<MemberSummary> members;
    
    // Response metadata
    private String status;
    private String message;
    private Instant fetchedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripDetails {
        private String tripId;
        private String tripName;
        private String startDate;
        private String endDate;
        private String baseCity;
        private String budgetLevel;
        private String activityPacing;
        private List<String> preferredActivities;
        private List<String> preferredTerrains;
        private Boolean multiCityAllowed;
        private List<DailyPlanSummary> dailyPlans;
        private Instant createdAt;
        private Instant lastUpdated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPlanSummary {
        private int day;
        private String city;
        private Boolean userSelected;
        private int attractionsCount;
        private int hotelsCount;
        private int restaurantsCount;
        private List<PlaceSummary> attractions;
        private List<PlaceSummary> hotels;
        private List<PlaceSummary> restaurants;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceSummary {
        private String name;
        private String category;
        private Double rating;
        private String address;
        private Boolean userSelected;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        private String groupId;
        private String groupName;
        private String visibility; // "public" or "private"
        private String status; // "draft", "active", "finalized"
        private String groupLeader;
        private int currentMembers;
        private int maxMembers;
        private int availableSlots;
        private Boolean requiresApproval;
        private Instant createdAt;
        private Instant lastUpdated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummary {
        private String userId;
        private String name; // Will be fetched from user service or defaulted  
        private String email; // Will be fetched from user service or defaulted
        private String role; // "leader" or "member"
        private Instant joinedAt;
        private String status; // "active", "pending", "left"
        private TravelPreferences preferences;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelPreferences {
        private String budgetLevel;
        private List<String> preferredActivities;
        private List<String> preferredTerrains;
        private String activityPacing;
    }
}
