package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
public class Trip {
    
    @Id
    private String tripId;
    
    private String userId;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime arrivalTime;
    private String baseCity;
    private boolean multiCity;
    private List<String> categories; // Nature, Culture, Adventure, Leisure
    private ActivityPacing pacing; // RELAXED, NORMAL, ACTIVE
    
    private List<PlannedPlace> places;
    private List<DayPlan> dayPlans;
    private TripStatistics statistics;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private TripStatus status; // PLANNING, ACTIVE, COMPLETED, CANCELLED
    
    // Recommendation metadata
    private Map<String, Object> preferences;
    private List<String> excludedAttractions;
    
    public enum ActivityPacing {
        RELAXED, NORMAL, ACTIVE
    }
    
    public enum TripStatus {
        PLANNING, ACTIVE, COMPLETED, CANCELLED
    }
}
