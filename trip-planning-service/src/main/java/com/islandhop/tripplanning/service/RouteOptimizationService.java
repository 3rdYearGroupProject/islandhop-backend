package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {
    
    private final TravelTimeService travelTimeService;
    
    /**
     * Optimize the route for the entire trip
     */
    public Trip optimizeRoute(Trip trip) {
        log.info("Optimizing route for trip: {}", trip.getTripId());
        
        // Group places by day
        Map<Integer, List<PlannedPlace>> placesByDay = groupPlacesByDay(trip);
        
        // Optimize each day separately
        for (Map.Entry<Integer, List<PlannedPlace>> entry : placesByDay.entrySet()) {
            Integer day = entry.getKey();
            List<PlannedPlace> places = entry.getValue();
            
            if (places.size() > 1) {
                List<PlannedPlace> optimizedOrder = optimizeDayRoute(places);
                assignOptimizedOrder(optimizedOrder, day);
            }
        }
        
        // Create day plans with travel segments
        List<DayPlan> dayPlans = createOptimizedDayPlans(trip, placesByDay);
        trip.setDayPlans(dayPlans);
        
        return trip;
    }
    
    /**
     * Group places by day number
     */
    private Map<Integer, List<PlannedPlace>> groupPlacesByDay(Trip trip) {
        return trip.getPlaces().stream()
                .filter(place -> place.getDayNumber() != null)
                .collect(Collectors.groupingBy(PlannedPlace::getDayNumber));
    }
    
    /**
     * Optimize route for a single day using nearest neighbor algorithm
     */
    private List<PlannedPlace> optimizeDayRoute(List<PlannedPlace> places) {
        if (places.size() <= 1) {
            return new ArrayList<>(places);
        }
        
        log.info("Optimizing route for {} places", places.size());
        
        List<PlannedPlace> unvisited = new ArrayList<>(places);
        List<PlannedPlace> optimizedRoute = new ArrayList<>();
        
        // Start with the first place (or could be smarter about starting point)
        PlannedPlace current = unvisited.remove(0);
        optimizedRoute.add(current);
        
        // Apply nearest neighbor algorithm
        while (!unvisited.isEmpty()) {
            PlannedPlace nearest = findNearestPlace(current, unvisited);
            optimizedRoute.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }
        
        // Try to improve with 2-opt optimization
        optimizedRoute = improve2Opt(optimizedRoute);
        
        return optimizedRoute;
    }
    
    /**
     * Find the nearest place to the current location
     */
    private PlannedPlace findNearestPlace(PlannedPlace current, List<PlannedPlace> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(place -> 
                        calculateDistance(current.getLatitude(), current.getLongitude(),
                                        place.getLatitude(), place.getLongitude())))
                .orElse(candidates.get(0));
    }
    
    /**
     * Improve route using 2-opt optimization
     */
    private List<PlannedPlace> improve2Opt(List<PlannedPlace> route) {
        if (route.size() < 4) {
            return route; // Need at least 4 places for 2-opt
        }
        
        List<PlannedPlace> bestRoute = new ArrayList<>(route);
        double bestDistance = calculateTotalDistance(bestRoute);
        boolean improved = true;
        
        while (improved) {
            improved = false;
            
            for (int i = 1; i < route.size() - 2; i++) {
                for (int j = i + 1; j < route.size(); j++) {
                    if (j - i == 1) continue; // Skip adjacent edges
                    
                    List<PlannedPlace> newRoute = twoOptSwap(route, i, j);
                    double newDistance = calculateTotalDistance(newRoute);
                    
                    if (newDistance < bestDistance) {
                        bestRoute = newRoute;
                        bestDistance = newDistance;
                        route = newRoute;
                        improved = true;
                    }
                }
            }
        }
        
        return bestRoute;
    }
    
    /**
     * Perform 2-opt swap
     */
    private List<PlannedPlace> twoOptSwap(List<PlannedPlace> route, int i, int j) {
        List<PlannedPlace> newRoute = new ArrayList<>();
        
        // Add places from start to i
        for (int k = 0; k <= i - 1; k++) {
            newRoute.add(route.get(k));
        }
        
        // Add places from j to i in reverse order
        for (int k = j; k >= i; k--) {
            newRoute.add(route.get(k));
        }
        
        // Add remaining places
        for (int k = j + 1; k < route.size(); k++) {
            newRoute.add(route.get(k));
        }
        
        return newRoute;
    }
    
    /**
     * Calculate total distance for a route
     */
    private double calculateTotalDistance(List<PlannedPlace> route) {
        double totalDistance = 0.0;
        
        for (int i = 0; i < route.size() - 1; i++) {
            PlannedPlace from = route.get(i);
            PlannedPlace to = route.get(i + 1);
            totalDistance += calculateDistance(from.getLatitude(), from.getLongitude(),
                                             to.getLatitude(), to.getLongitude());
        }
        
        return totalDistance;
    }
    
    /**
     * Assign optimized order to places
     */
    private void assignOptimizedOrder(List<PlannedPlace> places, Integer day) {
        for (int i = 0; i < places.size(); i++) {
            places.get(i).setOrderInDay(i + 1);
        }
    }
    
    /**
     * Create optimized day plans with travel segments and timing
     */
    private List<DayPlan> createOptimizedDayPlans(Trip trip, Map<Integer, List<PlannedPlace>> placesByDay) {
        List<DayPlan> dayPlans = new ArrayList<>();
        
        for (Map.Entry<Integer, List<PlannedPlace>> entry : placesByDay.entrySet()) {
            Integer day = entry.getKey();
            List<PlannedPlace> places = entry.getValue();
            
            DayPlan dayPlan = createDayPlan(trip, day, places);
            dayPlans.add(dayPlan);
        }
        
        return dayPlans;
    }
    
    /**
     * Create a single day plan with timing and travel segments
     */
    private DayPlan createDayPlan(Trip trip, Integer day, List<PlannedPlace> places) {
        DayPlan dayPlan = new DayPlan();
        dayPlan.setDayNumber(day);
        dayPlan.setDate(trip.getStartDate().plusDays(day - 1));
        dayPlan.setBaseCity(trip.getBaseCity());
        dayPlan.setStartTime(LocalTime.of(9, 0)); // Start at 9 AM
        
        // Create activities and travel segments
        List<PlannedActivity> activities = new ArrayList<>();
        List<TravelSegment> travelSegments = new ArrayList<>();
        
        LocalTime currentTime = dayPlan.getStartTime();
        int totalTravelTime = 0;
        int totalVisitTime = 0;
        
        for (int i = 0; i < places.size(); i++) {
            PlannedPlace place = places.get(i);
            
            // Add travel time if not the first place
            if (i > 0) {
                PlannedPlace previousPlace = places.get(i - 1);
                TravelSegment segment = travelTimeService.calculateTravelTime(
                        previousPlace.getLatitude(), previousPlace.getLongitude(),
                        place.getLatitude(), place.getLongitude());
                
                segment.setFromPlaceId(previousPlace.getPlaceId());
                segment.setToPlaceId(place.getPlaceId());
                segment.setFromPlaceName(previousPlace.getName());
                segment.setToPlaceName(place.getName());
                
                travelSegments.add(segment);
                currentTime = currentTime.plusMinutes(segment.getDurationMinutes());
                totalTravelTime += segment.getDurationMinutes();
            }
            
            // Create activity for this place
            PlannedActivity activity = new PlannedActivity();
            activity.setActivityId(UUID.randomUUID().toString());
            activity.setPlace(place);
            activity.setStartTime(currentTime);
            activity.setType(PlannedActivity.ActivityType.VISIT);
            
            int visitDuration = place.getEstimatedVisitDurationMinutes() != null ? 
                    place.getEstimatedVisitDurationMinutes() : 120;
            activity.setDurationMinutes(visitDuration);
            activity.setEndTime(currentTime.plusMinutes(visitDuration));
            
            activities.add(activity);
            currentTime = currentTime.plusMinutes(visitDuration);
            totalVisitTime += visitDuration;
            
            // Update place timing
            place.setSuggestedArrivalTime(activity.getStartTime());
            place.setSuggestedDepartureTime(activity.getEndTime());
        }
        
        dayPlan.setActivities(activities);
        dayPlan.setTravelSegments(travelSegments);
        dayPlan.setEndTime(currentTime);
        dayPlan.setTotalTravelTimeMinutes(totalTravelTime);
        dayPlan.setTotalVisitTimeMinutes(totalVisitTime);
        dayPlan.setTotalActivities(activities.size());
        
        // Assess pacing
        int totalDayMinutes = totalTravelTime + totalVisitTime;
        dayPlan.setPacingAssessment(assessPacing(totalDayMinutes, activities.size()));
        
        // Add day tips and warnings
        dayPlan.setDayTips(generateDayTips(dayPlan));
        dayPlan.setWarnings(generateDayWarnings(dayPlan));
        
        return dayPlan;
    }
    
    /**
     * Assess the pacing of a day
     */
    private String assessPacing(int totalMinutes, int activityCount) {
        if (totalMinutes <= 360) { // 6 hours or less
            return "Relaxed";
        } else if (totalMinutes <= 480) { // 8 hours or less
            return "Perfect";
        } else if (totalMinutes <= 600) { // 10 hours or less
            return "Busy";
        } else {
            return "Overpacked";
        }
    }
    
    /**
     * Generate tips for the day
     */
    private List<String> generateDayTips(DayPlan dayPlan) {
        List<String> tips = new ArrayList<>();
        
        if (dayPlan.getTotalTravelTimeMinutes() > 120) {
            tips.add("Consider booking transportation in advance due to longer travel times");
        }
        
        if (dayPlan.getActivities().size() > 4) {
            tips.add("Pack snacks and water for this busy day");
        }
        
        if ("Overpacked".equals(dayPlan.getPacingAssessment())) {
            tips.add("Consider moving some activities to another day for a more relaxed experience");
        }
        
        return tips;
    }
    
    /**
     * Generate warnings for the day
     */
    private List<String> generateDayWarnings(DayPlan dayPlan) {
        List<String> warnings = new ArrayList<>();
        
        if (dayPlan.getEndTime().isAfter(LocalTime.of(19, 0))) {
            warnings.add("Day ends quite late - consider restaurant reservations");
        }
        
        if ("Overpacked".equals(dayPlan.getPacingAssessment())) {
            warnings.add("This day might be too busy - consider reducing activities");
        }
        
        return warnings;
    }
    
    /**
     * Calculate distance between two points
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
}
