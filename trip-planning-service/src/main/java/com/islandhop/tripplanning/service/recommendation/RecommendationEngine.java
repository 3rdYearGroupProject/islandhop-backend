package com.islandhop.tripplanning.service.recommendation;

import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.repository.TripRepository;
import com.islandhop.tripplanning.repository.UserPreferenceRepository;
import com.islandhop.tripplanning.service.external.TripAdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {
    
    private final TripRepository tripRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final TripAdvisorService tripAdvisorService;
    private final ContentBasedRecommender contentBasedRecommender;
    private final CollaborativeRecommender collaborativeRecommender;
    
    @Value("${recommendation.similarity-threshold:0.7}")
    private double similarityThreshold;
    
    @Value("${recommendation.max-attractions-per-day:4}")
    private int maxAttractionsPerDay;
    
    /**
     * Hybrid recommendation algorithm for attractions
     * Combines content-based and collaborative filtering
     */
    public List<Recommendation> recommendAttractions(Trip trip, Integer day) {
        log.info("Generating attraction recommendations for trip {} day {}", trip.getTripId(), day);
        
        try {
            // Get user preference profile
            UserPreferenceProfile userProfile = getUserPreferenceProfile(trip.getUserId());
            
            // Content-based recommendations (based on trip preferences and location)
            List<Recommendation> contentRecommendations = contentBasedRecommender
                    .recommendAttractions(trip, day, userProfile);
            
            // Collaborative filtering recommendations (based on similar users)
            List<Recommendation> collaborativeRecommendations = collaborativeRecommender
                    .recommendAttractions(trip, day, userProfile);
            
            // Hybrid combination with weighted scoring
            List<Recommendation> hybridRecommendations = combineRecommendations(
                    contentRecommendations, collaborativeRecommendations, 0.7, 0.3);
            
            // Apply business rules and constraints
            List<Recommendation> filteredRecommendations = applyConstraints(hybridRecommendations, trip, day);
            
            // Sort by final score and limit results
            return filteredRecommendations.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(day != null ? maxAttractionsPerDay : maxAttractionsPerDay * 2)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating attraction recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Recommend hotels based on trip locations and preferences
     */
    public List<Recommendation> recommendHotels(Trip trip, Integer day) {
        log.info("Generating hotel recommendations for trip {} day {}", trip.getTripId(), day);
        
        try {
            List<PlannedPlace> attractions = getAttractionsForDay(trip, day);
            if (attractions.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Get hotels near attractions
            List<PlannedPlace> hotels = tripAdvisorService.searchHotels(
                    attractions.get(0).getLatitude(), 
                    attractions.get(0).getLongitude(), 
                    10); // 10km radius
            
            return hotels.stream()
                    .map(hotel -> createHotelRecommendation(hotel, trip, day))
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(5)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating hotel recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Recommend restaurants based on location and meal times
     */
    public List<Recommendation> recommendRestaurants(Trip trip, Integer day) {
        log.info("Generating restaurant recommendations for trip {} day {}", trip.getTripId(), day);
        
        try {
            List<PlannedPlace> attractions = getAttractionsForDay(trip, day);
            if (attractions.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Get restaurants near attractions
            List<PlannedPlace> restaurants = tripAdvisorService.searchRestaurants(
                    attractions.get(0).getLatitude(), 
                    attractions.get(0).getLongitude(), 
                    5); // 5km radius
            
            return restaurants.stream()
                    .map(restaurant -> createRestaurantRecommendation(restaurant, trip, day))
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(3)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating restaurant recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Combine content-based and collaborative recommendations with weights
     */
    private List<Recommendation> combineRecommendations(
            List<Recommendation> contentRecs, 
            List<Recommendation> collaborativeRecs, 
            double contentWeight, 
            double collaborativeWeight) {
        
        Map<String, Recommendation> combinedMap = new HashMap<>();
        
        // Add content-based recommendations
        for (Recommendation rec : contentRecs) {
            rec.setScore(rec.getScore() * contentWeight);
            combinedMap.put(rec.getSuggestedPlace().getPlaceId(), rec);
        }
        
        // Add or combine collaborative recommendations
        for (Recommendation rec : collaborativeRecs) {
            String placeId = rec.getSuggestedPlace().getPlaceId();
            if (combinedMap.containsKey(placeId)) {
                // Combine scores
                Recommendation existing = combinedMap.get(placeId);
                double combinedScore = existing.getScore() + (rec.getScore() * collaborativeWeight);
                existing.setScore(combinedScore);
                
                // Combine reasons
                List<String> combinedReasons = new ArrayList<>(existing.getReasons());
                combinedReasons.addAll(rec.getReasons());
                existing.setReasons(combinedReasons);
            } else {
                rec.setScore(rec.getScore() * collaborativeWeight);
                combinedMap.put(placeId, rec);
            }
        }
        
        return new ArrayList<>(combinedMap.values());
    }
    
    /**
     * Apply business rules and constraints
     */
    private List<Recommendation> applyConstraints(List<Recommendation> recommendations, Trip trip, Integer day) {
        return recommendations.stream()
                .filter(rec -> !trip.getExcludedAttractions().contains(rec.getSuggestedPlace().getPlaceId()))
                .filter(rec -> !isAlreadyInTrip(rec.getSuggestedPlace(), trip))
                .filter(rec -> rec.getScore() >= similarityThreshold)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user preference profile or create new one
     */
    private UserPreferenceProfile getUserPreferenceProfile(String userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserProfile(userId));
    }
    
    /**
     * Create new user preference profile
     */
    private UserPreferenceProfile createNewUserProfile(String userId) {
        UserPreferenceProfile profile = new UserPreferenceProfile();
        profile.setUserId(userId);
        profile.setCategoryPreferences(new HashMap<>());
        profile.setLocationPreferences(new HashMap<>());
        profile.setVisitedAttractions(new HashMap<>());
        profile.setPreferredTravelModes(new ArrayList<>());
        profile.setAttractionRatings(new HashMap<>());
        profile.setSimilarUsers(new ArrayList<>());
        profile.setLastUpdated(java.time.LocalDateTime.now());
        
        return userPreferenceRepository.save(profile);
    }
    
    /**
     * Get attractions for a specific day
     */
    private List<PlannedPlace> getAttractionsForDay(Trip trip, Integer day) {
        if (day == null) {
            return trip.getPlaces().stream()
                    .filter(place -> place.getType() == PlannedPlace.PlaceType.ATTRACTION)
                    .collect(Collectors.toList());
        } else {
            return trip.getPlaces().stream()
                    .filter(place -> place.getType() == PlannedPlace.PlaceType.ATTRACTION)
                    .filter(place -> day.equals(place.getDayNumber()))
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Create hotel recommendation
     */
    private Recommendation createHotelRecommendation(PlannedPlace hotel, Trip trip, Integer day) {
        Recommendation rec = new Recommendation();
        rec.setRecommendationId(UUID.randomUUID().toString());
        rec.setTripId(trip.getTripId());
        rec.setSuggestedPlace(hotel);
        rec.setType(Recommendation.RecommendationType.HOTEL);
        rec.setSuggestedDay(day);
        
        // Calculate hotel score based on rating, location, and price
        double score = calculateHotelScore(hotel, trip);
        rec.setScore(score);
        
        List<String> reasons = new ArrayList<>();
        reasons.add("Near your planned attractions");
        if (hotel.getRating() != null && hotel.getRating() > 4.0) {
            reasons.add("Highly rated (" + hotel.getRating() + "/5)");
        }
        rec.setReasons(reasons);
        
        return rec;
    }
    
    /**
     * Create restaurant recommendation
     */
    private Recommendation createRestaurantRecommendation(PlannedPlace restaurant, Trip trip, Integer day) {
        Recommendation rec = new Recommendation();
        rec.setRecommendationId(UUID.randomUUID().toString());
        rec.setTripId(trip.getTripId());
        rec.setSuggestedPlace(restaurant);
        rec.setType(Recommendation.RecommendationType.RESTAURANT);
        rec.setSuggestedDay(day);
        
        // Calculate restaurant score
        double score = calculateRestaurantScore(restaurant, trip);
        rec.setScore(score);
        
        List<String> reasons = new ArrayList<>();
        reasons.add("Convenient location near attractions");
        if (restaurant.getRating() != null && restaurant.getRating() > 4.0) {
            reasons.add("Excellent reviews (" + restaurant.getRating() + "/5)");
        }
        rec.setReasons(reasons);
        
        return rec;
    }
    
    /**
     * Calculate hotel score
     */
    private double calculateHotelScore(PlannedPlace hotel, Trip trip) {
        double score = 0.5; // Base score
        
        // Rating factor
        if (hotel.getRating() != null) {
            score += (hotel.getRating() / 5.0) * 0.3;
        }
        
        // Price factor (budget-friendly gets higher score)
        if ("BUDGET".equals(hotel.getPriceLevel())) {
            score += 0.2;
        } else if ("MODERATE".equals(hotel.getPriceLevel())) {
            score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * Calculate restaurant score
     */
    private double calculateRestaurantScore(PlannedPlace restaurant, Trip trip) {
        double score = 0.5; // Base score
        
        // Rating factor
        if (restaurant.getRating() != null) {
            score += (restaurant.getRating() / 5.0) * 0.4;
        }
        
        // Review count factor
        if (restaurant.getReviewCount() != null && restaurant.getReviewCount() > 100) {
            score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * Check if place is already in trip
     */
    private boolean isAlreadyInTrip(PlannedPlace place, Trip trip) {
        return trip.getPlaces().stream()
                .anyMatch(p -> p.getPlaceId().equals(place.getPlaceId()));
    }
}
