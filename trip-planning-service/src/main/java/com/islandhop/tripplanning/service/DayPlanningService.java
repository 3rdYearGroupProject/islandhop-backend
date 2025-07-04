package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.dto.DayPlanResponse;
import com.islandhop.tripplanning.model.Trip;
import com.islandhop.tripplanning.model.PlannedPlace;
import com.islandhop.tripplanning.service.external.TripAdvisorService;
import com.islandhop.tripplanning.service.external.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced day planning service with TripAdvisor + Google integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DayPlanningService {
    
    private final TripAdvisorService tripAdvisorService;
    private final GooglePlacesService googlePlacesService;
    private final TravelTimeService travelTimeService;
    private final PlaceCategoryService placeCategoryService;
    private final LocationService locationService;
    
    /**
     * Get comprehensive day plan with inline suggestions (no navigation)
     */
    public DayPlanResponse getDayPlanWithSuggestions(Trip trip, Integer dayNumber) {
        log.info("Getting day plan with suggestions for trip {} day {}", trip.getTripId(), dayNumber);
        
        DayPlanResponse response = new DayPlanResponse();
        response.setTripId(trip.getTripId());
        response.setDayNumber(dayNumber);
        
        // Calculate actual date
        LocalDate dayDate = trip.getStartDate().plusDays(dayNumber - 1);
        response.setDayDate(dayDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        response.setDayName("Day " + dayNumber + " - " + dayDate.getDayOfWeek().toString());
        
        // Get existing places for this day
        List<PlannedPlace> dayPlaces = trip.getPlaces().stream()
                .filter(place -> dayNumber.equals(place.getDayNumber()))
                .sorted(Comparator.comparing(place -> place.getPlaceId())) // Simple ordering
                .collect(Collectors.toList());
        
        response.setPlaces(convertToDayPlaces(dayPlaces));
        
        // Generate quick suggestions based on current state
        response.setQuickSuggestions(generateQuickSuggestions(trip, dayNumber, dayPlaces));
        
        // Build day context
        response.setContext(buildDayContext(trip, dayNumber, dayPlaces));
        
        return response;
    }
    
    /**
     * Generate real-time suggestions without search (proximity + preferences)
     */
    public List<DayPlanResponse.QuickSuggestion> generateRealtimeSuggestions(
            Trip trip, Integer dayNumber, String lastPlaceId, String categoryFilter) {
        
        log.info("Generating realtime suggestions for trip {} day {} after place {}", 
                trip.getTripId(), dayNumber, lastPlaceId);
        
        List<DayPlanResponse.QuickSuggestion> suggestions = new ArrayList<>();
        
        // Get reference point for proximity
        PlannedPlace referencePlace = getLastPlaceForContext(trip, dayNumber, lastPlaceId);
        
        if (referencePlace != null && referencePlace.getLatitude() != null) {
            // Get suggestions from both TripAdvisor and Google
            suggestions.addAll(getTripAdvisorSuggestions(referencePlace, trip.getCategories(), categoryFilter));
            suggestions.addAll(getGooglePlacesSuggestions(referencePlace, trip.getCategories(), categoryFilter));
        } else {
            // No reference place, use trip base city
            suggestions.addAll(getBaseCitySuggestions(trip, categoryFilter));
        }
        
        // Remove duplicates and sort by relevance
        return deduplicateAndSort(suggestions, referencePlace);
    }
    
    // Private helper methods
    
    private List<DayPlanResponse.DayPlace> convertToDayPlaces(List<PlannedPlace> places) {
        List<DayPlanResponse.DayPlace> dayPlaces = new ArrayList<>();
        
        for (int i = 0; i < places.size(); i++) {
            PlannedPlace place = places.get(i);
            DayPlanResponse.DayPlace dayPlace = new DayPlanResponse.DayPlace();
            
            dayPlace.setPlaceId(place.getPlaceId());
            dayPlace.setName(place.getName());
            dayPlace.setCity(place.getCity());
            dayPlace.setPlaceType(place.getType());
            dayPlace.setLatitude(place.getLatitude());
            dayPlace.setLongitude(place.getLongitude());
            dayPlace.setVisitDurationMinutes(place.getEstimatedVisitDurationMinutes());
            dayPlace.setOrderInDay(i + 1);
            
            // Calculate travel from previous place
            if (i > 0) {
                PlannedPlace previousPlace = places.get(i - 1);
                if (previousPlace.getLatitude() != null && place.getLatitude() != null) {
                    TravelTimeService.TravelInfo travelInfo = travelTimeService.getDetailedTravelInfo(
                        previousPlace.getLatitude(), previousPlace.getLongitude(),
                        place.getLatitude(), place.getLongitude(), "driving");
                    
                    dayPlace.setTravelTimeFromPrevious(travelInfo.getDurationMinutes());
                    dayPlace.setDistanceFromPrevious(travelInfo.getDistanceKm());
                    dayPlace.setTravelMode(travelInfo.getTravelMode());
                }
            }
            
            // Set action permissions
            dayPlace.setCanEdit(true);
            dayPlace.setCanRemove(true);
            dayPlace.setCanReorder(places.size() > 1);
            
            dayPlaces.add(dayPlace);
        }
        
        return dayPlaces;
    }
    
    private List<DayPlanResponse.QuickSuggestion> generateQuickSuggestions(
            Trip trip, Integer dayNumber, List<PlannedPlace> existingPlaces) {
        
        List<DayPlanResponse.QuickSuggestion> suggestions = new ArrayList<>();
        
        // Determine what type of suggestion to prioritize
        boolean needsAccommodation = needsAccommodation(existingPlaces);
        boolean hasRestaurant = hasRestaurant(existingPlaces);
        
        if (needsAccommodation) {
            // Priority: Find accommodation
            suggestions.addAll(getAccommodationSuggestions(trip, existingPlaces));
        }
        
        if (existingPlaces.isEmpty()) {
            // First suggestions: Mix of everything based on preferences
            suggestions.addAll(getInitialSuggestions(trip, dayNumber));
        } else {
            // Contextual suggestions based on last place
            PlannedPlace lastPlace = existingPlaces.get(existingPlaces.size() - 1);
            suggestions.addAll(getContextualSuggestions(trip, lastPlace));
        }
        
        if (!hasRestaurant && suggestions.size() < 8) {
            // Add some restaurant options
            suggestions.addAll(getRestaurantSuggestions(trip, existingPlaces));
        }
        
        return suggestions.stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private List<DayPlanResponse.QuickSuggestion> getTripAdvisorSuggestions(
            PlannedPlace referencePlace, List<String> tripCategories, String categoryFilter) {
        
        List<DayPlanResponse.QuickSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Search radius: 20km around reference place
            List<PlannedPlace> tripAdvisorPlaces = new ArrayList<>();
            
            if ("restaurant".equalsIgnoreCase(categoryFilter)) {
                tripAdvisorPlaces = tripAdvisorService.searchRestaurants(
                    referencePlace.getLatitude(), referencePlace.getLongitude(), 20.0);
            } else if ("hotel".equalsIgnoreCase(categoryFilter)) {
                tripAdvisorPlaces = tripAdvisorService.searchHotels(
                    referencePlace.getLatitude(), referencePlace.getLongitude(), 20.0);
            } else {
                tripAdvisorPlaces = tripAdvisorService.searchAttractions(
                    referencePlace.getLatitude(), referencePlace.getLongitude(), 20.0);
            }
            
            for (PlannedPlace place : tripAdvisorPlaces) {
                DayPlanResponse.QuickSuggestion suggestion = new DayPlanResponse.QuickSuggestion();
                suggestion.setPlaceId(place.getPlaceId());
                suggestion.setName(place.getName());
                suggestion.setCity(place.getCity());
                suggestion.setPlaceType(place.getType());
                suggestion.setCategory(getCategoryDisplayName(place.getType()));
                suggestion.setReasonForSuggestion("Popular on TripAdvisor near " + referencePlace.getName());
                
                // Calculate travel context
                if (place.getLatitude() != null) {
                    TravelTimeService.TravelInfo travelInfo = travelTimeService.getDetailedTravelInfo(
                        referencePlace.getLatitude(), referencePlace.getLongitude(),
                        place.getLatitude(), place.getLongitude(), "driving");
                    
                    suggestion.setTravelTimeMinutes(travelInfo.getDurationMinutes());
                    suggestion.setDistanceKm(travelInfo.getDistanceKm());
                }
                
                suggestion.setQuickAddUrl("/trip/" + referencePlace.getPlaceId() + "/quick-add/" + place.getPlaceId());
                suggestions.add(suggestion);
            }
            
        } catch (Exception e) {
            log.error("Error getting TripAdvisor suggestions: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    private List<DayPlanResponse.QuickSuggestion> getGooglePlacesSuggestions(
            PlannedPlace referencePlace, List<String> tripCategories, String categoryFilter) {
        
        List<DayPlanResponse.QuickSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Use Google Places for more comprehensive local data
            List<GooglePlacesService.LocationSearchResult> googleResults = 
                googlePlacesService.searchPlacesByText(
                    categoryFilter != null ? categoryFilter : "attraction",
                    referencePlace.getLatitude(), 
                    referencePlace.getLongitude(), 
                    10);
            
            for (GooglePlacesService.LocationSearchResult result : googleResults) {
                DayPlanResponse.QuickSuggestion suggestion = new DayPlanResponse.QuickSuggestion();
                suggestion.setPlaceId(result.getPlaceId());
                suggestion.setName(result.getName());
                suggestion.setRating(result.getRating());
                
                // Categorize using our service
                PlaceCategoryService.MainCategory category = 
                    placeCategoryService.categorizeFromGoogleTypes(result.getTypes());
                suggestion.setCategory(category.getDisplayName());
                suggestion.setPlaceType(placeCategoryService.toPlannedPlaceType(category));
                
                suggestion.setReasonForSuggestion("Highly rated locally near " + referencePlace.getName());
                
                // Calculate travel context
                if (result.getLatitude() != null) {
                    TravelTimeService.TravelInfo travelInfo = travelTimeService.getDetailedTravelInfo(
                        referencePlace.getLatitude(), referencePlace.getLongitude(),
                        result.getLatitude(), result.getLongitude(), "driving");
                    
                    suggestion.setTravelTimeMinutes(travelInfo.getDurationMinutes());
                    suggestion.setDistanceKm(travelInfo.getDistanceKm());
                }
                
                suggestions.add(suggestion);
            }
            
        } catch (Exception e) {
            log.error("Error getting Google Places suggestions: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    private DayPlanResponse.DayContext buildDayContext(Trip trip, Integer dayNumber, List<PlannedPlace> dayPlaces) {
        DayPlanResponse.DayContext context = new DayPlanResponse.DayContext();
        
        context.setTotalPlaces(dayPlaces.size());
        
        int totalDuration = dayPlaces.stream()
                .mapToInt(place -> place.getEstimatedVisitDurationMinutes() != null ? 
                    place.getEstimatedVisitDurationMinutes() : 120)
                .sum();
        context.setTotalDurationMinutes(totalDuration);
        context.setRemainingTimeMinutes(Math.max(0, 480 - totalDuration)); // 8-hour day
        
        if (!dayPlaces.isEmpty()) {
            PlannedPlace lastPlace = dayPlaces.get(dayPlaces.size() - 1);
            context.setCurrentArea(lastPlace.getCity());
        } else {
            context.setCurrentArea(trip.getBaseCity());
        }
        
        // Suggest next category
        context.setSuggestedNextCategory(suggestNextCategory(dayPlaces, trip.getCategories()));
        
        // Generate insights
        List<String> insights = new ArrayList<>();
        if (dayPlaces.isEmpty()) {
            insights.add("Start by selecting accommodation or your first attraction");
        } else if (dayPlaces.size() >= 4) {
            insights.add("You have a packed day! Check travel times between places");
        }
        context.setDayInsights(insights);
        
        // Generate warnings
        List<String> warnings = new ArrayList<>();
        if (totalDuration > 480) {
            warnings.add("This day might be too packed. Consider moving some activities to another day");
        }
        if (!needsAccommodation(dayPlaces) && dayNumber == 1) {
            warnings.add("Consider adding accommodation for your stay");
        }
        context.setDayWarnings(warnings);
        
        return context;
    }
    
    // Helper methods
    
    private boolean needsAccommodation(List<PlannedPlace> places) {
        return places.stream().noneMatch(place -> PlannedPlace.PlaceType.HOTEL.equals(place.getType()));
    }
    
    private boolean hasRestaurant(List<PlannedPlace> places) {
        return places.stream().anyMatch(place -> PlannedPlace.PlaceType.RESTAURANT.equals(place.getType()));
    }
    
    private String getCategoryDisplayName(PlannedPlace.PlaceType placeType) {
        switch (placeType) {
            case HOTEL: return "Accommodation";
            case RESTAURANT: return "Restaurant";
            case ATTRACTION: return "Attraction";
            case SHOPPING: return "Shopping";
            case VIEWPOINT: return "Nature";
            case TRANSPORT_HUB: return "Transport";
            default: return "Activity";
        }
    }
    
    private String suggestNextCategory(List<PlannedPlace> existingPlaces, List<String> tripPreferences) {
        if (needsAccommodation(existingPlaces)) {
            return "Accommodation";
        }
        if (!hasRestaurant(existingPlaces)) {
            return "Restaurant";
        }
        if (tripPreferences.contains("Adventure")) {
            return "Activity";
        }
        return "Attraction";
    }
    
    private PlannedPlace getLastPlaceForContext(Trip trip, Integer dayNumber, String lastPlaceId) {
        if (lastPlaceId != null) {
            return trip.getPlaces().stream()
                    .filter(place -> lastPlaceId.equals(place.getPlaceId()))
                    .findFirst()
                    .orElse(null);
        }
        
        return trip.getPlaces().stream()
                .filter(place -> dayNumber.equals(place.getDayNumber()))
                .max(Comparator.comparing(place -> place.getPlaceId()))
                .orElse(null);
    }
    
    private List<DayPlanResponse.QuickSuggestion> deduplicateAndSort(
            List<DayPlanResponse.QuickSuggestion> suggestions, PlannedPlace referencePlace) {
        
        // Remove duplicates by name similarity
        Map<String, DayPlanResponse.QuickSuggestion> uniqueSuggestions = new LinkedHashMap<>();
        
        for (DayPlanResponse.QuickSuggestion suggestion : suggestions) {
            String key = suggestion.getName().toLowerCase().trim();
            if (!uniqueSuggestions.containsKey(key)) {
                uniqueSuggestions.put(key, suggestion);
            }
        }
        
        // Sort by travel time, then by rating
        return uniqueSuggestions.values().stream()
                .sorted((a, b) -> {
                    if (a.getTravelTimeMinutes() != null && b.getTravelTimeMinutes() != null) {
                        int timeCompare = Integer.compare(a.getTravelTimeMinutes(), b.getTravelTimeMinutes());
                        if (timeCompare != 0) return timeCompare;
                    }
                    if (a.getRating() != null && b.getRating() != null) {
                        return Double.compare(b.getRating(), a.getRating());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }
    
    private List<DayPlanResponse.QuickSuggestion> getAccommodationSuggestions(Trip trip, List<PlannedPlace> existingPlaces) {
        // Implementation for accommodation suggestions
        return new ArrayList<>();
    }
    
    private List<DayPlanResponse.QuickSuggestion> getInitialSuggestions(Trip trip, Integer dayNumber) {
        // Implementation for initial day suggestions
        return new ArrayList<>();
    }
    
    private List<DayPlanResponse.QuickSuggestion> getContextualSuggestions(Trip trip, PlannedPlace lastPlace) {
        // Implementation for contextual suggestions
        return new ArrayList<>();
    }
    
    private List<DayPlanResponse.QuickSuggestion> getRestaurantSuggestions(Trip trip, List<PlannedPlace> existingPlaces) {
        // Implementation for restaurant suggestions
        return new ArrayList<>();
    }
    
    private List<DayPlanResponse.QuickSuggestion> getBaseCitySuggestions(Trip trip, String categoryFilter) {
        // Implementation for base city suggestions
        return new ArrayList<>();
    }
}
