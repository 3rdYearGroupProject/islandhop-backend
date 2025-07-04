package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.dto.AddPlaceToDayRequest;
import com.islandhop.tripplanning.dto.ContextualSuggestionsResponse;
import com.islandhop.tripplanning.model.PlannedPlace;
import com.islandhop.tripplanning.model.Trip;
import com.islandhop.tripplanning.service.external.TripAdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating contextual, location-aware recommendations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContextualRecommendationService {
    
    private final LocationService locationService;
    private final TripAdvisorService tripAdvisorService;
    private final TravelTimeService travelTimeService;
    
    /**
     * Generate contextual suggestions based on user's current trip state
     */
    public ContextualSuggestionsResponse generateContextualSuggestions(
            Trip trip, Integer dayNumber, String contextType) {
        
        log.info("Generating contextual suggestions for trip {} day {} (context: {})", 
                trip.getTripId(), dayNumber, contextType);
        
        ContextualSuggestionsResponse response = new ContextualSuggestionsResponse();
        response.setDayNumber(dayNumber);
        response.setContextType(contextType);
        
        // Get current context
        PlannedPlace lastAddedPlace = getLastAddedPlaceForDay(trip, dayNumber);
        ContextualSuggestionsResponse.TravelContext travelContext = 
            buildTravelContext(trip, dayNumber, lastAddedPlace);
        
        response.setLastAddedPlace(lastAddedPlace);
        response.setTravelContext(travelContext);
        
        // Generate categorized suggestions based on context
        if ("initial".equals(contextType)) {
            // First suggestions when planning a day
            generateInitialDaySuggestions(response, trip, dayNumber);
        } else if ("next_place".equals(contextType)) {
            // Suggestions after adding a place
            generateNextPlaceSuggestions(response, trip, dayNumber, lastAddedPlace);
        }
        
        // Add insights and tips
        response.setInsights(generateInsights(trip, dayNumber, travelContext));
        response.setWarnings(generateWarnings(trip, dayNumber, travelContext));
        response.setTips(generateTips(trip, dayNumber, travelContext));
        
        return response;
    }
    
    /**
     * Generate suggestions for places near a specific location
     */
    public List<ContextualSuggestionsResponse.PlaceSuggestion> getSuggestionsNearPlace(
            PlannedPlace referencePlace, List<String> categories, 
            PlannedPlace.PlaceType placeType, int maxResults) {
        
        log.info("Getting suggestions near {} for type {} with categories {}", 
                referencePlace.getName(), placeType, categories);
        
        List<ContextualSuggestionsResponse.PlaceSuggestion> suggestions = new ArrayList<>();
        
        if (referencePlace.getLatitude() != null && referencePlace.getLongitude() != null) {
            // Search for places based on type
            List<PlannedPlace> places = searchPlacesByType(
                referencePlace.getLatitude(), 
                referencePlace.getLongitude(), 
                placeType, 
                20.0 // 20km radius
            );
            
            // Convert to suggestions with travel context
            for (PlannedPlace place : places) {
                ContextualSuggestionsResponse.PlaceSuggestion suggestion = 
                    convertToSuggestion(place, referencePlace);
                
                if (suggestion != null && matchesCategories(place, categories)) {
                    suggestions.add(suggestion);
                }
            }
        }
        
        // Sort by relevance and distance
        suggestions.sort(Comparator.comparing(
            ContextualSuggestionsResponse.PlaceSuggestion::getDistanceFromLastPlaceKm)
            .thenComparing(s -> -s.getRelevanceScore()));
        
        return suggestions.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    // Private helper methods
    
    private void generateInitialDaySuggestions(ContextualSuggestionsResponse response, 
                                             Trip trip, Integer dayNumber) {
        
        // For initial suggestions, recommend based on trip preferences and base city
        String baseCity = trip.getBaseCity();
        List<String> categories = trip.getCategories();
        
        // Get accommodations first if not set
        if (!hasAccommodationForDay(trip, dayNumber)) {
            List<ContextualSuggestionsResponse.PlaceSuggestion> accommodations = 
                getSuggestionsForCity(baseCity, PlannedPlace.PlaceType.HOTEL, categories, 5);
            response.setAccommodations(accommodations);
        }
        
        // Get attractions based on preferences
        List<ContextualSuggestionsResponse.PlaceSuggestion> attractions = 
            getSuggestionsForCity(baseCity, PlannedPlace.PlaceType.ATTRACTION, categories, 10);
        response.setAttractions(attractions);
        
        // Get restaurants
        List<ContextualSuggestionsResponse.PlaceSuggestion> restaurants = 
            getSuggestionsForCity(baseCity, PlannedPlace.PlaceType.RESTAURANT, categories, 5);
        response.setRestaurants(restaurants);
    }
    
    private void generateNextPlaceSuggestions(ContextualSuggestionsResponse response, 
                                            Trip trip, Integer dayNumber, 
                                            PlannedPlace lastAddedPlace) {
        
        if (lastAddedPlace == null) {
            generateInitialDaySuggestions(response, trip, dayNumber);
            return;
        }
        
        List<String> categories = trip.getCategories();
        
        // Get nearby attractions
        List<ContextualSuggestionsResponse.PlaceSuggestion> attractions = 
            getSuggestionsNearPlace(lastAddedPlace, categories, PlannedPlace.PlaceType.ATTRACTION, 8);
        response.setAttractions(attractions);
        
        // Get nearby restaurants
        List<ContextualSuggestionsResponse.PlaceSuggestion> restaurants = 
            getSuggestionsNearPlace(lastAddedPlace, categories, PlannedPlace.PlaceType.RESTAURANT, 5);
        response.setRestaurants(restaurants);
        
        // Get activities based on the area
        List<ContextualSuggestionsResponse.PlaceSuggestion> activities = 
            getSuggestionsNearPlace(lastAddedPlace, categories, PlannedPlace.PlaceType.ATTRACTION, 6);
        response.setActivities(activities);
    }
    
    private PlannedPlace getLastAddedPlaceForDay(Trip trip, Integer dayNumber) {
        return trip.getPlaces().stream()
                .filter(place -> dayNumber.equals(place.getDayNumber()))
                .max(Comparator.comparing(place -> place.getPlaceId())) // Assuming newer IDs are "larger"
                .orElse(null);
    }
    
    private ContextualSuggestionsResponse.TravelContext buildTravelContext(
            Trip trip, Integer dayNumber, PlannedPlace lastAddedPlace) {
        
        ContextualSuggestionsResponse.TravelContext context = 
            new ContextualSuggestionsResponse.TravelContext();
        
        if (lastAddedPlace != null) {
            context.setCurrentCity(lastAddedPlace.getCity());
            context.setCurrentLatitude(lastAddedPlace.getLatitude());
            context.setCurrentLongitude(lastAddedPlace.getLongitude());
            context.setCurrentProvince(extractProvince(lastAddedPlace.getCity()));
        } else {
            context.setCurrentCity(trip.getBaseCity());
            context.setCurrentProvince(extractProvince(trip.getBaseCity()));
        }
        
        // Count places for today
        long placesToday = trip.getPlaces().stream()
                .filter(place -> dayNumber.equals(place.getDayNumber()))
                .count();
        context.setTotalPlacesToday((int) placesToday);
        
        // Estimate remaining time (assuming 8-hour day)
        int estimatedUsedTime = trip.getPlaces().stream()
                .filter(place -> dayNumber.equals(place.getDayNumber()))
                .mapToInt(place -> place.getEstimatedVisitDurationMinutes() != null ? 
                    place.getEstimatedVisitDurationMinutes() : 120)
                .sum();
        
        context.setRemainingTimeToday(Math.max(0, 480 - estimatedUsedTime)); // 8 hours = 480 minutes
        
        return context;
    }
    
    private List<ContextualSuggestionsResponse.PlaceSuggestion> getSuggestionsForCity(
            String city, PlannedPlace.PlaceType placeType, List<String> categories, int maxResults) {
        
        List<ContextualSuggestionsResponse.PlaceSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Search using location service
            List<LocationService.LocationSearchResult> searchResults = 
                locationService.searchLocations(
                    placeType.toString().toLowerCase(), city, null, null, maxResults * 2);
            
            for (LocationService.LocationSearchResult result : searchResults) {
                ContextualSuggestionsResponse.PlaceSuggestion suggestion = 
                    convertLocationResultToSuggestion(result, null);
                if (suggestion != null) {
                    suggestions.add(suggestion);
                }
            }
        } catch (Exception e) {
            log.error("Error getting suggestions for city {}: {}", city, e.getMessage());
        }
        
        return suggestions.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    private List<PlannedPlace> searchPlacesByType(double latitude, double longitude, 
                                                 PlannedPlace.PlaceType placeType, double radiusKm) {
        
        try {
            switch (placeType) {
                case ATTRACTION:
                    return tripAdvisorService.searchAttractions(latitude, longitude, radiusKm);
                case HOTEL:
                    return tripAdvisorService.searchHotels(latitude, longitude, radiusKm);
                case RESTAURANT:
                    return tripAdvisorService.searchRestaurants(latitude, longitude, radiusKm);
                default:
                    return tripAdvisorService.searchAttractions(latitude, longitude, radiusKm);
            }
        } catch (Exception e) {
            log.error("Error searching places by type: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private ContextualSuggestionsResponse.PlaceSuggestion convertToSuggestion(
            PlannedPlace place, PlannedPlace referencePlace) {
        
        ContextualSuggestionsResponse.PlaceSuggestion suggestion = 
            new ContextualSuggestionsResponse.PlaceSuggestion();
        
        suggestion.setPlaceId(place.getPlaceId());
        suggestion.setName(place.getName());
        suggestion.setDescription(place.getDescription());
        suggestion.setCity(place.getCity());
        suggestion.setLatitude(place.getLatitude());
        suggestion.setLongitude(place.getLongitude());
        suggestion.setPlaceType(place.getType());
        suggestion.setCategories(place.getCategories());
        suggestion.setEstimatedVisitDurationMinutes(place.getEstimatedVisitDurationMinutes());
        
        // Calculate travel context if reference place provided
        if (referencePlace != null && referencePlace.getLatitude() != null && 
            place.getLatitude() != null) {
            
            double distance = calculateDistance(
                referencePlace.getLatitude(), referencePlace.getLongitude(),
                place.getLatitude(), place.getLongitude());
            
            suggestion.setDistanceFromLastPlaceKm(distance);
            suggestion.setTravelTimeFromLastPlaceMinutes(
                travelTimeService.estimateTravelTime(
                    referencePlace.getLatitude(), referencePlace.getLongitude(),
                    place.getLatitude(), place.getLongitude()));
            suggestion.setTravelMode("driving");
        }
        
        // Set relevance score (simplified)
        suggestion.setRelevanceScore(0.8); // Default high relevance
        suggestion.setReasonForSuggestion("Based on your preferences and location");
        
        return suggestion;
    }
    
    private ContextualSuggestionsResponse.PlaceSuggestion convertLocationResultToSuggestion(
            LocationService.LocationSearchResult result, PlannedPlace referencePlace) {
        
        ContextualSuggestionsResponse.PlaceSuggestion suggestion = 
            new ContextualSuggestionsResponse.PlaceSuggestion();
        
        suggestion.setPlaceId(result.getPlaceId());
        suggestion.setName(result.getName());
        suggestion.setLatitude(result.getLatitude());
        suggestion.setLongitude(result.getLongitude());
        suggestion.setRating(result.getRating());
        suggestion.setCategories(result.getTypes());
        suggestion.setRelevanceScore(0.7);
        suggestion.setReasonForSuggestion("Popular destination in " + extractCityFromAddress(result.getFormattedAddress()));
        
        return suggestion;
    }
    
    private boolean hasAccommodationForDay(Trip trip, Integer dayNumber) {
        return trip.getPlaces().stream()
                .anyMatch(place -> dayNumber.equals(place.getDayNumber()) && 
                          PlannedPlace.PlaceType.HOTEL.equals(place.getType()));
    }
    
    private boolean matchesCategories(PlannedPlace place, List<String> categories) {
        if (place.getCategories() == null || categories == null) {
            return true;
        }
        
        return place.getCategories().stream()
                .anyMatch(category -> categories.stream()
                    .anyMatch(tripCategory -> category.toLowerCase().contains(tripCategory.toLowerCase())));
    }
    
    private List<String> generateInsights(Trip trip, Integer dayNumber, 
                                        ContextualSuggestionsResponse.TravelContext context) {
        List<String> insights = new ArrayList<>();
        
        if (context.getTotalPlacesToday() == 0) {
            insights.add("Start your day by selecting accommodation if you haven't already");
            insights.add("Based on your preferences, we recommend exploring " + 
                        trip.getCategories().get(0).toLowerCase() + " attractions");
        } else if (context.getTotalPlacesToday() >= 4) {
            insights.add("You have quite a packed day! Consider the travel time between places");
        }
        
        if (context.getRemainingTimeToday() != null && context.getRemainingTimeToday() < 120) {
            insights.add("Limited time remaining today. Consider quick visits or nearby restaurants");
        }
        
        return insights;
    }
    
    private List<String> generateWarnings(Trip trip, Integer dayNumber, 
                                        ContextualSuggestionsResponse.TravelContext context) {
        List<String> warnings = new ArrayList<>();
        
        if (context.getTotalPlacesToday() > 5) {
            warnings.add("You might be planning too many places for one day");
        }
        
        return warnings;
    }
    
    private List<String> generateTips(Trip trip, Integer dayNumber, 
                                    ContextualSuggestionsResponse.TravelContext context) {
        List<String> tips = new ArrayList<>();
        
        tips.add("Book popular attractions in advance to avoid disappointment");
        tips.add("Consider traffic and weather when planning your route");
        
        if (context.getCurrentProvince() != null) {
            tips.add("Explore local cuisine unique to " + context.getCurrentProvince() + " province");
        }
        
        return tips;
    }
    
    // Utility methods
    
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
    
    private String extractProvince(String city) {
        if (city == null) return "Unknown";
        
        // Simple province mapping for Sri Lankan cities
        String cityLower = city.toLowerCase();
        if (cityLower.contains("colombo") || cityLower.contains("gampaha") || cityLower.contains("kalutara")) {
            return "Western";
        } else if (cityLower.contains("kandy") || cityLower.contains("matale") || cityLower.contains("nuwara eliya")) {
            return "Central";
        } else if (cityLower.contains("galle") || cityLower.contains("matara") || cityLower.contains("hambantota")) {
            return "Southern";
        } else if (cityLower.contains("anuradhapura") || cityLower.contains("polonnaruwa")) {
            return "North Central";
        }
        return "Unknown";
    }
    
    private String extractCityFromAddress(String address) {
        if (address == null) return "Sri Lanka";
        
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return "Sri Lanka";
    }
}
