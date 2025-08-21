package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.TripSuggestionsResponse;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating trip compatibility between groups.
 * Implements scoring algorithms for matching similar trips.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripCompatibilityService {
    
    private final GroupRepository groupRepository;
    
    @Value("${pooling.compatibility.min-score:0.2}")
    private double minCompatibilityScore;
    
    @Value("${pooling.compatibility.weights.destinations:0.4}")
    private double destinationWeight;
    
    @Value("${pooling.compatibility.weights.preferences:0.3}")
    private double preferenceWeight;
    
    @Value("${pooling.compatibility.weights.dates:0.2}")
    private double dateWeight;
    
    @Value("${pooling.compatibility.weights.budget:0.1}")
    private double budgetWeight;
    
    /**
     * Finds compatible groups for a given trip.
     * 
     * @param sourceGroup The group to find matches for
     * @param tripData The trip data from the planning service
     * @return List of compatible groups with scores
     */
    public List<TripSuggestionsResponse.CompatibleGroup> findCompatibleGroups(Group sourceGroup, Map<String, Object> tripData) {
        log.info("Finding compatible groups for group {} with trip {}", sourceGroup.getId(), sourceGroup.getTripId());
        
        // Get all public finalized groups except the source group
        List<Group> candidateGroups = groupRepository.findByVisibilityAndStatusAndIdNot("public", "finalized", sourceGroup.getId());
        
        List<TripSuggestionsResponse.CompatibleGroup> compatibleGroups = new ArrayList<>();
        
        for (Group candidate : candidateGroups) {
            if (candidate.isFull()) {
                continue; // Skip full groups
            }
            
            try {
                double compatibilityScore = calculateCompatibilityScore(sourceGroup, candidate, tripData);
                
                if (compatibilityScore >= minCompatibilityScore) {
                    TripSuggestionsResponse.CompatibleGroup compatibleGroup = createCompatibleGroupResponse(candidate, compatibilityScore, tripData);
                    compatibleGroups.add(compatibleGroup);
                }
            } catch (Exception e) {
                log.warn("Error calculating compatibility for group {}: {}", candidate.getId(), e.getMessage());
            }
        }
        
        // Sort by compatibility score in descending order
        compatibleGroups.sort((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
        
        log.info("Found {} compatible groups for group {}", compatibleGroups.size(), sourceGroup.getId());
        return compatibleGroups;
    }
    
    /**
     * Calculates compatibility score between two preference maps.
     * Used for filtering and scoring without full Group objects.
     * 
     * @param userPreferences The user's preferences
     * @param groupPreferences The group's preferences  
     * @return Compatibility score between 0 and 1
     */
    public double calculateCompatibilityScore(Map<String, Object> userPreferences, Map<String, Object> groupPreferences) {
        double totalScore = 0.0;
        
        // 1. Preference similarity (activities, terrains, pacing)
        double preferenceScore = calculatePreferenceSimilarity(userPreferences, groupPreferences);
        totalScore += preferenceScore * preferenceWeight;
        
        // 2. Date proximity  
        double dateScore = calculateDateScore(userPreferences, groupPreferences);
        totalScore += dateScore * dateWeight;
        
        // 3. Budget compatibility
        double budgetScore = calculateBudgetSimilarity(userPreferences, groupPreferences);
        totalScore += budgetScore * budgetWeight;
        
        // 4. Base city compatibility
        double baseCityScore = calculateBaseCityScore(userPreferences, groupPreferences);
        totalScore += baseCityScore * destinationWeight;
        
        log.debug("Compatibility score calculation: preference={}, date={}, budget={}, baseCity={}, total={}", 
                 preferenceScore, dateScore, budgetScore, baseCityScore, totalScore);
        
        return Math.min(1.0, totalScore); // Cap at 1.0
    }
    
    /**
     * Calculates compatibility score between two groups.
     * 
     * @param sourceGroup The source group
     * @param candidateGroup The candidate group
     * @param sourceTripData The source trip data
     * @return Compatibility score between 0 and 1
     */
    private double calculateCompatibilityScore(Group sourceGroup, Group candidateGroup, Map<String, Object> sourceTripData) {
        double totalScore = 0.0;
        
        // 1. Destination similarity (based on daily plans)
        double destinationScore = calculateDestinationSimilarity(sourceTripData, candidateGroup);
        totalScore += destinationScore * destinationWeight;
        
        // 2. Preference similarity
        double preferenceScore = calculatePreferenceSimilarity(sourceGroup.getPreferences(), candidateGroup.getPreferences());
        totalScore += preferenceScore * preferenceWeight;
        
        // 3. Date proximity
        double dateScore = calculateDateSimilarity(sourceTripData, candidateGroup);
        totalScore += dateScore * dateWeight;
        
        // 4. Budget compatibility
        double budgetScore = calculateBudgetSimilarity(sourceGroup.getPreferences(), candidateGroup.getPreferences());
        totalScore += budgetScore * budgetWeight;
        
        log.debug("Compatibility score for groups {} and {}: destination={}, preference={}, date={}, budget={}, total={}", 
                 sourceGroup.getId(), candidateGroup.getId(), destinationScore, preferenceScore, dateScore, budgetScore, totalScore);
        
        return Math.min(1.0, totalScore); // Cap at 1.0
    }
    
    /**
     * Calculates destination similarity based on cities and places.
     */
    private double calculateDestinationSimilarity(Map<String, Object> sourceTripData, Group candidateGroup) {
        try {
            // Extract cities from source trip data
            Set<String> sourceCities = extractCitiesFromTripData(sourceTripData);
            
            // Get candidate group's trip data (this would need to be fetched from trip service)
            // For now, we'll use a simplified approach based on baseCity and preferences
            Set<String> candidateCities = extractCitiesFromGroupPreferences(candidateGroup);
            
            if (sourceCities.isEmpty() && candidateCities.isEmpty()) {
                return 0.5; // Neutral score if no city data
            }
            
            // Calculate Jaccard similarity
            Set<String> intersection = new HashSet<>(sourceCities);
            intersection.retainAll(candidateCities);
            
            Set<String> union = new HashSet<>(sourceCities);
            union.addAll(candidateCities);
            
            return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        } catch (Exception e) {
            log.warn("Error calculating destination similarity: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calculates preference similarity using cosine similarity.
     */
    private double calculatePreferenceSimilarity(Map<String, Object> sourcePrefs, Map<String, Object> candidatePrefs) {
        if (sourcePrefs == null || candidatePrefs == null) {
            return 0.0;
        }
        
        try {
            // Extract terrain and activity preferences
            Set<String> sourceTerrains = extractStringList(sourcePrefs, "preferredTerrains");
            Set<String> candidateTerrains = extractStringList(candidatePrefs, "preferredTerrains");
            
            Set<String> sourceActivities = extractStringList(sourcePrefs, "preferredActivities");
            Set<String> candidateActivities = extractStringList(candidatePrefs, "preferredActivities");
            
            double terrainSimilarity = calculateJaccardSimilarity(sourceTerrains, candidateTerrains);
            double activitySimilarity = calculateJaccardSimilarity(sourceActivities, candidateActivities);
            
            return (terrainSimilarity + activitySimilarity) / 2.0;
        } catch (Exception e) {
            log.warn("Error calculating preference similarity: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calculates date similarity based on trip dates.
     */
    private double calculateDateSimilarity(Map<String, Object> sourceTripData, Group candidateGroup) {
        try {
            String sourceStartDate = (String) sourceTripData.get("startDate");
            String sourceEndDate = (String) sourceTripData.get("endDate");
            
            // Get candidate dates from preferences (this would ideally come from trip service)
            Map<String, Object> candidatePrefs = candidateGroup.getPreferences();
            if (candidatePrefs == null) {
                return 0.0;
            }
            
            String candidateStartDate = (String) candidatePrefs.get("startDate");
            String candidateEndDate = (String) candidatePrefs.get("endDate");
            
            if (sourceStartDate == null || sourceEndDate == null || candidateStartDate == null || candidateEndDate == null) {
                return 0.0;
            }
            
            // Calculate date overlap (simplified approach)
            // In a real implementation, you'd parse dates and calculate actual overlap
            return sourceStartDate.equals(candidateStartDate) && sourceEndDate.equals(candidateEndDate) ? 1.0 : 0.0;
        } catch (Exception e) {
            log.warn("Error calculating date similarity: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calculates budget similarity.
     */
    private double calculateBudgetSimilarity(Map<String, Object> sourcePrefs, Map<String, Object> candidatePrefs) {
        try {
            String sourceBudget = (String) sourcePrefs.get("budgetLevel");
            String candidateBudget = (String) candidatePrefs.get("budgetLevel");
            
            if (sourceBudget == null || candidateBudget == null) {
                return 0.0;
            }
            
            return sourceBudget.equals(candidateBudget) ? 1.0 : 0.0;
        } catch (Exception e) {
            log.warn("Error calculating budget similarity: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Helper method to calculate Jaccard similarity between two sets.
     */
    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Extracts cities from trip data.
     */
    private Set<String> extractCitiesFromTripData(Map<String, Object> tripData) {
        Set<String> cities = new HashSet<>();
        
        // Add base city
        String baseCity = (String) tripData.get("baseCity");
        if (baseCity != null) {
            cities.add(baseCity);
        }
        
        // Extract cities from daily plans
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyPlans = (List<Map<String, Object>>) tripData.get("dailyPlans");
        if (dailyPlans != null) {
            for (Map<String, Object> plan : dailyPlans) {
                String city = (String) plan.get("city");
                if (city != null) {
                    cities.add(city);
                }
            }
        }
        
        return cities;
    }
    
    /**
     * Extracts cities from group preferences.
     */
    private Set<String> extractCitiesFromGroupPreferences(Group group) {
        Set<String> cities = new HashSet<>();
        
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            String baseCity = (String) preferences.get("baseCity");
            if (baseCity != null) {
                cities.add(baseCity);
            }
        }
        
        return cities;
    }
    
    /**
     * Extracts string list from preferences map.
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractStringList(Map<String, Object> preferences, String key) {
        Object value = preferences.get(key);
        if (value instanceof List) {
            return new HashSet<>((List<String>) value);
        }
        return new HashSet<>();
    }
    
    /**
     * Creates a CompatibleGroup response object.
     */
    private TripSuggestionsResponse.CompatibleGroup createCompatibleGroupResponse(Group group, double score, Map<String, Object> tripData) {
        TripSuggestionsResponse.CompatibleGroup compatibleGroup = new TripSuggestionsResponse.CompatibleGroup();
        compatibleGroup.setGroupId(group.getId());
        compatibleGroup.setGroupName(group.getGroupName());
        compatibleGroup.setTripId(group.getTripId());
        compatibleGroup.setCompatibilityScore(score);
        compatibleGroup.setCurrentMembers(group.getUserIds().size());
        compatibleGroup.setMaxMembers(group.getMaxMembers());
        compatibleGroup.setCreatedBy(group.getCreatorUserId());
        
        // Extract trip information from preferences
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            compatibleGroup.setTripName((String) preferences.get("tripName"));
            compatibleGroup.setStartDate((String) preferences.get("startDate"));
            compatibleGroup.setEndDate((String) preferences.get("endDate"));
            compatibleGroup.setBaseCity((String) preferences.get("baseCity"));
        }
        
        // Set common destinations and preferences (simplified)
        compatibleGroup.setCommonDestinations(new ArrayList<>());
        compatibleGroup.setCommonPreferences(new ArrayList<>());
        
        return compatibleGroup;
    }
    
    /**
     * Calculates compatibility score for pre-check with limited user data.
     * Uses preference-based scoring when full trip data is not available.
     */
    public double calculatePreCheckCompatibilityScore(Map<String, Object> userPreferences, Group group) {
        log.debug("Calculating pre-check compatibility score for group {}", group.getId());
        
        double totalScore = 0.0;
        Map<String, Object> groupPreferences = group.getPreferences();
        
        if (groupPreferences == null) {
            log.debug("Group {} has no preferences, returning low score", group.getId());
            return 0.1;
        }
        
        // Base city matching (weight: 0.5)
        double baseCityScore = calculateBaseCityScore(userPreferences, groupPreferences);
        totalScore += baseCityScore * getBaseCityWeight();
        
        // Budget level matching (weight: 0.2)
        double budgetScore = calculateBudgetScore(userPreferences, groupPreferences);
        totalScore += budgetScore * getBudgetWeight();
        
        // Date matching (weight: 0.1)
        double dateScore = calculateDateScore(userPreferences, groupPreferences);
        totalScore += dateScore * getDateWeight();
        
        // Activity preferences matching (weight: 0.1)
        double activityScore = calculateActivityScore(userPreferences, groupPreferences);
        totalScore += activityScore * getActivityWeight();
        
        // Terrain preferences matching (weight: 0.1)
        double terrainScore = calculateTerrainScore(userPreferences, groupPreferences);
        totalScore += terrainScore * getTerrainWeight();
        
        log.debug("Pre-check compatibility score breakdown for group {}: baseCity={}, budget={}, dates={}, activities={}, terrains={}, total={}", 
                group.getId(), baseCityScore, budgetScore, dateScore, activityScore, terrainScore, totalScore);
        
        return Math.min(1.0, totalScore); // Cap at 1.0
    }
    
    private double calculateBaseCityScore(Map<String, Object> userPrefs, Map<String, Object> groupPrefs) {
        String userCity = (String) userPrefs.get("baseCity");
        String groupCity = (String) groupPrefs.get("baseCity");
        
        if (userCity == null || groupCity == null) {
            return 0.0;
        }
        
        return userCity.equalsIgnoreCase(groupCity) ? 1.0 : 0.0;
    }
    
    private double calculateBudgetScore(Map<String, Object> userPrefs, Map<String, Object> groupPrefs) {
        String userBudget = (String) userPrefs.get("budgetLevel");
        String groupBudget = (String) groupPrefs.get("budgetLevel");
        
        if (userBudget == null || groupBudget == null) {
            return 0.5; // Neutral score if not specified
        }
        
        return userBudget.equalsIgnoreCase(groupBudget) ? 1.0 : 0.0;
    }
    
    private double calculateDateScore(Map<String, Object> userPrefs, Map<String, Object> groupPrefs) {
        String userStart = (String) userPrefs.get("startDate");
        String userEnd = (String) userPrefs.get("endDate");
        String groupStart = (String) groupPrefs.get("startDate");
        String groupEnd = (String) groupPrefs.get("endDate");
        
        if (userStart == null || userEnd == null || groupStart == null || groupEnd == null) {
            return 0.0;
        }
        
        // Exact match for dates
        if (userStart.equals(groupStart) && userEnd.equals(groupEnd)) {
            return 1.0;
        }
        
        // TODO: Could implement date overlap scoring here
        return 0.0;
    }
    
    private double calculateActivityScore(Map<String, Object> userPrefs, Map<String, Object> groupPrefs) {
        List<String> userActivities = (List<String>) userPrefs.get("preferredActivities");
        List<String> groupActivities = (List<String>) groupPrefs.get("preferredActivities");
        
        return calculateJaccardSimilarity(
            userActivities != null ? new HashSet<>(userActivities) : new HashSet<>(),
            groupActivities != null ? new HashSet<>(groupActivities) : new HashSet<>()
        );
    }
    
    private double calculateTerrainScore(Map<String, Object> userPrefs, Map<String, Object> groupPrefs) {
        List<String> userTerrains = (List<String>) userPrefs.get("preferredTerrains");
        List<String> groupTerrains = (List<String>) groupPrefs.get("preferredTerrains");
        
        return calculateJaccardSimilarity(
            userTerrains != null ? new HashSet<>(userTerrains) : new HashSet<>(),
            groupTerrains != null ? new HashSet<>(groupTerrains) : new HashSet<>()
        );
    }
    
    // Weight getters with configurable defaults
    @Value("${pooling.compatibility.weights.baseCity:0.5}")
    private double baseCityWeight;
    
    @Value("${pooling.compatibility.weights.activities:0.1}")
    private double activityWeight;
    
    @Value("${pooling.compatibility.weights.terrains:0.1}")
    private double terrainWeight;
    
    private double getBaseCityWeight() { return baseCityWeight; }
    private double getBudgetWeight() { return budgetWeight; }
    private double getDateWeight() { return dateWeight; }
    private double getActivityWeight() { return activityWeight; }
    private double getTerrainWeight() { return terrainWeight; }
}
