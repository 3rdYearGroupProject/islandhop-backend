package com.islandhop.tripplanning.controller;

import com.islandhop.tripplanning.dto.*;
import com.islandhop.tripplanning.model.*;
import com.islandhop.tripplanning.service.TripPlanningService;
import com.islandhop.tripplanning.service.SessionValidationService;
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
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("GET /trip/health called");
        return ResponseEntity.ok("Trip Planning Service is running");
    }
}
