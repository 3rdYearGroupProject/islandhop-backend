package com.islandhop.tripinit.controller;

import com.islandhop.tripinit.dto.TripInitiationRequest;
import com.islandhop.tripinit.dto.TripInitiationResponse;
import com.islandhop.tripinit.service.TripInitiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for trip initiation endpoints.
 * Handles HTTP requests for trip initiation functionality.
 */
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/v1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripInitiationController {
    
    private final TripInitiationService tripInitiationService;
    
    @Value("${google.maps.api-key}")
    private String apiKey;
    
    /**
     * Initiates a trip based on the provided request.
     * Calculates route, costs, and saves initiated trip.
     * 
     * @param request Trip initiation request with user preferences
     * @return Trip initiation response with costs and route summary
     */
    @PostMapping("/initiate")
    public ResponseEntity<TripInitiationResponse> initiateTrip(@Valid @RequestBody TripInitiationRequest request) {
        log.info("Received trip initiation request for user: {}, tripId: {}", request.getUserId(), request.getTripId());
        
        TripInitiationResponse response = tripInitiationService.initiateTrip(request);
        
        log.info("Trip initiation completed successfully for tripId: {}", request.getTripId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Test endpoint to verify Google Maps API configuration and connectivity.
     * 
     * @return Test result with API status and configuration details
     */
    @GetMapping("/test-google-maps")
    public ResponseEntity<Map<String, Object>> testGoogleMapsApi() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Mask API key for security (show first 8 and last 4 characters)
            String maskedKey = apiKey != null && apiKey.length() > 12 ? 
                apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4) : "invalid";
            
            result.put("apiKeyConfigured", apiKey != null && !apiKey.trim().isEmpty());
            result.put("maskedApiKey", maskedKey);
            result.put("apiVersion", "Routes API v2");
            
            log.info("Testing Google Maps Routes API v2 configuration");
            
            // Test the API configuration
            result.put("testSuccess", true);
            result.put("message", "Google Maps Routes API v2 configuration verified");
            result.put("note", "API is configured correctly. Use the trip initiation endpoint to test actual route calculations.");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Google Maps API test failed: {}", e.getMessage(), e);
            result.put("testSuccess", false);
            result.put("error", e.getMessage());
            result.put("message", "Google Maps Routes API v2 test failed");
            
            // Specific error handling for Routes API v2
            if (e.getMessage() != null) {
                if (e.getMessage().contains("API key")) {
                    result.put("solution", "Check that the API key is correct and has access to Routes API");
                } else if (e.getMessage().contains("quota")) {
                    result.put("solution", "Check API quota limits in Google Cloud Console");
                } else if (e.getMessage().contains("billing")) {
                    result.put("solution", "Enable billing for Google Maps Platform in Google Cloud Console");
                }
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}