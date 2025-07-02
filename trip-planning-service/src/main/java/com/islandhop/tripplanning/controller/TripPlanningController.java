package com.islandhop.tripplanning.controller;

import com.islandhop.tripplanning.dto.*;
import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.service.TripPlanningService;
import com.islandhop.tripplanning.service.SessionValidationService;
import com.islandhop.tripplanning.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trip")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class TripPlanningController {
    
    private final TripPlanningService tripPlanningService;
    private final SessionValidationService sessionValidationService;
    private final LocationService locationService;
    
    /**
     * Create a new trip with user preferences
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTrip(@Valid @RequestBody CreateTripRequest request, 
                                         HttpSession session) {
        log.info("POST /trip/initiate called");
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip trip = tripPlanningService.createTrip(request, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Trip created successfully",
                "tripId", trip.getTripId(),
                "trip", trip
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/initiate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating trip: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Add a place manually to an existing trip
     */
    @PostMapping("/{tripId}/add-place")
    public ResponseEntity<?> addPlaceToTrip(@PathVariable String tripId,
                                           @Valid @RequestBody AddPlaceRequest request,
                                           HttpSession session) {
        log.info("POST /trip/{}/add-place called", tripId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip updatedTrip = tripPlanningService.addPlaceToTrip(tripId, request, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Place added successfully",
                "trip", updatedTrip
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/add-place: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for /trip/{}/add-place: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad request", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding place to trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get AI-powered suggestions for nearby attractions
     */
    @GetMapping("/{tripId}/suggestions")
    public ResponseEntity<?> getSuggestions(@PathVariable String tripId,
                                          @RequestParam(required = false) Integer day,
                                          HttpSession session) {
        log.info("GET /trip/{}/suggestions called for day {}", tripId, day);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            SuggestionsResponse suggestions = tripPlanningService.generateSuggestions(tripId, day, userId);
            
            return ResponseEntity.ok(suggestions);
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/suggestions: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting suggestions for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Optimize the visiting order of places
     */
    @PostMapping("/{tripId}/optimize-order")
    public ResponseEntity<?> optimizeOrder(@PathVariable String tripId,
                                         HttpSession session) {
        log.info("POST /trip/{}/optimize-order called", tripId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip optimizedTrip = tripPlanningService.optimizeVisitingOrder(tripId, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Trip order optimized successfully",
                "trip", optimizedTrip
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/optimize-order: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error optimizing trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get detailed breakdown for a specific day
     */
    @GetMapping("/{tripId}/day/{day}")
    public ResponseEntity<?> getDayPlan(@PathVariable String tripId,
                                       @PathVariable Integer day,
                                       HttpSession session) {
        log.info("GET /trip/{}/day/{} called", tripId, day);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            DayPlan dayPlan = tripPlanningService.getDayPlan(tripId, day, userId);
            
            return ResponseEntity.ok(dayPlan);
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}: {}", tripId, day, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting day plan for trip {} day {}: {}", tripId, day, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get high-level trip summary
     */
    @GetMapping("/{tripId}/summary")
    public ResponseEntity<?> getTripSummary(@PathVariable String tripId,
                                          HttpSession session) {
        log.info("GET /trip/{}/summary called", tripId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip trip = tripPlanningService.getTripSummary(tripId, userId);
            
            return ResponseEntity.ok(trip);
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/summary: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting trip summary for {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get GPS coordinates for map display
     */
    @GetMapping("/{tripId}/map-data")
    public ResponseEntity<?> getMapData(@PathVariable String tripId,
                                      HttpSession session) {
        log.info("GET /trip/{}/map-data called", tripId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Map<String, Object> mapData = tripPlanningService.getMapData(tripId, userId);
            
            return ResponseEntity.ok(mapData);
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/map-data: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting map data for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user's trips
     */
    @GetMapping("/my-trips")
    public ResponseEntity<?> getUserTrips(HttpSession session) {
        log.info("GET /trip/my-trips called");
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            List<Trip> trips = tripPlanningService.getUserTrips(userId);
            
            return ResponseEntity.ok(Map.of("trips", trips));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/my-trips: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting user trips: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Search for locations by text query
     */
    @GetMapping("/search-locations")
    public ResponseEntity<?> searchLocations(@RequestParam String query,
                                           @RequestParam(required = false) String city,
                                           @RequestParam(required = false) Double biasLat,
                                           @RequestParam(required = false) Double biasLng,
                                           @RequestParam(required = false, defaultValue = "10") Integer maxResults,
                                           HttpSession session) {
        log.info("GET /trip/search-locations called with query: {}, city: {}", query, city);
        
        try {
            // Validate session
            sessionValidationService.validateSessionAndGetUserId(session);
            
            List<LocationService.LocationSearchResult> results = 
                locationService.searchLocations(query, city, biasLat, biasLng, maxResults);
            
            return ResponseEntity.ok(Map.of(
                "results", results,
                "count", results.size(),
                "query", query
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/search-locations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Add a place to a specific day with contextual information
     */
    @PostMapping("/{tripId}/day/{dayNumber}/add-place")
    public ResponseEntity<?> addPlaceToDay(@PathVariable String tripId,
                                          @PathVariable Integer dayNumber,
                                          @Valid @RequestBody AddPlaceToDayRequest request,
                                          HttpSession session) {
        log.info("POST /trip/{}/day/{}/add-place called for: {}", tripId, dayNumber, request.getPlaceName());
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            // Set the day number from path parameter
            request.setDayNumber(dayNumber);
            
            Trip updatedTrip = tripPlanningService.addPlaceToSpecificDay(tripId, request, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Place added to day " + dayNumber + " successfully",
                "tripId", tripId,
                "dayNumber", dayNumber,
                "placeName", request.getPlaceName(),
                "trip", updatedTrip
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}/add-place: {}", tripId, dayNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding place to day {}: {}", dayNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add place", "message", e.getMessage()));
        }
    }
    
    /**
     * Get contextual suggestions for a specific day based on user's current selections
     */
    @GetMapping("/{tripId}/day/{dayNumber}/contextual-suggestions")
    public ResponseEntity<?> getContextualSuggestions(@PathVariable String tripId,
                                                     @PathVariable Integer dayNumber,
                                                     @RequestParam(required = false, defaultValue = "initial") String contextType,
                                                     HttpSession session) {
        log.info("GET /trip/{}/day/{}/contextual-suggestions called (context: {})", tripId, dayNumber, contextType);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            ContextualSuggestionsResponse suggestions = 
                tripPlanningService.getContextualSuggestions(tripId, dayNumber, contextType, userId);
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "tripId", tripId,
                "dayNumber", dayNumber,
                "contextType", contextType
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}/contextual-suggestions: {}", tripId, dayNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting contextual suggestions for day {}: {}", dayNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get suggestions", "message", e.getMessage()));
        }
    }
    
    /**
     * Get nearby suggestions based on a selected place
     */
    @GetMapping("/{tripId}/nearby-suggestions")
    public ResponseEntity<?> getNearbySuggestions(@PathVariable String tripId,
                                                 @RequestParam String placeId,
                                                 @RequestParam(required = false) String placeType,
                                                 @RequestParam(required = false, defaultValue = "10") Integer maxResults,
                                                 HttpSession session) {
        log.info("GET /trip/{}/nearby-suggestions called for place: {}", tripId, placeId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            List<ContextualSuggestionsResponse.PlaceSuggestion> suggestions = 
                tripPlanningService.getNearbySuggestions(tripId, placeId, placeType, maxResults, userId);
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "basePlaceId", placeId,
                "count", suggestions.size()
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/nearby-suggestions: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting nearby suggestions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get nearby suggestions", "message", e.getMessage()));
        }
    }
    
    /**
     * Get comprehensive day plan with inline suggestions (TripAdvisor style)
     */
    @GetMapping("/{tripId}/day/{dayNumber}/plan")
    public ResponseEntity<?> getDayPlanWithSuggestions(@PathVariable String tripId,
                                       @PathVariable Integer dayNumber,
                                       HttpSession session) {
        log.info("GET /trip/{}/day/{}/plan called", tripId, dayNumber);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip trip = tripPlanningService.getTripByIdAndUserId(tripId, userId);
            
            DayPlanResponse dayPlan = tripPlanningService.getDayPlanWithInlineSuggestions(trip, dayNumber);
            
            return ResponseEntity.ok(dayPlan);
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}/plan: {}", tripId, dayNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting day plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get day plan", "message", e.getMessage()));
        }
    }
    
    /**
     * Get real-time suggestions without search (proximity + preferences)
     */
    @GetMapping("/{tripId}/day/{dayNumber}/realtime-suggestions")
    public ResponseEntity<?> getRealtimeSuggestions(@PathVariable String tripId,
                                                   @PathVariable Integer dayNumber,
                                                   @RequestParam(required = false) String lastPlaceId,
                                                   @RequestParam(required = false) String category,
                                                   HttpSession session) {
        log.info("GET /trip/{}/day/{}/realtime-suggestions called (lastPlace: {}, category: {})", 
                tripId, dayNumber, lastPlaceId, category);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            Trip trip = tripPlanningService.getTripByIdAndUserId(tripId, userId);
            
            ContextualSuggestionsResponse suggestionsResponse = 
                tripPlanningService.getRealtimeSuggestions(trip, dayNumber, lastPlaceId, category);
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestionsResponse.getSuggestions(),
                "tripId", tripId,
                "dayNumber", dayNumber,
                "basedOn", lastPlaceId != null ? "proximity" : "preferences",
                "category", category != null ? category : "all"
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}/realtime-suggestions: {}", tripId, dayNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting realtime suggestions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get suggestions", "message", e.getMessage()));
        }
    }
    
    /**
     * Quick add place to day (inline, no navigation)
     */
    @PostMapping("/{tripId}/day/{dayNumber}/quick-add")
    public ResponseEntity<?> quickAddPlace(@PathVariable String tripId,
                                          @PathVariable Integer dayNumber,
                                          @RequestParam String placeId,
                                          @RequestParam String placeName,
                                          @RequestParam String placeType,
                                          @RequestParam(required = false) String insertAfterPlaceId,
                                          HttpSession session) {
        log.info("POST /trip/{}/day/{}/quick-add called for place: {}", tripId, dayNumber, placeName);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            // Build quick add request
            AddPlaceToDayRequest request = new AddPlaceToDayRequest();
            request.setPlaceName(placeName);
            request.setDayNumber(dayNumber);
            request.setPlaceType(PlannedPlace.PlaceType.valueOf(placeType.toUpperCase()));
            request.setPreviousPlaceId(insertAfterPlaceId);
            
            // Get place details from Google Places if it's a Google Place ID
            if (placeId.startsWith("ChIJ")) {
                LocationService.PlaceDetails details = locationService.getPlaceDetails(placeId);
                if (details != null) {
                    request.setLatitude(details.getLatitude());
                    request.setLongitude(details.getLongitude());
                    request.setCity(extractCityFromAddress(details.getFormattedAddress()));
                }
            }
            
            Trip updatedTrip = tripPlanningService.addPlaceToSpecificDay(tripId, request, userId);
            
            // Return updated day plan
            DayPlanResponse updatedDayPlan = tripPlanningService.getDayPlanWithInlineSuggestions(updatedTrip, dayNumber);
            
            return ResponseEntity.ok(Map.of(
                "message", "Place added successfully",
                "dayPlan", updatedDayPlan,
                "addedPlace", placeName
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/day/{}/quick-add: {}", tripId, dayNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error quick adding place: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add place", "message", e.getMessage()));
        }
    }
    
    /**
     * Get available place categories (Google-style)
     */
    @GetMapping("/place-categories")
    public ResponseEntity<?> getPlaceCategories(HttpSession session) {
        log.info("GET /trip/place-categories called");
        
        try {
            sessionValidationService.validateSessionAndGetUserId(session);
            
            Map<String, List<String>> categories = 
                tripPlanningService.getAvailablePlaceCategories();
            
            return ResponseEntity.ok(Map.of(
                "categories", categories,
                "total", categories.size()
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/place-categories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting place categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get categories", "message", e.getMessage()));
        }
    }
    
    /**
     * Get enhanced travel info with multiple route options
     */
    @GetMapping("/{tripId}/enhanced-travel-info")
    public ResponseEntity<?> getEnhancedTravelInfo(@PathVariable String tripId,
                                                  @RequestParam String fromPlaceId,
                                                  @RequestParam String toPlaceId,
                                                  HttpSession session) {
        log.info("GET /trip/{}/enhanced-travel-info called from {} to {}", tripId, fromPlaceId, toPlaceId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            EnhancedTravelInfoResponse enhancedTravelInfo = 
                tripPlanningService.getEnhancedTravelInfo(tripId, fromPlaceId, toPlaceId, userId);
            
            return ResponseEntity.ok(enhancedTravelInfo);
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/enhanced-travel-info: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting enhanced travel info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get travel info", "message", e.getMessage()));
        }
    }
    
    // Helper method
    private String extractCityFromAddress(String formattedAddress) {
        if (formattedAddress == null) return null;
        
        String[] parts = formattedAddress.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return null;
    }
    
    /**
     * Search locations with contextual filtering based on trip preferences and current location
     */
    @GetMapping("/{tripId}/contextual-search")
    public ResponseEntity<?> contextualLocationSearch(@PathVariable String tripId,
                                                     @RequestParam String query,
                                                     @RequestParam(required = false) String placeType,
                                                     @RequestParam(required = false) Integer dayNumber,
                                                     @RequestParam(required = false) String lastPlaceId,
                                                     @RequestParam(required = false, defaultValue = "10") Integer maxResults,
                                                     HttpSession session) {
        log.info("GET /trip/{}/contextual-search called with query: {}", tripId, query);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            LocationSearchResponse searchResults = tripPlanningService.contextualLocationSearch(
                tripId, query, placeType, dayNumber, lastPlaceId, maxResults, userId);
            
            return ResponseEntity.ok(searchResults);
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/contextual-search: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in contextual location search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Get travel time and route information between two places
     */
    @GetMapping("/{tripId}/travel-info")
    public ResponseEntity<?> getTravelInfo(@PathVariable String tripId,
                                          @RequestParam String fromPlaceId,
                                          @RequestParam String toPlaceId,
                                          HttpSession session) {
        log.info("GET /trip/{}/travel-info called from {} to {}", tripId, fromPlaceId, toPlaceId);
        
        try {
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            
            Map<String, Object> travelInfo = tripPlanningService.getTravelInfo(tripId, fromPlaceId, toPlaceId, userId);
            
            return ResponseEntity.ok(travelInfo);
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/travel-info: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting travel info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get travel info", "message", e.getMessage()));
        }
    }
    
    /**
     * Validate and enrich place information
     */
    @PostMapping("/validate-place")
    public ResponseEntity<?> validatePlace(@Valid @RequestBody AddPlaceRequest request,
                                         HttpSession session) {
        log.info("POST /trip/validate-place called for: {}", request.getPlaceName());
        
        try {
            // Validate session
            sessionValidationService.validateSessionAndGetUserId(session);
            
            LocationService.PlaceValidationResult validation = 
                locationService.validateAndEnrichPlace(request);
            
            return ResponseEntity.ok(Map.of(
                "validation", validation,
                "valid", validation.isValid(),
                "hasWarnings", !validation.getWarnings().isEmpty(),
                "hasErrors", !validation.getErrors().isEmpty()
            ));
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/validate-place: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error validating place: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Validation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Get detailed information about a place by Google Place ID
     */
    @GetMapping("/place-details/{placeId}")
    public ResponseEntity<?> getPlaceDetails(@PathVariable String placeId,
                                           HttpSession session) {
        log.info("GET /trip/place-details/{} called", placeId);
        
        try {
            // Validate session
            sessionValidationService.validateSessionAndGetUserId(session);
            
            LocationService.PlaceDetails details = locationService.getPlaceDetails(placeId);
            
            if (details != null) {
                return ResponseEntity.ok(Map.of(
                    "details", details,
                    "found", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Place not found", "placeId", placeId));
            }
            
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/place-details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get place details", "message", e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("GET /trip/health called");
        return ResponseEntity.ok("Trip Planning Service is running");
    }
}
