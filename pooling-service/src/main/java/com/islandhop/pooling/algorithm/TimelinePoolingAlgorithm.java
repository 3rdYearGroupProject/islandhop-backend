package com.islandhop.pooling.algorithm;

import com.islandhop.pooling.client.TripDto;
import com.islandhop.pooling.client.TouristProfileDto;
import com.islandhop.pooling.client.PlannedPlaceDto;
import com.islandhop.pooling.model.CompatibilityScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TimelinePoolingAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(TimelinePoolingAlgorithm.class);

    @Value("${pooling.timeline.flexibility-days:2}")
    private int flexibilityDays;

    @Value("${pooling.compatibility.min-score:0.6}")
    private double minCompatibilityScore;

    @Value("${pooling.geographic.max-distance-km:50}")
    private double maxDistanceKm;

    /**
     * Find potential trip pools based on timeline overlap
     */
    public List<TripPoolMatch> findTimelineMatches(TripDto userTrip, List<TripDto> candidateTrips, 
                                                  TouristProfileDto userProfile, 
                                                  Map<String, TouristProfileDto> candidateProfiles) {
        
        logger.info("Finding timeline matches for trip {} with {} candidates", 
                   userTrip.getTripId(), candidateTrips.size());
        
        List<TripPoolMatch> matches = new ArrayList<>();
        
        for (TripDto candidateTrip : candidateTrips) {
            // Skip own trip
            if (candidateTrip.getTripId().equals(userTrip.getTripId())) {
                continue;
            }
            
            // Check timeline overlap
            TimelineOverlap overlap = calculateTimelineOverlap(userTrip, candidateTrip);
            if (overlap.getOverlapDays() < 1) {
                continue; // No meaningful overlap
            }
            
            // Calculate compatibility score
            TouristProfileDto candidateProfile = candidateProfiles.get(candidateTrip.getUserId());
            if (candidateProfile == null) {
                logger.warn("No profile found for user: {}", candidateTrip.getUserId());
                continue;
            }
            
            CompatibilityScore compatibility = calculateCompatibility(
                userTrip, candidateTrip, userProfile, candidateProfile);
            
            if (compatibility.getOverallScore() >= minCompatibilityScore) {
                TripPoolMatch match = new TripPoolMatch();
                match.setCandidateTrip(candidateTrip);
                match.setCandidateProfile(candidateProfile);
                match.setTimelineOverlap(overlap);
                match.setCompatibilityScore(compatibility);
                match.setRouteCompatibility(calculateRouteCompatibility(userTrip, candidateTrip));
                
                matches.add(match);
                logger.debug("Found compatible match: {} with score {}", 
                           candidateTrip.getTripId(), compatibility.getOverallScore());
            }
        }
        
        // Sort by compatibility score (highest first)
        matches.sort((m1, m2) -> Double.compare(
            m2.getCompatibilityScore().getOverallScore(),
            m1.getCompatibilityScore().getOverallScore()
        ));
        
        logger.info("Found {} compatible matches for trip {}", matches.size(), userTrip.getTripId());
        return matches;
    }
    
    /**
     * Calculate timeline overlap between two trips
     */
    private TimelineOverlap calculateTimelineOverlap(TripDto trip1, TripDto trip2) {
        LocalDate start1 = trip1.getStartDate();
        LocalDate end1 = trip1.getEndDate();
        LocalDate start2 = trip2.getStartDate();
        LocalDate end2 = trip2.getEndDate();
        
        // Calculate overlap with flexibility
        LocalDate flexStart1 = start1.minusDays(flexibilityDays);
        LocalDate flexEnd1 = end1.plusDays(flexibilityDays);
        LocalDate flexStart2 = start2.minusDays(flexibilityDays);
        LocalDate flexEnd2 = end2.plusDays(flexibilityDays);
        
        LocalDate overlapStart = Collections.max(Arrays.asList(flexStart1, flexStart2));
        LocalDate overlapEnd = Collections.min(Arrays.asList(flexEnd1, flexEnd2));
        
        long overlapDays = 0;
        double overlapPercentage = 0.0;
        
        if (overlapStart.isBefore(overlapEnd) || overlapStart.isEqual(overlapEnd)) {
            overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            
            long trip1Days = ChronoUnit.DAYS.between(start1, end1) + 1;
            long trip2Days = ChronoUnit.DAYS.between(start2, end2) + 1;
            long totalUniqueDays = ChronoUnit.DAYS.between(
                Collections.min(Arrays.asList(start1, start2)),
                Collections.max(Arrays.asList(end1, end2))
            ) + 1;
            
            overlapPercentage = (double) overlapDays / totalUniqueDays;
        }
        
        TimelineOverlap overlap = new TimelineOverlap();
        overlap.setOverlapDays((int) overlapDays);
        overlap.setOverlapPercentage(overlapPercentage);
        overlap.setOverlapStart(overlapStart);
        overlap.setOverlapEnd(overlapEnd);
        overlap.setFlexibilityUsed(flexibilityDays);
        
        return overlap;
    }
    
    /**
     * Calculate compatibility score between two users and their trips
     */
    private CompatibilityScore calculateCompatibility(TripDto trip1, TripDto trip2,
                                                    TouristProfileDto profile1, TouristProfileDto profile2) {
        
        CompatibilityScore score = new CompatibilityScore();
        score.setUserId1(trip1.getUserId());
        score.setUserId2(trip2.getUserId());
        
        Map<String, Double> detailed = new HashMap<>();
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 1. Timeline Compatibility (25% weight)
        double timelineScore = calculateTimelineCompatibilityScore(trip1, trip2);
        detailed.put("timeline", timelineScore);
        if (timelineScore > 0.8) {
            reasons.add("Excellent timeline overlap (" + Math.round(timelineScore * 100) + "%)");
        }
        
        // 2. Interest Compatibility (30% weight)
        double interestScore = calculateInterestCompatibilityScore(trip1, trip2);
        detailed.put("interests", interestScore);
        if (interestScore > 0.7) {
            reasons.add("Similar travel interests");
        }
        
        // 3. Pacing Compatibility (20% weight)
        double pacingScore = calculatePacingCompatibilityScore(trip1, trip2);
        detailed.put("pacing", pacingScore);
        if (pacingScore < 0.5) {
            warnings.add("Different travel pacing preferences");
        }
        
        // 4. Demographic Compatibility (15% weight)
        double demographicScore = calculateDemographicCompatibilityScore(profile1, profile2);
        detailed.put("demographics", demographicScore);
        if (demographicScore > 0.8) {
            reasons.add("Compatible demographics");
        }
        
        // 5. Base City Compatibility (10% weight)
        double baseCityScore = trip1.getBaseCity().equalsIgnoreCase(trip2.getBaseCity()) ? 1.0 : 0.3;
        detailed.put("baseCity", baseCityScore);
        if (baseCityScore == 1.0) {
            reasons.add("Same starting city");
        }
        
        // Calculate weighted overall score
        double overallScore = 
            timelineScore * 0.25 +
            interestScore * 0.30 +
            pacingScore * 0.20 +
            demographicScore * 0.15 +
            baseCityScore * 0.10;
        
        score.setOverallScore(overallScore);
        score.setTimelineCompatibility(timelineScore);
        score.setInterestCompatibility(interestScore);
        score.setPacingCompatibility(pacingScore);
        score.setDemographicCompatibility(demographicScore);
        score.setDetailedScores(detailed);
        score.setCompatibilityReasons(reasons);
        score.setIncompatibilityWarnings(warnings);
        
        Map<String, Double> weights = new HashMap<>();
        weights.put("timeline", 0.25);
        weights.put("interests", 0.30);
        weights.put("pacing", 0.20);
        weights.put("demographics", 0.15);
        weights.put("baseCity", 0.10);
        score.setScoreWeights(weights);
        
        return score;
    }
    
    private double calculateTimelineCompatibilityScore(TripDto trip1, TripDto trip2) {
        TimelineOverlap overlap = calculateTimelineOverlap(trip1, trip2);
        return Math.min(1.0, overlap.getOverlapPercentage() * 2); // Scale to make overlap more significant
    }
    
    private double calculateInterestCompatibilityScore(TripDto trip1, TripDto trip2) {
        Set<String> interests1 = new HashSet<>(trip1.getCategories());
        Set<String> interests2 = new HashSet<>(trip2.getCategories());
        
        Set<String> intersection = new HashSet<>(interests1);
        intersection.retainAll(interests2);
        
        Set<String> union = new HashSet<>(interests1);
        union.addAll(interests2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private double calculatePacingCompatibilityScore(TripDto trip1, TripDto trip2) {
        String pacing1 = trip1.getPacing();
        String pacing2 = trip2.getPacing();
        
        if (pacing1.equals(pacing2)) {
            return 1.0;
        }
        
        // Define compatibility matrix
        Map<String, Map<String, Double>> pacingCompatibility = new HashMap<>();
        pacingCompatibility.put("RELAXED", Map.of("RELAXED", 1.0, "NORMAL", 0.7, "ACTIVE", 0.3));
        pacingCompatibility.put("NORMAL", Map.of("RELAXED", 0.7, "NORMAL", 1.0, "ACTIVE", 0.7));
        pacingCompatibility.put("ACTIVE", Map.of("RELAXED", 0.3, "NORMAL", 0.7, "ACTIVE", 1.0));
        
        return pacingCompatibility.getOrDefault(pacing1, new HashMap<>()).getOrDefault(pacing2, 0.5);
    }
    
    private double calculateDemographicCompatibilityScore(TouristProfileDto profile1, TouristProfileDto profile2) {
        double score = 0.0;
        int factors = 0;
        
        // Language compatibility
        Set<String> languages1 = new HashSet<>(profile1.getLanguages());
        Set<String> languages2 = new HashSet<>(profile2.getLanguages());
        Set<String> commonLanguages = new HashSet<>(languages1);
        commonLanguages.retainAll(languages2);
        
        if (!commonLanguages.isEmpty()) {
            score += 0.8; // High bonus for common language
        } else {
            score += 0.3; // Still manageable without common language
        }
        factors++;
        
        // Nationality compatibility (same nationality gets bonus, different is neutral)
        if (profile1.getNationality().equals(profile2.getNationality())) {
            score += 0.7;
        } else {
            score += 0.5; // Neutral - diversity can be good
        }
        factors++;
        
        return factors > 0 ? score / factors : 0.5;
    }
    
    /**
     * Calculate route similarity based on planned places
     */
    private double calculateRouteCompatibility(TripDto trip1, TripDto trip2) {
        if (trip1.getPlaces() == null || trip2.getPlaces() == null) {
            return 0.3; // Neutral if no place data
        }
        
        Set<String> cities1 = trip1.getPlaces().stream()
                .map(p -> p.getCity().toLowerCase())
                .collect(Collectors.toSet());
        
        Set<String> cities2 = trip2.getPlaces().stream()
                .map(p -> p.getCity().toLowerCase())
                .collect(Collectors.toSet());
        
        Set<String> commonCities = new HashSet<>(cities1);
        commonCities.retainAll(cities2);
        
        Set<String> allCities = new HashSet<>(cities1);
        allCities.addAll(cities2);
        
        if (allCities.isEmpty()) {
            return 0.3;
        }
        
        double cityOverlap = (double) commonCities.size() / allCities.size();
        
        // Also consider proximity of places
        double proximityScore = calculatePlaceProximityScore(trip1.getPlaces(), trip2.getPlaces());
        
        return (cityOverlap * 0.7 + proximityScore * 0.3);
    }
    
    private double calculatePlaceProximityScore(List<PlannedPlaceDto> places1, List<PlannedPlaceDto> places2) {
        if (places1.isEmpty() || places2.isEmpty()) {
            return 0.0;
        }
        
        int nearbyPairs = 0;
        int totalComparisons = 0;
        
        for (PlannedPlaceDto place1 : places1) {
            for (PlannedPlaceDto place2 : places2) {
                double distance = calculateDistance(
                    place1.getLatitude(), place1.getLongitude(),
                    place2.getLatitude(), place2.getLongitude()
                );
                
                if (distance <= maxDistanceKm) {
                    nearbyPairs++;
                }
                totalComparisons++;
            }
        }
        
        return totalComparisons > 0 ? (double) nearbyPairs / totalComparisons : 0.0;
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    // Helper classes
    public static class TripPoolMatch {
        private TripDto candidateTrip;
        private TouristProfileDto candidateProfile;
        private TimelineOverlap timelineOverlap;
        private CompatibilityScore compatibilityScore;
        private double routeCompatibility;
        
        // Getters and Setters
        public TripDto getCandidateTrip() { return candidateTrip; }
        public void setCandidateTrip(TripDto candidateTrip) { this.candidateTrip = candidateTrip; }
        public TouristProfileDto getCandidateProfile() { return candidateProfile; }
        public void setCandidateProfile(TouristProfileDto candidateProfile) { this.candidateProfile = candidateProfile; }
        public TimelineOverlap getTimelineOverlap() { return timelineOverlap; }
        public void setTimelineOverlap(TimelineOverlap timelineOverlap) { this.timelineOverlap = timelineOverlap; }
        public CompatibilityScore getCompatibilityScore() { return compatibilityScore; }
        public void setCompatibilityScore(CompatibilityScore compatibilityScore) { this.compatibilityScore = compatibilityScore; }
        public double getRouteCompatibility() { return routeCompatibility; }
        public void setRouteCompatibility(double routeCompatibility) { this.routeCompatibility = routeCompatibility; }
    }
    
    public static class TimelineOverlap {
        private int overlapDays;
        private double overlapPercentage;
        private LocalDate overlapStart;
        private LocalDate overlapEnd;
        private int flexibilityUsed;
        
        // Getters and Setters
        public int getOverlapDays() { return overlapDays; }
        public void setOverlapDays(int overlapDays) { this.overlapDays = overlapDays; }
        public double getOverlapPercentage() { return overlapPercentage; }
        public void setOverlapPercentage(double overlapPercentage) { this.overlapPercentage = overlapPercentage; }
        public LocalDate getOverlapStart() { return overlapStart; }
        public void setOverlapStart(LocalDate overlapStart) { this.overlapStart = overlapStart; }
        public LocalDate getOverlapEnd() { return overlapEnd; }
        public void setOverlapEnd(LocalDate overlapEnd) { this.overlapEnd = overlapEnd; }
        public int getFlexibilityUsed() { return flexibilityUsed; }
        public void setFlexibilityUsed(int flexibilityUsed) { this.flexibilityUsed = flexibilityUsed; }
    }
}
