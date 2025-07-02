package com.islandhop.tripplanning.controller;

import com.islandhop.tripplanning.dto.*;
import com.islandhop.tripplanning.model.PlannedPlace;
import com.islandhop.tripplanning.service.LocationService;
import com.islandhop.tripplanning.service.TripPlanningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Test controller for development/testing purposes
 * Only active in dev profile
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class TestController {
    
    private final TripPlanningService tripPlanningService;
    private final LocationService locationService;
    
    private static final String MOCK_USER_ID = "test-user-123";
    
    /**
     * Test location search without session validation
     */
    @GetMapping("/search-locations")
    public ResponseEntity<?> testSearchLocations(@RequestParam String query,
                                                @RequestParam(required = false) String city,
                                                @RequestParam(required = false) Double biasLat,
                                                @RequestParam(required = false) Double biasLng,
                                                @RequestParam(required = false, defaultValue = "10") Integer maxResults) {
        log.info("TEST: Searching locations for query: {}, city: {}", query, city);
        
        try {
            List<LocationService.LocationSearchResult> results = 
                locationService.searchLocations(query, city, biasLat, biasLng, maxResults);
            
            return ResponseEntity.ok(Map.of(
                "results", results,
                "count", results.size(),
                "query", query,
                "testMode", true
            ));
            
        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Search failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test trip creation without session validation
     */
    @PostMapping("/create-trip")
    public ResponseEntity<?> testCreateTrip(@Valid @RequestBody CreateTripRequest request) {
        log.info("TEST: Creating trip with request: {}", request);
        
        try {
            var trip = tripPlanningService.createTrip(request, MOCK_USER_ID);
            
            return ResponseEntity.ok(Map.of(
                "message", "Trip created successfully",
                "tripId", trip.getTripId(),
                "trip", trip,
                "testMode", true,
                "mockUserId", MOCK_USER_ID
            ));
        } catch (Exception e) {
            log.error("Error creating trip: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Trip creation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test place validation without session validation
     */
    @PostMapping("/validate-place")
    public ResponseEntity<?> testValidatePlace(@Valid @RequestBody AddPlaceRequest request) {
        log.info("TEST: Validating place: {}", request);
        
        try {
            // Create mock validation response
            var validation = Map.of(
                "placeName", request.getPlaceName(),
                "city", request.getCity() != null ? request.getCity() : "Unknown",
                "isValid", true,
                "suggestions", List.of("Place appears to be valid"),
                "testMode", true
            );
            
            return ResponseEntity.ok(Map.of(
                "validation", validation,
                "testMode", true
            ));
        } catch (Exception e) {
            log.error("Error validating place: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Place validation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test place categories without session validation
     */
    @GetMapping("/place-categories")
    public ResponseEntity<?> testGetPlaceCategories() {
        log.info("TEST: Getting place categories");
        
        try {
            // Mock categories response
            var categories = List.of(
                Map.of("type", "ATTRACTION", "displayName", "Tourist Attractions", "description", "Museums, temples, historical sites"),
                Map.of("type", "ACCOMMODATION", "displayName", "Hotels & Accommodation", "description", "Hotels, guesthouses, resorts"),
                Map.of("type", "RESTAURANT", "displayName", "Restaurants & Dining", "description", "Restaurants, cafes, local eateries"),
                Map.of("type", "ACTIVITY", "displayName", "Activities & Adventures", "description", "Tours, activities, adventure sports")
            );
            
            return ResponseEntity.ok(Map.of(
                "categories", categories,
                "total", categories.size(),
                "testMode", true
            ));
        } catch (Exception e) {
            log.error("Error getting place categories: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get categories", "message", e.getMessage()));
        }
    }
    
    /**
     * Get test session info
     */
    @GetMapping("/session-info")
    public ResponseEntity<?> getTestSessionInfo() {
        return ResponseEntity.ok(Map.of(
            "mockUserId", MOCK_USER_ID,
            "testMode", true,
            "message", "This is a mock session for testing",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Test adding place to trip without session validation
     */
    @PostMapping("/add-place")
    public ResponseEntity<?> testAddPlaceToTrip(@Valid @RequestBody Map<String, Object> request) {
        log.info("TEST: Adding place to trip: {}", request);
        
        try {
            String tripId = (String) request.get("tripId");
            if (tripId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "tripId is required"));
            }
            
            // Create AddPlaceToDayRequest from the request map
            AddPlaceToDayRequest addRequest = new AddPlaceToDayRequest();
            addRequest.setPlaceName((String) request.get("placeName"));
            addRequest.setCity((String) request.get("city"));
            addRequest.setDescription((String) request.get("description"));
            addRequest.setDayNumber(request.get("dayNumber") != null ? (Integer) request.get("dayNumber") : 1);
            addRequest.setEstimatedVisitDurationMinutes(
                request.get("estimatedVisitDurationMinutes") != null ? 
                (Integer) request.get("estimatedVisitDurationMinutes") : 120);
            addRequest.setPriority(request.get("priority") != null ? (Integer) request.get("priority") : 5);
            
            // Set placeType
            String placeTypeStr = (String) request.getOrDefault("placeType", "ATTRACTION");
            try {
                addRequest.setPlaceType(PlannedPlace.PlaceType.valueOf(placeTypeStr));
            } catch (IllegalArgumentException e) {
                addRequest.setPlaceType(PlannedPlace.PlaceType.ATTRACTION);
            }
            
            var result = tripPlanningService.addPlaceToSpecificDay(tripId, addRequest, MOCK_USER_ID);
            
            return ResponseEntity.ok(Map.of(
                "message", "Place added to trip successfully",
                "result", result,
                "testMode", true,
                "mockUserId", MOCK_USER_ID
            ));
        } catch (Exception e) {
            log.error("Error adding place to trip: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Adding place failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test getting day plans without session validation
     */
    @GetMapping("/day-plans/{tripId}")
    public ResponseEntity<?> testGetDayPlans(@PathVariable String tripId) {
        log.info("TEST: Getting day plans for trip: {}", tripId);
        
        try {
            var trip = tripPlanningService.getTripSummary(tripId, MOCK_USER_ID);
            var dayPlans = trip.getDayPlans();
            
            return ResponseEntity.ok(Map.of(
                "tripId", tripId,
                "dayPlans", dayPlans,
                "totalDays", dayPlans != null ? dayPlans.size() : 0,
                "testMode", true,
                "mockUserId", MOCK_USER_ID
            ));
        } catch (Exception e) {
            log.error("Error getting day plans: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get day plans", "message", e.getMessage()));
        }
    }
    
    /**
     * Test optimizing trip order without session validation
     */
    @PostMapping("/optimize-order/{tripId}")
    public ResponseEntity<?> testOptimizeOrder(@PathVariable String tripId) {
        log.info("TEST: Optimizing order for trip: {}", tripId);
        
        try {
            var optimizedTrip = tripPlanningService.optimizeVisitingOrder(tripId, MOCK_USER_ID);
            
            return ResponseEntity.ok(Map.of(
                "message", "Trip order optimized successfully",
                "tripId", tripId,
                "optimizedTrip", optimizedTrip,
                "testMode", true,
                "mockUserId", MOCK_USER_ID
            ));
        } catch (Exception e) {
            log.error("Error optimizing trip order: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Optimization failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test getting trip summary without session validation
     */
    @GetMapping("/trip-summary/{tripId}")
    public ResponseEntity<?> testGetTripSummary(@PathVariable String tripId) {
        log.info("TEST: Getting trip summary for: {}", tripId);
        
        try {
            var trip = tripPlanningService.getTripSummary(tripId, MOCK_USER_ID);
            
            return ResponseEntity.ok(Map.of(
                "tripId", tripId,
                "trip", trip,
                "testMode", true,
                "mockUserId", MOCK_USER_ID
            ));
        } catch (Exception e) {
            log.error("Error getting trip summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get trip summary", "message", e.getMessage()));
        }
    }
}
