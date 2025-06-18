package com.islandhop.tourplanning.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "trips")
public class Trip {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Place> places;
    private TripStatus status;
    private boolean isPublic;
    private List<String> sharedWithUserIds;

    private TripPreferences preferences;
    private List<DailyPlan> dailyPlans;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class TripPreferences {
        private List<String> attractionTypes; // BEACH, JUNGLE, CULTURAL, ADVENTURE, etc.
        private List<String> foodPreferences; // CUISINE_TYPES
        private List<String> dietaryRestrictions;
        private BudgetRange budgetRange;
    }

    @Data
    public static class BudgetRange {
        private Range accommodation;
        private Range food;
        private Range activities;
    }

    @Data
    public static class Range {
        private Double min;
        private Double max;
    }

    @Data
    public static class DailyPlan {
        private LocalDateTime date;
        private List<Activity> activities;
    }

    @Data
    public static class Activity {
        private String id;
        private ActivityType type; // ATTRACTION, RESTAURANT, HOTEL
        private String placeId; // Google Places ID
        private String tripAdvisorId;
        private String name;
        private Location location;
        private Integer estimatedDuration; // in minutes
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer travelTime; // in minutes
        private Double cost;
        private ActivityStatus status;
    }

    @Data
    public static class Location {
        private Double latitude;
        private Double longitude;
    }

    public enum TripStatus {
        PLANNING, ACTIVE, COMPLETED, CANCELLED
    }

    public enum ActivityType {
        ATTRACTION, RESTAURANT, HOTEL
    }

    public enum ActivityStatus {
        PLANNED, COMPLETED, CANCELLED
    }
} 