package com.islandhop.tourplanning.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "user_preferences")
public class UserPreferences {
    @Id
    private String id;

    @Indexed
    private String userId;

    private List<String> preferredAttractionTypes;
    private List<String> preferredCuisineTypes;
    private List<String> dietaryRestrictions;
    private List<String> preferredAccommodationTypes;
    private List<String> preferredActivities;
    private List<String> preferredTransportationModes;

    private BudgetPreferences budgetPreferences;
    private TimePreferences timePreferences;
    private AccessibilityPreferences accessibilityPreferences;
    private Map<String, Object> customPreferences;

    @Data
    public static class BudgetPreferences {
        private Double dailyBudget;
        private Double accommodationBudget;
        private Double foodBudget;
        private Double activitiesBudget;
        private Double transportationBudget;
        private PriceLevel preferredPriceLevel;
    }

    @Data
    public static class TimePreferences {
        private Integer preferredStartTime; // Hour of day (0-23)
        private Integer preferredEndTime; // Hour of day (0-23)
        private Integer maxDailyActivities;
        private Integer minRestTimeBetweenActivities; // in minutes
        private Boolean preferMorningActivities;
        private Boolean preferEveningActivities;
    }

    @Data
    public static class AccessibilityPreferences {
        private Boolean wheelchairAccessible;
        private Boolean familyFriendly;
        private Boolean seniorFriendly;
        private List<String> accessibilityFeatures;
    }

    public enum PriceLevel {
        BUDGET,
        MODERATE,
        LUXURY
    }
} 