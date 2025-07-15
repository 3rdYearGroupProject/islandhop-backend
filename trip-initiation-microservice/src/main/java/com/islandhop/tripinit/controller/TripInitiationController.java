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
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
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
    private final GeoApiContext geoApiContext;
    
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
            
            // Test a simple directions request (Colombo to Kandy)
            LatLng origin = new LatLng(6.927079, 79.861243); // Colombo
            LatLng destination = new LatLng(7.290572, 80.633728); // Kandy
            
            log.info("Testing Google Maps API with a simple request from Colombo to Kandy");
            
            DirectionsResult directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .await();
            
            if (directionsResult.routes.length > 0) {
                double distance = directionsResult.routes[0].legs[0].distance.inMeters / 1000.0;
                result.put("testSuccess", true);
                result.put("testDistance", distance + " km");
                result.put("message", "Google Maps API is working correctly");
                log.info("Google Maps API test successful. Distance: {} km", distance);
            } else {
                result.put("testSuccess", false);
                result.put("message", "No routes found in API response");
                log.warn("Google Maps API test: No routes found");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Google Maps API test failed: {}", e.getMessage(), e);
            result.put("testSuccess", false);
            result.put("error", e.getMessage());
            result.put("message", "Google Maps API test failed");
            
            // Specific error handling
            if (e.getMessage() != null) {
                if (e.getMessage().contains("API keys with referer restrictions")) {
                    result.put("solution", "Remove referer restrictions from API key or add server domain to allowed referers");
                } else if (e.getMessage().contains("API key not valid")) {
                    result.put("solution", "Check that the API key is correct and has access to Directions API");
                } else if (e.getMessage().contains("quota")) {
                    result.put("solution", "Check API quota limits in Google Cloud Console");
                }
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}