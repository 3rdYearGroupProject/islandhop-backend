package com.islandhop.tripplanning.service.recommendation;

import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollaborativeRecommender {
    
    private final TripRepository tripRepository;
    
    /**
     * Collaborative filtering recommendation based on similar users
     */
    public List<Recommendation> recommendAttractions(Trip trip, Integer day, UserPreferenceProfile userProfile) {
        log.info("Generating collaborative recommendations for trip {}", trip.getTripId());
        
        try {
            // Find similar users/trips
            List<Trip> similarTrips = findSimilarTrips(trip);
            
            if (similarTrips.isEmpty()) {
                log.info("No similar trips found for collaborative filtering");
                return Collections.emptyList();
            }
            
            // Analyze what similar users visited
            Map<String, AttractionStats> attractionStats = analyzePopularAttractions(similarTrips, trip);
            
            // Create recommendations based on collaborative data
            return attractionStats.entrySet().stream()
                    .map(entry -> createCollaborativeRecommendation(entry.getKey(), entry.getValue(), trip, day))
                    .filter(rec -> rec.getScore() > 0.4) // Minimum collaborative threshold
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error in collaborative filtering: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find trips similar to the current trip
     */
    private List<Trip> findSimilarTrips(Trip currentTrip) {
        // Find trips with similar categories and pacing
        List<Trip> candidateTrips = tripRepository.findSimilarTrips(
                currentTrip.getCategories(), 
                currentTrip.getPacing());
        
        // Calculate similarity scores and filter
        return candidateTrips.stream()
                .filter(trip -> !trip.getTripId().equals(currentTrip.getTripId())) // Exclude current trip
                .filter(trip -> calculateTripSimilarity(currentTrip, trip) > 0.6) // Similarity threshold
                .limit(20) // Limit to top 20 similar trips
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate similarity between two trips
     */
    private double calculateTripSimilarity(Trip trip1, Trip trip2) {
        double similarity = 0.0;
        
        // Category similarity (40% weight)
        double categorySimilarity = calculateCategorySimilarity(trip1.getCategories(), trip2.getCategories());
        similarity += categorySimilarity * 0.4;
        
        // Pacing similarity (20% weight)
        double pacingSimilarity = trip1.getPacing() == trip2.getPacing() ? 1.0 : 0.0;
        similarity += pacingSimilarity * 0.2;
        
        // Duration similarity (20% weight)
        double durationSimilarity = calculateDurationSimilarity(trip1, trip2);
        similarity += durationSimilarity * 0.2;
        
        // Location similarity (20% weight)
        double locationSimilarity = calculateLocationSimilarity(trip1, trip2);
        similarity += locationSimilarity * 0.2;
        
        return similarity;
    }
    
    /**
     * Calculate category similarity using Jaccard index
     */
    private double calculateCategorySimilarity(List<String> categories1, List<String> categories2) {
        if (categories1.isEmpty() && categories2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> set1 = new HashSet<>(categories1);
        Set<String> set2 = new HashSet<>(categories2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate duration similarity
     */
    private double calculateDurationSimilarity(Trip trip1, Trip trip2) {
        long duration1 = trip1.getEndDate().toEpochDay() - trip1.getStartDate().toEpochDay() + 1;
        long duration2 = trip2.getEndDate().toEpochDay() - trip2.getStartDate().toEpochDay() + 1;
        
        double ratio = Math.min(duration1, duration2) / (double) Math.max(duration1, duration2);
        return ratio;
    }
    
    /**
     * Calculate location similarity based on base cities
     */
    private double calculateLocationSimilarity(Trip trip1, Trip trip2) {
        // Simple string comparison - in production, use geographic distance
        if (trip1.getBaseCity().equalsIgnoreCase(trip2.getBaseCity())) {
            return 1.0;
        }
        
        // Check if cities are in the same region (simplified)
        Map<String, String> cityRegions = Map.of(
            "Colombo", "Western",
            "Kandy", "Central",
            "Galle", "Southern",
            "Anuradhapura", "North Central",
            "Nuwara Eliya", "Central"
        );
        
        String region1 = cityRegions.get(trip1.getBaseCity());
        String region2 = cityRegions.get(trip2.getBaseCity());
        
        if (region1 != null && region1.equals(region2)) {
            return 0.7; // Same region
        }
        
        return 0.3; // Different regions
    }
    
    /**
     * Analyze popular attractions from similar trips
     */
    private Map<String, AttractionStats> analyzePopularAttractions(List<Trip> similarTrips, Trip currentTrip) {
        Map<String, AttractionStats> attractionStats = new HashMap<>();
        
        for (Trip trip : similarTrips) {
            double tripWeight = calculateTripWeight(trip, currentTrip);
            
            for (PlannedPlace place : trip.getPlaces()) {
                if (place.getType() == PlannedPlace.PlaceType.ATTRACTION) {
                    AttractionStats stats = attractionStats.computeIfAbsent(
                            place.getPlaceId(), 
                            k -> new AttractionStats(place));
                    
                    stats.addVisit(tripWeight);
                    
                    // Track user satisfaction if available
                    if (place.isConfirmed()) {
                        stats.addPositiveFeedback(tripWeight);
                    }
                }
            }
        }
        
        return attractionStats;
    }
    
    /**
     * Calculate weight for a trip based on recency and user satisfaction
     */
    private double calculateTripWeight(Trip trip, Trip currentTrip) {
        double weight = 1.0;
        
        // Recency weight - more recent trips have higher weight
        if (trip.getCreatedAt() != null) {
            long daysSinceCreation = java.time.ChronoUnit.DAYS.between(
                    trip.getCreatedAt().toLocalDate(), 
                    java.time.LocalDate.now());
            
            if (daysSinceCreation <= 30) weight *= 1.2;      // Recent trips
            else if (daysSinceCreation <= 90) weight *= 1.0;  // Medium age
            else if (daysSinceCreation <= 180) weight *= 0.8; // Older trips
            else weight *= 0.6; // Very old trips
        }
        
        // Trip completion weight
        if (trip.getStatus() == Trip.TripStatus.COMPLETED) {
            weight *= 1.1; // Completed trips are more valuable
        }
        
        // User satisfaction weight
        if (trip.getStatistics() != null && trip.getStatistics().getUserSatisfactionScore() != null) {
            weight *= trip.getStatistics().getUserSatisfactionScore();
        }
        
        return weight;
    }
    
    /**
     * Create collaborative recommendation
     */
    private Recommendation createCollaborativeRecommendation(String placeId, AttractionStats stats, Trip trip, Integer day) {
        Recommendation rec = new Recommendation();
        rec.setRecommendationId(UUID.randomUUID().toString());
        rec.setTripId(trip.getTripId());
        rec.setSuggestedPlace(stats.getPlace());
        rec.setType(Recommendation.RecommendationType.ATTRACTION);
        rec.setSuggestedDay(day);
        
        // Calculate collaborative score
        double popularityScore = Math.min(1.0, stats.getVisitCount() / 10.0); // Normalize visits
        double satisfactionScore = stats.getPositiveFeedbackRatio();
        double collaborativeScore = (popularityScore * 0.6) + (satisfactionScore * 0.4);
        
        rec.setScore(collaborativeScore);
        
        // Set scoring factors
        Map<String, Double> scoringFactors = new HashMap<>();
        scoringFactors.put("popularity", popularityScore);
        scoringFactors.put("userSatisfaction", satisfactionScore);
        scoringFactors.put("collaborative", collaborativeScore);
        rec.setScoringFactors(scoringFactors);
        
        // Generate reasons
        List<String> reasons = generateCollaborativeReasons(stats);
        rec.setReasons(reasons);
        
        return rec;
    }
    
    /**
     * Generate reasons for collaborative recommendations
     */
    private List<String> generateCollaborativeReasons(AttractionStats stats) {
        List<String> reasons = new ArrayList<>();
        
        if (stats.getVisitCount() >= 5) {
            reasons.add("Popular among travelers with similar interests");
        }
        
        if (stats.getPositiveFeedbackRatio() > 0.8) {
            reasons.add("Highly recommended by previous visitors");
        }
        
        if (stats.getVisitCount() >= 3 && stats.getPositiveFeedbackRatio() > 0.7) {
            reasons.add("Frequently visited and well-rated by similar travelers");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("Recommended based on similar traveler preferences");
        }
        
        return reasons;
    }
    
    /**
     * Statistics for tracking attraction popularity and satisfaction
     */
    private static class AttractionStats {
        private final PlannedPlace place;
        private double visitCount = 0.0;
        private double positiveFeedback = 0.0;
        
        public AttractionStats(PlannedPlace place) {
            this.place = place;
        }
        
        public void addVisit(double weight) {
            this.visitCount += weight;
        }
        
        public void addPositiveFeedback(double weight) {
            this.positiveFeedback += weight;
        }
        
        public PlannedPlace getPlace() {
            return place;
        }
        
        public double getVisitCount() {
            return visitCount;
        }
        
        public double getPositiveFeedbackRatio() {
            return visitCount > 0 ? positiveFeedback / visitCount : 0.0;
        }
    }
}
