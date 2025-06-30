package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.dto.*;
import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.repository.TripRepository;
import com.islandhop.tripplanning.service.recommendation.RecommendationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPlanningService {
    
    private final TripRepository tripRepository;
    private final RecommendationEngine recommendationEngine;
    private final PlaceService placeService;
    private final TravelTimeService travelTimeService;
    private final RouteOptimizationService routeOptimizationService;
    private final ContextualRecommendationService contextualRecommendationService;
    
    /**
     * Create a new trip with user preferences
     */
    public Trip createTrip(CreateTripRequest request, String userId) {
        log.info("Creating new trip for user: {}", userId);
        
        Trip trip = new Trip();
        trip.setTripId(UUID.randomUUID().toString());
        trip.setUserId(userId);
        trip.setTripName(request.getTripName() != null ? request.getTripName() : 
                        "Trip to " + request.getBaseCity());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setArrivalTime(request.getArrivalTime());
        trip.setBaseCity(request.getBaseCity());
        trip.setMultiCity(request.isMultiCity());
        trip.setCategories(request.getCategories());
        trip.setPacing(request.getPacing());
        trip.setStatus(Trip.TripStatus.PLANNING);
        trip.setPlaces(new ArrayList<>());
        trip.setDayPlans(new ArrayList<>());
        trip.setExcludedAttractions(new ArrayList<>());
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        
        // Initialize preferences map
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("maxDailyTravelHours", 8);
        preferences.put("bufferTimeMinutes", 30);
        preferences.put("maxAttractionsPerDay", 4);
        trip.setPreferences(preferences);
        
        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created successfully with ID: {}", savedTrip.getTripId());
        
        return savedTrip;
    }
    
    /**
     * Add a place to an existing trip
     */
    public Trip addPlaceToTrip(String tripId, AddPlaceRequest request, String userId) {
        log.info("Adding place {} to trip {} for user {}", request.getPlaceName(), tripId, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Create planned place
        PlannedPlace place = placeService.createPlannedPlace(request);
        place.setUserAdded(true);
        
        // Add to trip
        trip.getPlaces().add(place);
        trip.setUpdatedAt(LocalDateTime.now());
        
        Trip savedTrip = tripRepository.save(trip);
        log.info("Place added successfully to trip {}", tripId);
        
        return savedTrip;
    }
    
    /**
     * Generate AI-powered suggestions
     */
    public SuggestionsResponse generateSuggestions(String tripId, Integer day, String userId) {
        log.info("Generating suggestions for trip {} day {} for user {}", tripId, day, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Generate recommendations using the hybrid algorithm
        List<Recommendation> attractions = recommendationEngine.recommendAttractions(trip, day);
        List<Recommendation> hotels = recommendationEngine.recommendHotels(trip, day);
        List<Recommendation> restaurants = recommendationEngine.recommendRestaurants(trip, day);
        
        // Generate insights and warnings
        List<String> insights = generateInsights(trip, attractions);
        List<String> warnings = generateWarnings(trip, day);
        
        String message = generateSuggestionsMessage(trip, day, attractions.size());
        
        return new SuggestionsResponse(tripId, attractions, hotels, restaurants, 
                                     insights, warnings, message);
    }
    
    /**
     * Optimize visiting order using travel time and constraints
     */
    public Trip optimizeVisitingOrder(String tripId, String userId) {
        log.info("Optimizing visiting order for trip {} for user {}", tripId, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Use route optimization service
        Trip optimizedTrip = routeOptimizationService.optimizeRoute(trip);
        optimizedTrip.setUpdatedAt(LocalDateTime.now());
        
        Trip savedTrip = tripRepository.save(optimizedTrip);
        log.info("Trip order optimized successfully for {}", tripId);
        
        return savedTrip;
    }
    
    /**
     * Get detailed day plan
     */
    public DayPlan getDayPlan(String tripId, Integer day, String userId) {
        log.info("Getting day plan for trip {} day {} for user {}", tripId, day, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Find or create day plan
        DayPlan dayPlan = trip.getDayPlans().stream()
                .filter(dp -> dp.getDayNumber().equals(day))
                .findFirst()
                .orElse(createDayPlan(trip, day));
        
        return dayPlan;
    }
    
    /**
     * Get trip summary
     */
    public Trip getTripSummary(String tripId, String userId) {
        log.info("Getting trip summary for {} for user {}", tripId, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Calculate and update statistics if needed
        if (trip.getStatistics() == null) {
            trip.setStatistics(calculateTripStatistics(trip));
            tripRepository.save(trip);
        }
        
        return trip;
    }
    
    /**
     * Get map data for trip visualization
     */
    public Map<String, Object> getMapData(String tripId, String userId) {
        log.info("Getting map data for trip {} for user {}", tripId, userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        Map<String, Object> mapData = new HashMap<>();
        List<Map<String, Object>> points = new ArrayList<>();
        List<Map<String, Object>> routes = new ArrayList<>();
        
        // Add all places as points
        for (PlannedPlace place : trip.getPlaces()) {
            Map<String, Object> point = new HashMap<>();
            point.put("id", place.getPlaceId());
            point.put("name", place.getName());
            point.put("latitude", place.getLatitude());
            point.put("longitude", place.getLongitude());
            point.put("type", place.getType());
            point.put("day", place.getDayNumber());
            points.add(point);
        }
        
        // Add routes between places
        for (DayPlan dayPlan : trip.getDayPlans()) {
            for (TravelSegment segment : dayPlan.getTravelSegments()) {
                Map<String, Object> route = new HashMap<>();
                route.put("from", segment.getFromPlaceName());
                route.put("to", segment.getToPlaceName());
                route.put("distance", segment.getDistance());
                route.put("duration", segment.getDurationMinutes());
                route.put("mode", segment.getTravelMode());
                routes.add(route);
            }
        }
        
        mapData.put("points", points);
        mapData.put("routes", routes);
        mapData.put("center", calculateMapCenter(trip.getPlaces()));
        
        return mapData;
    }
    
    /**
     * Get user's trips
     */
    public List<Trip> getUserTrips(String userId) {
        log.info("Getting trips for user: {}", userId);
        return tripRepository.findByUserId(userId);
    }
    
    /**
     * Add a place to a specific day with enhanced contextual information
     */
    public Trip addPlaceToSpecificDay(String tripId, AddPlaceToDayRequest request, String userId) {
        log.info("Adding place {} to trip {} day {} for user {}", 
                request.getPlaceName(), tripId, request.getDayNumber(), userId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Validate and enrich the place using LocationService
        AddPlaceRequest basicRequest = new AddPlaceRequest();
        basicRequest.setPlaceName(request.getPlaceName());
        basicRequest.setCity(request.getCity());
        basicRequest.setDescription(request.getDescription());
        basicRequest.setLatitude(request.getLatitude());
        basicRequest.setLongitude(request.getLongitude());
        
        LocationService.PlaceValidationResult validation = 
            locationService.validateAndEnrichPlace(basicRequest);
        
        // Create planned place with enhanced information
        PlannedPlace place = new PlannedPlace();
        place.setPlaceId(UUID.randomUUID().toString());
        place.setName(request.getPlaceName());
        place.setCity(request.getCity());
        place.setDescription(request.getDescription());
        place.setType(request.getPlaceType());
        place.setDayNumber(request.getDayNumber());
        place.setUserAdded(true);
        place.setConfirmed(validation.isValid());
        
        // Use validated coordinates if available
        if (validation.isValid() && validation.getSuggestedLatitude() != null) {
            place.setLatitude(validation.getSuggestedLatitude());
            place.setLongitude(validation.getSuggestedLongitude());
        } else {
            place.setLatitude(request.getLatitude());
            place.setLongitude(request.getLongitude());
        }
        
        // Set visit duration
        place.setEstimatedVisitDurationMinutes(
            request.getEstimatedVisitDurationMinutes() != null ? 
            request.getEstimatedVisitDurationMinutes() : 
            getDefaultVisitDuration(request.getPlaceType()));
        
        // Calculate travel time from previous place if available
        if (request.getPreviousPlaceId() != null) {
            PlannedPlace previousPlace = findPlaceById(trip, request.getPreviousPlaceId());
            if (previousPlace != null && previousPlace.getLatitude() != null && place.getLatitude() != null) {
                Integer travelTime = travelTimeService.estimateTravelTime(
                    previousPlace.getLatitude(), previousPlace.getLongitude(),
                    place.getLatitude(), place.getLongitude());
                // You could store this in place metadata or trip statistics
            }
        }
        
        // Add to trip
        trip.getPlaces().add(place);
        trip.setUpdatedAt(LocalDateTime.now());
        
        Trip savedTrip = tripRepository.save(trip);
        log.info("Place added successfully to trip {} day {}", tripId, request.getDayNumber());
        
        return savedTrip;
    }
    
    /**
     * Get contextual suggestions based on trip state and user preferences
     */
    public ContextualSuggestionsResponse getContextualSuggestions(String tripId, Integer dayNumber, 
                                                                String contextType, String userId) {
        log.info("Getting contextual suggestions for trip {} day {} (context: {})", tripId, dayNumber, contextType);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        return contextualRecommendationService.generateContextualSuggestions(trip, dayNumber, contextType);
    }
    
    /**
     * Get suggestions near a specific place
     */
    public List<ContextualSuggestionsResponse.PlaceSuggestion> getNearbySuggestions(
            String tripId, String placeId, String placeType, Integer maxResults, String userId) {
        
        log.info("Getting nearby suggestions for place {} in trip {}", placeId, tripId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        PlannedPlace referencePlace = findPlaceById(trip, placeId);
        
        if (referencePlace == null) {
            throw new IllegalArgumentException("Place not found: " + placeId);
        }
        
        PlannedPlace.PlaceType type = placeType != null ? 
            PlannedPlace.PlaceType.valueOf(placeType.toUpperCase()) : 
            PlannedPlace.PlaceType.ATTRACTION;
        
        return contextualRecommendationService.getSuggestionsNearPlace(
            referencePlace, trip.getCategories(), type, maxResults);
    }
    
    /**
     * Perform contextual location search with trip preferences
     */
    public LocationSearchResponse contextualLocationSearch(String tripId, String query, String placeType, 
                                                         Integer dayNumber, String lastPlaceId, 
                                                         Integer maxResults, String userId) {
        
        log.info("Performing contextual search for trip {} with query: {}", tripId, query);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        // Get bias location from last place or trip base city
        Double biasLat = null;
        Double biasLng = null;
        
        if (lastPlaceId != null) {
            PlannedPlace lastPlace = findPlaceById(trip, lastPlaceId);
            if (lastPlace != null) {
                biasLat = lastPlace.getLatitude();
                biasLng = lastPlace.getLongitude();
            }
        }
        
        // Perform search with trip context
        List<LocationService.LocationSearchResult> results = 
            locationService.searchLocations(query, trip.getBaseCity(), biasLat, biasLng, maxResults);
        
        // Build response with metadata
        LocationSearchResponse response = new LocationSearchResponse();
        response.setQuery(query);
        response.setResults(results);
        response.setTotalResults(results.size());
        
        LocationSearchResponse.SearchMetadata metadata = new LocationSearchResponse.SearchMetadata();
        metadata.setBiasLatitude(biasLat);
        metadata.setBiasLongitude(biasLng);
        metadata.setSearchSource("hybrid");
        metadata.setSriLankaFiltered(true);
        response.setMetadata(metadata);
        
        return response;
    }
    
    /**
     * Get travel information between two places
     */
    public Map<String, Object> getTravelInfo(String tripId, String fromPlaceId, String toPlaceId, String userId) {
        log.info("Getting travel info from {} to {} for trip {}", fromPlaceId, toPlaceId, tripId);
        
        Trip trip = getTripByIdAndUserId(tripId, userId);
        
        PlannedPlace fromPlace = findPlaceById(trip, fromPlaceId);
        PlannedPlace toPlace = findPlaceById(trip, toPlaceId);
        
        if (fromPlace == null || toPlace == null) {
            throw new IllegalArgumentException("One or both places not found");
        }
        
        if (fromPlace.getLatitude() == null || toPlace.getLatitude() == null) {
            throw new IllegalArgumentException("Place coordinates not available");
        }
        
        // Calculate travel time and distance using TravelTimeService
        Integer travelTimeMinutes = travelTimeService.estimateTravelTime(
            fromPlace.getLatitude(), fromPlace.getLongitude(),
            toPlace.getLatitude(), toPlace.getLongitude());
        
        double distance = calculateDistance(
            fromPlace.getLatitude(), fromPlace.getLongitude(),
            toPlace.getLatitude(), toPlace.getLongitude());
        
        Map<String, Object> travelInfo = new HashMap<>();
        travelInfo.put("fromPlace", Map.of(
            "id", fromPlace.getPlaceId(),
            "name", fromPlace.getName(),
            "city", fromPlace.getCity()
        ));
        travelInfo.put("toPlace", Map.of(
            "id", toPlace.getPlaceId(),
            "name", toPlace.getName(),
            "city", toPlace.getCity()
        ));
        travelInfo.put("distanceKm", Math.round(distance * 100.0) / 100.0);
        travelInfo.put("travelTimeMinutes", travelTimeMinutes);
        travelInfo.put("travelMode", "driving");
        
        // Add travel insights
        List<String> insights = new ArrayList<>();
        if (distance > 100) {
            insights.add("This is a long distance trip. Consider breaking it into multiple days.");
        }
        if (travelTimeMinutes > 180) {
            insights.add("Travel time is over 3 hours. Plan for rest stops.");
        }
        if (!fromPlace.getCity().equals(toPlace.getCity())) {
            insights.add("You're traveling between different cities. Check local transport options.");
        }
        
        travelInfo.put("insights", insights);
        
        return travelInfo;
    }
    
    // Helper methods
    
    private Trip getTripByIdAndUserId(String tripId, String userId) {
        return tripRepository.findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found or access denied"));
    }
    
    private DayPlan createDayPlan(Trip trip, Integer day) {
        DayPlan dayPlan = new DayPlan();
        dayPlan.setDayNumber(day);
        dayPlan.setDate(trip.getStartDate().plusDays(day - 1));
        dayPlan.setBaseCity(trip.getBaseCity());
        dayPlan.setActivities(new ArrayList<>());
        dayPlan.setTravelSegments(new ArrayList<>());
        dayPlan.setSuggestedHotels(new ArrayList<>());
        dayPlan.setSuggestedRestaurants(new ArrayList<>());
        dayPlan.setDayTips(new ArrayList<>());
        dayPlan.setWarnings(new ArrayList<>());
        
        // Add to trip
        trip.getDayPlans().add(dayPlan);
        
        return dayPlan;
    }
    
    private List<String> generateInsights(Trip trip, List<Recommendation> attractions) {
        List<String> insights = new ArrayList<>();
        
        if (attractions.isEmpty()) {
            insights.add("No attractions found matching your preferences. Try expanding your search criteria.");
        } else {
            insights.add(String.format("Found %d attractions matching your interests in %s", 
                    attractions.size(), trip.getCategories()));
        }
        
        if (trip.getPacing() == Trip.ActivityPacing.RELAXED) {
            insights.add("With relaxed pacing, we recommend 2-3 attractions per day with longer visit times.");
        } else if (trip.getPacing() == Trip.ActivityPacing.ACTIVE) {
            insights.add("Active pacing allows for 4-5 attractions per day. Consider early starts!");
        }
        
        return insights;
    }
    
    private List<String> generateWarnings(Trip trip, Integer day) {
        List<String> warnings = new ArrayList<>();
        
        // Add day-specific warnings
        if (day != null && day == 1 && trip.getArrivalTime() != null && 
            trip.getArrivalTime().isAfter(java.time.LocalTime.of(20, 0))) {
            warnings.add("Late arrival on Day 1 - consider light activities or rest.");
        }
        
        return warnings;
    }
    
    private String generateSuggestionsMessage(Trip trip, Integer day, int attractionCount) {
        if (day == null) {
            return String.format("Found %d recommendations for your %d-day trip to %s", 
                    attractionCount, trip.getEndDate().toEpochDay() - trip.getStartDate().toEpochDay() + 1, 
                    trip.getBaseCity());
        } else {
            return String.format("Found %d recommendations for Day %d in %s", 
                    attractionCount, day, trip.getBaseCity());
        }
    }
    
    private TripStatistics calculateTripStatistics(Trip trip) {
        TripStatistics stats = new TripStatistics();
        stats.setTotalDays((int) (trip.getEndDate().toEpochDay() - trip.getStartDate().toEpochDay() + 1));
        stats.setTotalPlaces(trip.getPlaces().size());
        
        // Calculate other statistics
        double totalDistance = trip.getDayPlans().stream()
                .flatMap(dp -> dp.getTravelSegments().stream())
                .mapToDouble(TravelSegment::getDistance)
                .sum();
        stats.setTotalDistanceKm(totalDistance);
        
        int totalTravelTime = trip.getDayPlans().stream()
                .mapToInt(DayPlan::getTotalTravelTimeMinutes)
                .sum();
        stats.setTotalTravelTimeMinutes(totalTravelTime);
        
        // Set predominant category
        Map<String, Long> categoryCount = new HashMap<>();
        for (String category : trip.getCategories()) {
            categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
        }
        stats.setPredominantCategory(categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Mixed"));
        
        return stats;
    }
    
    private Map<String, Double> calculateMapCenter(List<PlannedPlace> places) {
        if (places.isEmpty()) {
            return Map.of("latitude", 7.8731, "longitude", 80.7718); // Default to Sri Lanka center
        }
        
        double avgLat = places.stream().mapToDouble(PlannedPlace::getLatitude).average().orElse(0.0);
        double avgLng = places.stream().mapToDouble(PlannedPlace::getLongitude).average().orElse(0.0);
        
        return Map.of("latitude", avgLat, "longitude", avgLng);
    }
    
    private PlannedPlace findPlaceById(Trip trip, String placeId) {
        return trip.getPlaces().stream()
                .filter(place -> placeId.equals(place.getPlaceId()))
                .findFirst()
                .orElse(null);
    }
    
    private Integer getDefaultVisitDuration(PlannedPlace.PlaceType placeType) {
        switch (placeType) {
            case ATTRACTION:
                return 120; // 2 hours
            case HOTEL:
                return 60;  // 1 hour (check-in/out)
            case RESTAURANT:
                return 90;  // 1.5 hours
            case SHOPPING:
                return 150; // 2.5 hours
            case VIEWPOINT:
                return 45;  // 45 minutes
            case TRANSPORT_HUB:
                return 30;  // 30 minutes
            default:
                return 90;  // 1.5 hours
        }
    }
    
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
     * Get trips by date range (for pooling service integration)
     */
    public List<Trip> getTripsByDateRange(String startDate, String endDate) {
        log.info("Fetching trips in date range: {} to {}", startDate, endDate);
        try {
            return tripRepository.findTripsInDateRange(
                java.time.LocalDate.parse(startDate),
                java.time.LocalDate.parse(endDate)
            );
        } catch (Exception e) {
            log.error("Error fetching trips by date range: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get trips by base city (for pooling service integration)
     */
    public List<Trip> getTripsByBaseCity(String baseCity) {
        log.info("Fetching trips for base city: {}", baseCity);
        try {
            return tripRepository.findByBaseCity(baseCity);
        } catch (Exception e) {
            log.error("Error fetching trips by base city: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
