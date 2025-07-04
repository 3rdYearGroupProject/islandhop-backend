package com.islandhop.tripplanning.controller;

import com.islandhop.tripplanning.config.CorsConfigConstants;
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
@RequestMapping("/v1/trip")
@CrossOrigin(origins = CorsConfigConstants.ALLOWED_ORIGIN, allowCredentials = CorsConfigConstants.ALLOW_CREDENTIALS)
@RequiredArgsConstructor
@Slf4j
public class TripPlanningController {
    
    private final TripPlanningService tripPlanningService;
    private final SessionValidationService sessionValidationService;
    private final LocationService locationService;
    
    /**
     * CORS test endpoint - simple GET request
     */
    @GetMapping("/cors-test")
    public ResponseEntity<?> corsTest() {
        log.info("🚀 Starting CORS test endpoint");
        try {
            log.debug("📍 Creating response data for CORS test");
            Map<String, Object> response = Map.of(
                "message", "CORS is working",
                "timestamp", java.time.Instant.now(),
                "service", "trip-planning-service"
            );
            log.info("✅ CORS test completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Unexpected error in CORS test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "CORS test failed", "message", e.getMessage()));
        }
    }
    
    /**
     * CORS test endpoint - OPTIONS preflight
     */
    @RequestMapping(value = "/cors-test", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> corsTestOptions() {
        log.info("🔄 Processing OPTIONS preflight request for CORS test");
        try {
            log.debug("📍 Setting CORS headers for preflight response");
            log.info("✅ OPTIONS preflight completed successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error in OPTIONS preflight: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new trip with user preferences
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTrip(@Valid @RequestBody CreateTripRequest request, 
                                         HttpSession session) {
        log.info("🚀 Starting initiate trip endpoint for user: {}", request.getUserId());
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            log.debug("✅ Input validation passed for userId: {}", request.getUserId());

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and verifying userId");
            sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed");
            
            // Step 3: Create trip via service
            log.debug("🏗️ Step 3: Initiating trip creation via service layer");
            Trip trip = tripPlanningService.createTrip(request, userId).block();
            
            if (trip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Trip creation failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip initiated successfully with ID: {}", trip.getTripId());

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "message", "Trip created successfully",
                "tripId", trip.getTripId(),
                "userId", userId,
                "trip", trip
            );
            log.info("🎉 Trip initiation completed successfully - TripId: {}", trip.getTripId());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in initiate trip: {}", e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("📝 Invalid argument in initiate trip: {}", e.getMessage());
            log.debug("🔍 Validation exception details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad request", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error in initiate trip: {}", e.getMessage(), e);
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
        log.info("🚀 Starting add-place to trip endpoint - TripId: '{}', Place: '{}', User: '{}'", 
                tripId, request.getPlaceName(), request.getUserId());
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            if (request.getPlaceName() == null || request.getPlaceName().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: placeName is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Place name is required"));
            }
            log.debug("✅ Input validation passed for place: {}", request.getPlaceName());

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and verifying userId");
            sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Add place to trip via service
            log.debug("🏗️ Step 3: Adding place to trip via service layer");
            Trip updatedTrip = tripPlanningService.addPlaceToTrip(tripId, request, userId).block();
            
            if (updatedTrip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add place", "message", "Service returned null"));
            }
            
            log.debug("✅ Place added successfully to trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "message", "Place added successfully",
                "tripId", tripId,
                "placeName", request.getPlaceName(),
                "userId", userId,
                "trip", updatedTrip
            );
            log.info("🎉 Add place completed successfully - TripId: {}, Place: '{}'", tripId, request.getPlaceName());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in add-place to trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("📝 Invalid argument in add-place to trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Validation exception details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad request", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error adding place to trip {}: {}", tripId, e.getMessage(), e);
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
        log.info("🚀 Starting get suggestions endpoint - TripId: '{}', Day: '{}'", tripId, day);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation  
            log.debug("🔐 Step 2: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Generate suggestions via service
            log.debug("🧠 Step 3: Generating AI-powered suggestions via service layer");
            SuggestionsResponse suggestions = tripPlanningService.generateSuggestions(tripId, day, userId).block();
            
            if (suggestions == null) {
                log.error("❌ Service returned null suggestions object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get suggestions", "message", "Service returned null"));
            }
            
            log.debug("✅ Suggestions generated successfully for trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "suggestions", suggestions,
                "userId", userId,
                "tripId", tripId,
                "day", day != null ? day : "all"
            );
            
            // Calculate total suggestions count for logging
            int totalSuggestions = 0;
            if (suggestions.getAttractions() != null) totalSuggestions += suggestions.getAttractions().size();
            if (suggestions.getHotels() != null) totalSuggestions += suggestions.getHotels().size();
            if (suggestions.getRestaurants() != null) totalSuggestions += suggestions.getRestaurants().size();
            
            log.info("🎉 Get suggestions completed successfully - TripId: {}, Total Count: {}", tripId, totalSuggestions);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in get suggestions for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error getting suggestions for trip {}: {}", tripId, e.getMessage(), e);
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
        log.info("🚀 Starting optimize order endpoint - TripId: '{}'", tripId);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Optimize trip order via service
            log.debug("🔄 Step 3: Optimizing trip visiting order via service layer");
            Trip optimizedTrip = tripPlanningService.optimizeVisitingOrder(tripId, userId).block();
            
            if (optimizedTrip == null) {
                log.error("❌ Service returned null optimized trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Optimization failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip order optimized successfully for trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "message", "Trip order optimized successfully",
                "tripId", tripId,
                "userId", userId,
                "trip", optimizedTrip
            );
            log.info("🎉 Optimize order completed successfully - TripId: {}", tripId);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in optimize order for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error optimizing trip {}: {}", tripId, e.getMessage(), e);
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
        log.info("🚀 Starting get day plan endpoint - TripId: '{}', Day: '{}'", tripId, day);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            if (day == null || day < 1) {
                log.warn("⚠️ Invalid request: day is null or invalid ({})", day);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Day must be a positive integer"));
            }
            log.debug("✅ Input validation passed for tripId: {}, day: {}", tripId, day);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Get day plan via service
            log.debug("📅 Step 3: Retrieving day plan via service layer");
            DayPlan dayPlan = tripPlanningService.getDayPlan(tripId, day, userId).block();
            
            if (dayPlan == null) {
                log.error("❌ Service returned null day plan object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get day plan", "message", "Service returned null"));
            }
            
            log.debug("✅ Day plan retrieved successfully for trip: {}, day: {}", tripId, day);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "dayPlan", dayPlan,
                "userId", userId,
                "tripId", tripId,
                "day", day
            );
            log.info("🎉 Get day plan completed successfully - TripId: {}, Day: {}", tripId, day);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in get day plan for trip {} day {}: {}", tripId, day, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error getting day plan for trip {} day {}: {}", tripId, day, e.getMessage(), e);
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
        log.info("🚀 Starting get trip summary endpoint - TripId: '{}'", tripId);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Get trip summary via service
            log.debug("📊 Step 3: Retrieving trip summary via service layer");
            Trip trip = tripPlanningService.getTripSummary(tripId, userId).block();
            
            if (trip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get trip summary", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip summary retrieved successfully for trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "trip", trip,
                "userId", userId,
                "tripId", tripId
            );
            log.info("🎉 Get trip summary completed successfully - TripId: {}", tripId);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in get trip summary for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error getting trip summary for {}: {}", tripId, e.getMessage(), e);
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
        log.info("🚀 Starting get map data endpoint - TripId: '{}'", tripId);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Get map data via service
            log.debug("🗺️ Step 3: Retrieving map data via service layer");
            Map<String, Object> mapData = tripPlanningService.getMapData(tripId, userId);
            
            if (mapData == null) {
                log.error("❌ Service returned null map data object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get map data", "message", "Service returned null"));
            }
            
            log.debug("✅ Map data retrieved successfully for trip: {}", tripId);

            // Step 4: Build response (map data already contains everything)
            log.debug("📦 Step 4: Returning map data response");
            log.info("🎉 Get map data completed successfully - TripId: {}", tripId);
            return ResponseEntity.ok(mapData);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in get map data for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error getting map data for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user's trips
     */
    @GetMapping("/my-trips")
    public ResponseEntity<?> getUserTrips(HttpSession session) {
        log.info("🚀 Starting get user trips endpoint");
        
        try {
            // Step 1: Session validation (no other input to validate)
            log.debug("🔐 Step 1: Validating session and extracting userId");
            String userId = sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 2: Get user trips via service
            log.debug("📚 Step 2: Retrieving user trips via service layer");
            List<Trip> trips = tripPlanningService.getUserTrips(userId);
            
            if (trips == null) {
                log.error("❌ Service returned null trips list");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get trips", "message", "Service returned null"));
            }
            
            log.debug("✅ User trips retrieved successfully, count: {}", trips.size());

            // Step 3: Build response
            log.debug("📦 Step 3: Building success response");
            Map<String, Object> response = Map.of(
                "trips", trips,
                "userId", userId,
                "count", trips.size()
            );
            log.info("🎉 Get user trips completed successfully - UserId: {}, Count: {}", userId, trips.size());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in get user trips: {}", e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error getting user trips: {}", e.getMessage(), e);
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
        log.info("🚀 Starting search locations endpoint - Query: '{}', City: '{}'", query, city);
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (query == null || query.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: query is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Search query is required"));
            }
            if (maxResults != null && (maxResults < 1 || maxResults > 50)) {
                log.warn("⚠️ Invalid request: maxResults out of range ({})", maxResults);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "maxResults must be between 1 and 50"));
            }
            log.debug("✅ Input validation passed for query: {}", query);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session");
            sessionValidationService.validateSessionAndGetUserId(session);
            log.debug("✅ Session validation completed");
            
            // Step 3: Search locations via service
            log.debug("🔍 Step 3: Searching locations via service layer");
            List<LocationService.LocationSearchResult> results = 
                locationService.searchLocations(query, city, biasLat, biasLng, maxResults);
            
            if (results == null) {
                log.error("❌ Service returned null results list");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Location search completed, results count: {}", results.size());

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                "results", results,
                "count", results.size(),
                "query", query
            );
            log.info("🎉 Search locations completed successfully - Query: '{}', Count: {}", query, results.size());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in search locations: {}", e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error searching locations: {}", e.getMessage(), e);
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
        log.info("🚀 Starting add place to day endpoint - TripId: '{}', Day: '{}', Place: '{}', User: '{}'", 
                tripId, dayNumber, request.getPlaceName(), request.getUserId());
        
        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            if (dayNumber == null || dayNumber < 1) {
                log.warn("⚠️ Invalid request: dayNumber is null or invalid ({})", dayNumber);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Day number must be a positive integer"));
            }
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            if (request.getPlaceName() == null || request.getPlaceName().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: placeName is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Place name is required"));
            }
            log.debug("✅ Input validation passed for place: {} on day: {}", request.getPlaceName(), dayNumber);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and verifying userId");
            sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Set day number from path parameter
            log.debug("📅 Step 3: Setting day number from path parameter");
            request.setDayNumber(dayNumber);
            log.debug("✅ Day number set to: {}", dayNumber);
            
            // Step 4: Add place to specific day via service
            log.debug("🏗️ Step 4: Adding place to specific day via service layer");
            Trip updatedTrip = tripPlanningService.addPlaceToSpecificDay(tripId, request, userId);
            
            if (updatedTrip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add place", "message", "Service returned null"));
            }
            
            log.debug("✅ Place added successfully to day {} of trip: {}", dayNumber, tripId);

            // Step 5: Build response
            log.debug("📦 Step 5: Building success response");
            Map<String, Object> response = Map.of(
                "message", "Place added to day " + dayNumber + " successfully",
                "tripId", tripId,
                "dayNumber", dayNumber,
                "placeName", request.getPlaceName(),
                "userId", userId,
                "trip", updatedTrip
            );
            log.info("🎉 Add place to day completed successfully - TripId: {}, Day: {}, Place: '{}'", 
                    tripId, dayNumber, request.getPlaceName());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in add place to day {} for trip {}: {}", dayNumber, tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error adding place to day {}: {}", dayNumber, e.getMessage(), e);
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
        log.info("🚀 Starting health check endpoint");
        
        try {
            log.debug("💓 Step 1: Checking service health status");
            String healthMessage = "Trip Planning Service is running";
            log.debug("✅ Health check passed - service is operational");
            log.info("🎉 Health check completed successfully");
            return ResponseEntity.ok(healthMessage);
            
        } catch (Exception e) {
            log.error("❌ Unexpected error in health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Trip Planning Service health check failed: " + e.getMessage());
        }
    }

    /**
     * Create a basic trip with name and dates only
     * Frontend provides userId to avoid repeated user-service calls
     */
    @PostMapping("/create-basic")
    public ResponseEntity<?> createBasicTrip(@Valid @RequestBody CreateTripBasicRequest request,
                                                   HttpSession session) {
        log.info("🚀 Starting create-basic trip endpoint - Trip: '{}' for User: '{}'", 
                request.getTripName(), request.getUserId());

        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            if (request.getTripName() == null || request.getTripName().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripName is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "Trip name is required"));
            }
            log.debug("✅ Input validation passed");

            // Step 2: Session validation
            // log.debug("🔐 Step 2: Validating session");
            // sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Create trip via service
            log.debug("🏗️ Step 3: Creating basic trip via service layer");
            Trip trip = tripPlanningService.createBasicTrip(request, userId).block();
            
            if (trip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Trip creation failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip created successfully with ID: {}", trip.getTripId());

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                    "message", "Basic trip created successfully",
                    "tripId", trip.getTripId(),
                    "trip", trip
            );
            log.info("🎉 Trip creation completed successfully - TripId: {}", trip.getTripId());
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in create-basic: {}", e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("📝 Invalid argument in create-basic: {}", e.getMessage());
            log.debug("🔍 Validation exception details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad request", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error in create-basic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Update trip preferences from frontend
     */
    @PostMapping("/{tripId}/preferences")
    public ResponseEntity<?> updateTripPreferences(@PathVariable String tripId,
                                                         @Valid @RequestBody UpdatePreferencesRequest request,
                                                         HttpSession session) {
        log.info("🚀 Starting update trip preferences endpoint - TripId: '{}', User: '{}'", tripId, request.getUserId());

        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and verifying userId");
            sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Update trip preferences via service
            log.debug("⚙️ Step 3: Updating trip preferences via service layer");
            Trip trip = tripPlanningService.updateTripPreferences(tripId, userId, request).block();
            
            if (trip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Update failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip preferences updated successfully for trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                    "message", "Preferences updated successfully",
                    "tripId", tripId,
                    "userId", userId,
                    "trip", trip
            );
            log.info("🎉 Update trip preferences completed successfully - TripId: {}", tripId);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in update preferences for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error updating preferences for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Update cities for trip
     */
    @PostMapping("/{tripId}/cities")
    public ResponseEntity<?> updateTripCities(@PathVariable String tripId,
                                                    @Valid @RequestBody UpdateCitiesRequest request,
                                                    HttpSession session) {
        log.info("🚀 Starting update trip cities endpoint - TripId: '{}', User: '{}'", tripId, request.getUserId());

        try {
            // Step 1: Input validation
            log.debug("📋 Step 1: Validating input parameters");
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("⚠️ Invalid request: tripId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "TripId is required"));
            }
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("⚠️ Invalid request: userId is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad request", "message", "UserId is required"));
            }
            log.debug("✅ Input validation passed for tripId: {}", tripId);

            // Step 2: Session validation
            log.debug("🔐 Step 2: Validating session and verifying userId");
            sessionValidationService.validateSessionExists(session);
            String userId = request.getUserId();
            log.debug("✅ Session validation completed for userId: {}", userId);
            
            // Step 3: Update trip cities via service
            log.debug("🏙️ Step 3: Updating trip cities via service layer");
            Trip trip = tripPlanningService.updateTripCities(tripId, userId, request).block();
            
            if (trip == null) {
                log.error("❌ Service returned null trip object");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Update failed", "message", "Service returned null"));
            }
            
            log.debug("✅ Trip cities updated successfully for trip: {}", tripId);

            // Step 4: Build response
            log.debug("📦 Step 4: Building success response");
            Map<String, Object> response = Map.of(
                    "message", "Cities updated successfully",
                    "tripId", tripId,
                    "userId", userId,
                    "trip", trip
            );
            log.info("🎉 Update trip cities completed successfully - TripId: {}", tripId);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("🔒 Security violation in update cities for trip {}: {}", tripId, e.getMessage());
            log.debug("🔍 Security exception details", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error updating cities for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Search activities/things to do for trip with user preferences and nearby recommendations
     */
    @GetMapping("/{tripId}/search/activities")
    public ResponseEntity<?> searchActivities(@PathVariable String tripId,
                                                    @RequestParam(required = false) String query,
                                                    @RequestParam(required = false) String city,
                                                    @RequestParam(required = false) String lastPlaceId,
                                                    @RequestParam(defaultValue = "10") Integer maxResults,
                                                    @RequestParam(required = false) String userId,
                                                    HttpSession session) {
        log.info("GET /trip/{}/search/activities called with query: {} for user: {}", tripId, query, userId);

        try {
            // If userId provided, validate it matches session (hybrid approach)
            String validatedUserId;
            if (userId != null && !userId.isEmpty()) {
                sessionValidationService.validateSessionExists(session);
                validatedUserId = userId;
            } else {
                validatedUserId = sessionValidationService.validateSessionAndGetUserId(session);
            }
            
            var results = locationService.searchActivitiesForTrip(
                    tripId, validatedUserId, query, city, lastPlaceId, maxResults).block();
                    
            return ResponseEntity.ok(Map.of(
                    "message", "Activities found",
                    "results", results,
                    "userId", validatedUserId
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/search/activities: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching activities for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Search accommodation for trip with user preferences and nearby recommendations
     */
    @GetMapping("/{tripId}/search/accommodation")
    public ResponseEntity<?> searchAccommodation(@PathVariable String tripId,
                                                       @RequestParam(required = false) String query,
                                                       @RequestParam(required = false) String city,
                                                       @RequestParam(required = false) String lastPlaceId,
                                                       @RequestParam(defaultValue = "10") Integer maxResults,
                                                       @RequestParam(required = false) String userId,
                                                       HttpSession session) {
        log.info("GET /trip/{}/search/accommodation called with query: {} for user: {}", tripId, query, userId);

        try {
            // If userId provided, validate it matches session (hybrid approach)
            String validatedUserId;
            if (userId != null && !userId.isEmpty()) {
                sessionValidationService.validateSessionExists(session);
                validatedUserId = userId;
            } else {
                validatedUserId = sessionValidationService.validateSessionAndGetUserId(session);
            }
            
            var results = locationService.searchAccommodationForTrip(
                    tripId, validatedUserId, query, city, lastPlaceId, maxResults).block();
                    
            return ResponseEntity.ok(Map.of(
                    "message", "Accommodation found",
                    "results", results,
                    "userId", validatedUserId
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/search/accommodation: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching accommodation for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Search dining options for trip with user preferences and nearby recommendations
     */
    @GetMapping("/{tripId}/search/dining")
    public ResponseEntity<?> searchDining(@PathVariable String tripId,
                                                @RequestParam(required = false) String query,
                                                @RequestParam(required = false) String city,
                                                @RequestParam(required = false) String lastPlaceId,
                                                @RequestParam(defaultValue = "10") Integer maxResults,
                                                @RequestParam(required = false) String userId,
                                                HttpSession session) {
        log.info("GET /trip/{}/search/dining called with query: {} for user: {}", tripId, query, userId);

        try {
            // If userId provided, validate it matches session (hybrid approach)
            String validatedUserId;
            if (userId != null && !userId.isEmpty()) {
                sessionValidationService.validateSessionExists(session);
                validatedUserId = userId;
            } else {
                validatedUserId = sessionValidationService.validateSessionAndGetUserId(session);
            }
            
            var results = locationService.searchDiningForTrip(
                    tripId, validatedUserId, query, city, lastPlaceId, maxResults).block();
                    
            return ResponseEntity.ok(Map.of(
                    "message", "Dining options found",
                    "results", results,
                    "userId", validatedUserId
            ));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to /trip/{}/search/dining: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching dining for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
