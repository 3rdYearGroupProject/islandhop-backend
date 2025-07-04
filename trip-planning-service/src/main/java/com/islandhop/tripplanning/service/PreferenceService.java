package com.islandhop.tripplanning.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PreferenceService {
    
    // Backend preference categories based on your requirements
    public static final Map<String, String> BACKEND_PREFERENCES = Map.of(
        "BEACH", "Beach & Coastal Areas",
        "NATURE", "Nature & Wildlife",
        "CULTURE", "Culture & Heritage", 
        "ADVENTURE", "Adventure & Sports",
        "FOOD", "Food & Dining",
        "WELLNESS", "Wellness & Relaxation",
        "NIGHTLIFE", "Nightlife & Entertainment",
        "SHOPPING", "Shopping & Markets",
        "URBAN", "Urban & City Life",
        "SPIRITUAL", "Spiritual & Religious"
    );
    
    /**
     * Map frontend terrain preferences to backend categories
     */
    public List<String> mapTerrainPreferences(List<String> terrainPreferences) {
        List<String> categories = new ArrayList<>();
        
        if (terrainPreferences.contains("beaches")) categories.add("BEACH");
        if (terrainPreferences.contains("mountains")) categories.add("NATURE");
        if (terrainPreferences.contains("forests")) categories.add("NATURE");
        if (terrainPreferences.contains("historical")) categories.add("CULTURE");
        if (terrainPreferences.contains("city")) categories.add("URBAN");
        if (terrainPreferences.contains("parks")) categories.add("NATURE");
        if (terrainPreferences.contains("islands")) categories.add("BEACH");
        if (terrainPreferences.contains("wetland")) categories.add("NATURE");
        if (terrainPreferences.contains("countryside")) categories.add("NATURE");
        
        return categories.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Map frontend activity preferences to backend categories
     */
    public List<String> mapActivityPreferences(List<String> activityPreferences) {
        List<String> categories = new ArrayList<>();
        
        if (activityPreferences.contains("surfing")) categories.add("ADVENTURE");
        if (activityPreferences.contains("hiking")) categories.add("ADVENTURE");
        if (activityPreferences.contains("photography")) categories.add("NATURE");
        if (activityPreferences.contains("sightseeing")) categories.add("CULTURE");
        if (activityPreferences.contains("dining")) categories.add("FOOD");
        if (activityPreferences.contains("nightlife")) categories.add("NIGHTLIFE");
        if (activityPreferences.contains("snorkeling")) categories.add("ADVENTURE");
        if (activityPreferences.contains("adventure")) categories.add("ADVENTURE");
        if (activityPreferences.contains("culture")) categories.add("CULTURE");
        if (activityPreferences.contains("wildlife")) categories.add("NATURE");
        if (activityPreferences.contains("wellness")) categories.add("WELLNESS");
        if (activityPreferences.contains("shopping")) categories.add("SHOPPING");
        
        return categories.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Combine and deduplicate all preference categories
     */
    public List<String> combinePreferences(List<String> terrainPreferences, List<String> activityPreferences) {
        List<String> allCategories = new ArrayList<>();
        allCategories.addAll(mapTerrainPreferences(terrainPreferences));
        allCategories.addAll(mapActivityPreferences(activityPreferences));
        
        return allCategories.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Get preference weight based on category priority
     */
    public double getPreferenceWeight(String category) {
        return switch (category) {
            case "BEACH", "NATURE", "CULTURE" -> 1.0;  // High priority
            case "ADVENTURE", "FOOD", "URBAN" -> 0.8;  // Medium-high priority
            case "WELLNESS", "NIGHTLIFE", "SHOPPING" -> 0.6;  // Medium priority
            case "SPIRITUAL" -> 0.4;  // Lower priority
            default -> 0.5;
        };
    }
}
