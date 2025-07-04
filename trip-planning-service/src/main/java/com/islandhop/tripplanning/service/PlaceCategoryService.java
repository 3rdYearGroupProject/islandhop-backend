package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing place categories based on Google Places categories
 * Simplified to essential travel categories
 */
@Service
@Slf4j
public class PlaceCategoryService {
    
    // Main categories we support (simplified from Google Places)
    public enum MainCategory {
        ACCOMMODATION("Accommodation", "🏨", Arrays.asList("lodging", "hotel", "motel", "resort", "guesthouse")),
        RESTAURANT("Restaurant", "🍽️", Arrays.asList("restaurant", "food", "meal_takeaway", "cafe", "bar")),
        ATTRACTION("Attraction", "🎯", Arrays.asList("tourist_attraction", "museum", "amusement_park", "zoo", "aquarium")),
        ACTIVITY("Activity", "🏃", Arrays.asList("gym", "spa", "bowling_alley", "casino", "night_club")),
        SHOPPING("Shopping", "🛍️", Arrays.asList("shopping_mall", "store", "supermarket", "market")),
        TRANSPORT("Transport", "🚌", Arrays.asList("transit_station", "airport", "bus_station", "taxi_stand")),
        NATURE("Nature", "🌿", Arrays.asList("park", "natural_feature", "campground", "hiking_area")),
        CULTURE("Culture", "🏛️", Arrays.asList("place_of_worship", "library", "art_gallery", "historic_site"));
        
        private final String displayName;
        private final String emoji;
        private final List<String> googleTypes;
        
        MainCategory(String displayName, String emoji, List<String> googleTypes) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.googleTypes = googleTypes;
        }
        
        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public List<String> getGoogleTypes() { return googleTypes; }
    }
    
    /**
     * Convert Google Places types to our simplified categories
     */
    public MainCategory categorizeFromGoogleTypes(List<String> googleTypes) {
        if (googleTypes == null || googleTypes.isEmpty()) {
            return MainCategory.ATTRACTION; // Default
        }
        
        // Priority order - more specific types checked first
        for (String type : googleTypes) {
            String lowerType = type.toLowerCase();
            
            // Accommodation
            if (MainCategory.ACCOMMODATION.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.ACCOMMODATION;
            }
            
            // Restaurant/Food
            if (MainCategory.RESTAURANT.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.RESTAURANT;
            }
            
            // Transport
            if (MainCategory.TRANSPORT.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.TRANSPORT;
            }
            
            // Culture
            if (MainCategory.CULTURE.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.CULTURE;
            }
            
            // Nature
            if (MainCategory.NATURE.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.NATURE;
            }
            
            // Activity
            if (MainCategory.ACTIVITY.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.ACTIVITY;
            }
            
            // Shopping
            if (MainCategory.SHOPPING.getGoogleTypes().stream()
                    .anyMatch(t -> lowerType.contains(t))) {
                return MainCategory.SHOPPING;
            }
        }
        
        // Default to attraction if no specific match
        return MainCategory.ATTRACTION;
    }
    
    /**
     * Convert to PlannedPlace.PlaceType
     */
    public PlannedPlace.PlaceType toPlannedPlaceType(MainCategory category) {
        switch (category) {
            case ACCOMMODATION:
                return PlannedPlace.PlaceType.HOTEL;
            case RESTAURANT:
                return PlannedPlace.PlaceType.RESTAURANT;
            case SHOPPING:
                return PlannedPlace.PlaceType.SHOPPING;
            case NATURE:
                return PlannedPlace.PlaceType.VIEWPOINT;
            case TRANSPORT:
                return PlannedPlace.PlaceType.TRANSPORT_HUB;
            case ATTRACTION:
            case ACTIVITY:
            case CULTURE:
            default:
                return PlannedPlace.PlaceType.ATTRACTION;
        }
    }
    
    /**
     * Get all available categories for frontend
     */
    public List<CategoryInfo> getAllCategories() {
        List<CategoryInfo> categories = new ArrayList<>();
        
        for (MainCategory category : MainCategory.values()) {
            categories.add(new CategoryInfo(
                category.name(),
                category.getDisplayName(),
                category.getEmoji(),
                toPlannedPlaceType(category).toString()
            ));
        }
        
        return categories;
    }
    
    /**
     * Get category suggestions based on trip preferences
     */
    public List<MainCategory> suggestCategoriesForPreferences(List<String> tripPreferences) {
        List<MainCategory> suggested = new ArrayList<>();
        
        if (tripPreferences == null || tripPreferences.isEmpty()) {
            return Arrays.asList(MainCategory.ATTRACTION, MainCategory.RESTAURANT, MainCategory.ACCOMMODATION);
        }
        
        for (String preference : tripPreferences) {
            String lowerPref = preference.toLowerCase();
            
            if (lowerPref.contains("adventure") || lowerPref.contains("nature")) {
                suggested.add(MainCategory.NATURE);
                suggested.add(MainCategory.ACTIVITY);
            }
            if (lowerPref.contains("culture") || lowerPref.contains("heritage")) {
                suggested.add(MainCategory.CULTURE);
                suggested.add(MainCategory.ATTRACTION);
            }
            if (lowerPref.contains("leisure") || lowerPref.contains("relax")) {
                suggested.add(MainCategory.RESTAURANT);
                suggested.add(MainCategory.SHOPPING);
            }
        }
        
        // Always include essentials
        if (!suggested.contains(MainCategory.ACCOMMODATION)) {
            suggested.add(MainCategory.ACCOMMODATION);
        }
        if (!suggested.contains(MainCategory.RESTAURANT)) {
            suggested.add(MainCategory.RESTAURANT);
        }
        
        return suggested.stream().distinct().collect(ArrayList::new, (list, item) -> {
            if (!list.contains(item)) list.add(item);
        }, ArrayList::addAll);
    }
    
    // Helper class for API responses
    public static class CategoryInfo {
        private String key;
        private String displayName;
        private String emoji;
        private String placeType;
        
        public CategoryInfo(String key, String displayName, String emoji, String placeType) {
            this.key = key;
            this.displayName = displayName;
            this.emoji = emoji;
            this.placeType = placeType;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public String getPlaceType() { return placeType; }
    }
}
