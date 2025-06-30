package com.islandhop.pooling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trip_pools")
public class TripPool {
    
    @Id
    private String poolId;
    
    private String poolName;
    private String description;
    private PoolType poolType;
    private PoolStatus status;
    
    // Timeline Information
    private LocalDate startDate;
    private LocalDate endDate;
    private String baseCity;
    private List<String> commonCities;
    
    // Pool Members
    private List<PoolMember> members;
    private String createdByUserId;
    private Integer maxMembers;
    private Integer currentMembers;
    
    // Shared Itinerary
    private List<SharedActivity> sharedActivities;
    private List<String> commonInterests;
    private Double routeSimilarityScore;
    
    // Pool Settings
    private boolean isPublic;
    private boolean allowJoinRequests;
    private String joinCode;
    
    // Statistics
    private Double averageCompatibilityScore;
    private Map<String, Object> poolStatistics;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum PoolType {
        TIMELINE_BASED,     // Based on overlapping trip dates
        ACTIVITY_BASED,     // Based on specific activities
        INTEREST_BASED,     // Based on travel preferences
        LOCATION_BASED      // Based on current location
    }
    
    public enum PoolStatus {
        FORMING,           // Pool is being formed
        ACTIVE,            // Pool is active with members
        COMPLETED,         // Trip completed
        CANCELLED,         // Pool cancelled
        ARCHIVED           // Old pool, kept for history
    }
}
