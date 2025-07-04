package com.islandhop.tripplanning.service.recommendation;

import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.service.external.TripAdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentBasedRecommender {
    
    private final TripAdvisorService tripAdvisorService;
    
    /**
     * Content-based recommendation based on trip preferences and location
     */
    public List<Recommendation> recommendAttractions(Trip trip, Integer day, UserPreferenceProfile userProfile) {
        log.info("Generating content-based recommendations for trip {}", trip.getTripId());
        
        try {
            // Determine search location
            LocationPoint searchLocation = determineSearchLocation(trip, day);
            
            // Search for attractions near the location
            List<PlannedPlace> candidates = tripAdvisorService.searchAttractions(
                    searchLocation.getLatitude(), 
                    searchLocation.getLongitude(), 
                    20); // 20km radius
            
            // Score and rank candidates
            return candidates.stream()
                    .map(place -> createContentBasedRecommendation(place, trip, day, userProfile))
                    .filter(rec -> rec.getScore() > 0.3) // Minimum threshold
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error in content-based recommendation: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Create content-based recommendation with detailed scoring
     */
    private Recommendation createContentBasedRecommendation(PlannedPlace place, Trip trip, Integer day, UserPreferenceProfile userProfile) {
        Recommendation rec = new Recommendation();
        rec.setRecommendationId(UUID.randomUUID().toString());
        rec.setTripId(trip.getTripId());
        rec.setSuggestedPlace(place);
        rec.setType(Recommendation.RecommendationType.ATTRACTION);
        rec.setSuggestedDay(day);
        
        // Calculate content-based score
        Map<String, Double> scoringFactors = calculateContentScore(place, trip, userProfile);
        double totalScore = scoringFactors.values().stream().mapToDouble(Double::doubleValue).sum() / scoringFactors.size();
        
        rec.setScore(Math.min(1.0, totalScore));
        rec.setScoringFactors(scoringFactors);
        rec.setUserPreferenceMatch(scoringFactors.get("categoryMatch"));
        rec.setLocationProximityScore(scoringFactors.get("locationProximity"));
        rec.setPopularityScore(scoringFactors.get("popularity"));
        
        // Generate reasons
        List<String> reasons = generateContentReasons(place, trip, scoringFactors);
        rec.setReasons(reasons);
        
        return rec;
    }
    
    /**
     * Calculate detailed content-based scoring
     */
    private Map<String, Double> calculateContentScore(PlannedPlace place, Trip trip, UserPreferenceProfile userProfile) {
        Map<String, Double> scores = new HashMap<>();
        
        // Category match score (0.0 - 1.0)
        scores.put("categoryMatch", calculateCategoryMatch(place, trip));
        
        // Location proximity score (0.0 - 1.0)
        scores.put("locationProximity", calculateLocationProximity(place, trip));
        
        // Popularity score (0.0 - 1.0)
        scores.put("popularity", calculatePopularityScore(place));
        
        // User historical preference score (0.0 - 1.0)
        scores.put("userPreference", calculateUserPreferenceScore(place, userProfile));
        
        // Time constraint score (0.0 - 1.0)
        scores.put("timeConstraint", calculateTimeConstraintScore(place, trip));
        
        // Diversity score (0.0 - 1.0) - encourages variety
        scores.put("diversity", calculateDiversityScore(place, trip));
        
        return scores;
    }
    
    /**
     * Calculate how well the place matches trip categories
     */
    private double calculateCategoryMatch(PlannedPlace place, Trip trip) {
        if (place.getCategories() == null || place.getCategories().isEmpty()) {
            return 0.2; // Default score for uncategorized places
        }
        
        Set<String> tripCategories = new HashSet<>(trip.getCategories());
        Set<String> placeCategories = new HashSet<>(place.getCategories());
        
        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(tripCategories);
        intersection.retainAll(placeCategories);
        
        Set<String> union = new HashSet<>(tripCategories);
        union.addAll(placeCategories);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate location proximity score
     */
    private double calculateLocationProximity(PlannedPlace place, Trip trip) {
        if (trip.getPlaces().isEmpty()) {
            // Base score for first place relative to base city
            return 0.8; // Assume good proximity to base city
        }
        
        // Find closest existing place
        double minDistance = trip.getPlaces().stream()
                .mapToDouble(existingPlace -> calculateDistance(
                        place.getLatitude(), place.getLongitude(),
                        existingPlace.getLatitude(), existingPlace.getLongitude()))
                .min()
                .orElse(Double.MAX_VALUE);
        
        // Convert distance to score (closer = higher score)
        if (minDistance <= 5.0) return 1.0;      // Within 5km
        if (minDistance <= 10.0) return 0.8;     // Within 10km
        if (minDistance <= 20.0) return 0.6;     // Within 20km
        if (minDistance <= 50.0) return 0.4;     // Within 50km
        return 0.2; // Further than 50km
    }
    
    /**
     * Calculate popularity score based on ratings and reviews
     */
    private double calculatePopularityScore(PlannedPlace place) {
        double score = 0.0;
        
        // Rating component (0.0 - 0.6)
        if (place.getRating() != null) {
            score += (place.getRating() / 5.0) * 0.6;
        }
        
        // Review count component (0.0 - 0.4)
        if (place.getReviewCount() != null) {
            if (place.getReviewCount() >= 1000) score += 0.4;
            else if (place.getReviewCount() >= 500) score += 0.3;
            else if (place.getReviewCount() >= 100) score += 0.2;
            else if (place.getReviewCount() >= 50) score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * Calculate user preference score based on historical data
     */
    private double calculateUserPreferenceScore(PlannedPlace place, UserPreferenceProfile userProfile) {
        if (userProfile == null || place.getCategories() == null) {
            return 0.5; // Neutral score
        }
        
        double score = 0.0;
        int categoryCount = 0;
        
        for (String category : place.getCategories()) {
            Integer preference = userProfile.getCategoryPreferences().get(category);
            if (preference != null) {
                score += Math.min(1.0, preference / 10.0); // Normalize to 0-1
                categoryCount++;
            }
        }
        
        return categoryCount > 0 ? score / categoryCount : 0.5;
    }
    
    /**
     * Calculate time constraint score
     */
    private double calculateTimeConstraintScore(PlannedPlace place, Trip trip) {
        // Consider activity pacing
        switch (trip.getPacing()) {
            case RELAXED:
                // Prefer longer visit times, fewer activities
                return place.getEstimatedVisitDurationMinutes() != null && 
                       place.getEstimatedVisitDurationMinutes() >= 120 ? 0.8 : 0.6;
            case ACTIVE:
                // Prefer shorter visit times, more activities
                return place.getEstimatedVisitDurationMinutes() != null && 
                       place.getEstimatedVisitDurationMinutes() <= 90 ? 0.8 : 0.6;
            case NORMAL:
            default:
                return 0.7; // Neutral
        }
    }
    
    /**
     * Calculate diversity score to encourage variety
     */
    private double calculateDiversityScore(PlannedPlace place, Trip trip) {
        if (trip.getPlaces().isEmpty()) {
            return 0.8; // High diversity for first place
        }
        
        // Check how different this place is from existing ones
        Set<String> existingCategories = trip.getPlaces().stream()
                .flatMap(p -> p.getCategories().stream())
                .collect(Collectors.toSet());
        
        if (place.getCategories() == null) {
            return 0.5;
        }
        
        long newCategories = place.getCategories().stream()
                .filter(cat -> !existingCategories.contains(cat))
                .count();
        
        return Math.min(1.0, 0.5 + (newCategories * 0.2));
    }
    
    /**
     * Generate human-readable reasons for the recommendation
     */
    private List<String> generateContentReasons(PlannedPlace place, Trip trip, Map<String, Double> scores) {
        List<String> reasons = new ArrayList<>();
        
        if (scores.get("categoryMatch") > 0.7) {
            reasons.add("Matches your interests in " + String.join(", ", trip.getCategories()));
        }
        
        if (scores.get("locationProximity") > 0.8) {
            reasons.add("Conveniently located near your other planned activities");
        }
        
        if (scores.get("popularity") > 0.8) {
            reasons.add("Highly rated attraction with excellent reviews");
        }
        
        if (scores.get("diversity") > 0.7) {
            reasons.add("Adds variety to your trip experience");
        }
        
        if (place.getRating() != null && place.getRating() >= 4.5) {
            reasons.add("Outstanding " + place.getRating() + "/5 rating");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("Recommended based on your preferences");
        }
        
        return reasons;
    }
    
    /**
     * Determine the best location to search for recommendations
     */
    private LocationPoint determineSearchLocation(Trip trip, Integer day) {
        // If specific day is requested and has places
        if (day != null) {
            List<PlannedPlace> dayPlaces = trip.getPlaces().stream()
                    .filter(place -> day.equals(place.getDayNumber()))
                    .collect(Collectors.toList());
            
            if (!dayPlaces.isEmpty()) {
                return calculateCentroid(dayPlaces);
            }
        }
        
        // If trip has existing places, use centroid
        if (!trip.getPlaces().isEmpty()) {
            return calculateCentroid(trip.getPlaces());
        }
        
        // Default to base city (you might want to geocode the city name)
        return getDefaultLocationForCity(trip.getBaseCity());
    }
    
    /**
     * Calculate centroid of a list of places
     */
    private LocationPoint calculateCentroid(List<PlannedPlace> places) {
        double avgLat = places.stream().mapToDouble(PlannedPlace::getLatitude).average().orElse(0.0);
        double avgLng = places.stream().mapToDouble(PlannedPlace::getLongitude).average().orElse(0.0);
        return new LocationPoint(avgLat, avgLng);
    }
    
    /**
     * Get default coordinates for major cities (simplified)
     */
    private LocationPoint getDefaultLocationForCity(String city) {
        // This is simplified - in production, you'd use geocoding
        Map<String, LocationPoint> cityCoordinates = Map.of(
            "Colombo", new LocationPoint(6.9271, 79.8612),
            "Kandy", new LocationPoint(7.2906, 80.6337),
            "Galle", new LocationPoint(6.0329, 80.2168),
            "Anuradhapura", new LocationPoint(8.3114, 80.4037),
            "Nuwara Eliya", new LocationPoint(6.9497, 80.7891)
        );
        
        return cityCoordinates.getOrDefault(city, new LocationPoint(7.8731, 80.7718)); // Sri Lanka center
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
    
    /**
     * Simple location point class
     */
    private static class LocationPoint {
        private final double latitude;
        private final double longitude;
        
        public LocationPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
