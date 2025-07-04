package com.islandhop.tripplanning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_preferences")
public class UserPreferenceProfile {
    
    @Id
    private String userId;
    
    // Historical preferences
    private Map<String, Integer> categoryPreferences; // Category -> frequency
    private Map<String, Double> locationPreferences; // City/Region -> preference score
    private Map<String, Integer> visitedAttractions; // AttractionId -> visit count
    
    // Behavioral patterns
    private Double averageDailyBudget;
    private Integer averageAttractionsPerDay;
    private List<String> preferredTravelModes;
    private String preferredPacing; // RELAXED, NORMAL, ACTIVE
    
    // Collaborative filtering data
    private List<String> similarUsers; // Users with similar preferences
    private Map<String, Double> attractionRatings; // User's implicit ratings
    
    private LocalDateTime lastUpdated;
}
