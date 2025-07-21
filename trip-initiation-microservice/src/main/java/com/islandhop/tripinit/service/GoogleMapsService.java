package com.islandhop.tripinit.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.islandhop.tripinit.model.mongodb.DailyPlan;
import com.islandhop.tripinit.model.mongodb.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Google Maps Routes API v2 integration.
 * Handles route calculation and distance computation using the new Routes API.
 */
@Service
public class GoogleMapsService {
    
    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    private static final String ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    
    public GoogleMapsService(RestTemplate restTemplate, 
                           ObjectMapper objectMapper,
                           @Value("${google.maps.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }
    
    /**
     * Calculates the total distance for a route through multiple waypoints.
     * Uses the new Google Routes API v2.
     * 
     * @param dailyPlans List of daily plans containing location data
     * @return Total distance in kilometers
     * @throws RuntimeException if calculation fails
     */
    public Double calculateTotalDistance(List<DailyPlan> dailyPlans) {
        if (dailyPlans == null || dailyPlans.isEmpty()) {
            log.warn("No daily plans provided for distance calculation");
            return 0.0;
        }
        
        List<LatLng> waypoints = extractWaypoints(dailyPlans);
        if (waypoints.size() < 2) {
            log.warn("Insufficient waypoints for distance calculation. Need at least 2, got {}", waypoints.size());
            return 0.0;
        }
        
        return calculateDistanceForWaypoints(waypoints);
    }
    
    /**
     * Calculates distance using Google Routes API v2 for the given waypoints.
     * 
     * @param waypoints List of waypoints
     * @return Total distance in kilometers
     */
    private double calculateDistanceForWaypoints(List<LatLng> waypoints) {
        try {
            double totalDistance = 0.0;
            
            log.info("Calculating distance for {} waypoints using Routes API v2", waypoints.size());
            
            // Calculate distance for each segment
            for (int i = 0; i < waypoints.size() - 1; i++) {
                LatLng origin = waypoints.get(i);
                LatLng destination = waypoints.get(i + 1);
                
                log.debug("Calculating distance from {} to {}", origin, destination);
                
                double segmentDistance = calculateSegmentDistance(origin, destination);
                totalDistance += segmentDistance;
                log.debug("Segment {} distance: {} km", i, segmentDistance);
            }
            
            log.info("Calculated total distance: {} km", totalDistance);
            return totalDistance;
            
        } catch (Exception e) {
            log.error("Error calculating distance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate route distance", e);
        }
    }
    
    /**
     * Calculates distance for a single segment using Routes API v2.
     * 
     * @param origin Starting point
     * @param destination End point
     * @return Distance in kilometers
     */
    private double calculateSegmentDistance(LatLng origin, LatLng destination) {
        try {
            // Prepare request body for Routes API v2
            Map<String, Object> requestBody = new HashMap<>();
            
            // Set origin
            Map<String, Object> originLocation = new HashMap<>();
            Map<String, Double> originLatLng = new HashMap<>();
            originLatLng.put("latitude", origin.lat);
            originLatLng.put("longitude", origin.lng);
            originLocation.put("latLng", originLatLng);
            requestBody.put("origin", Map.of("location", originLocation));
            
            // Set destination
            Map<String, Object> destinationLocation = new HashMap<>();
            Map<String, Double> destinationLatLng = new HashMap<>();
            destinationLatLng.put("latitude", destination.lat);
            destinationLatLng.put("longitude", destination.lng);
            destinationLocation.put("latLng", destinationLatLng);
            requestBody.put("destination", Map.of("location", destinationLocation));
            
            // Set travel mode
            requestBody.put("travelMode", "DRIVE");
            
            // Set routing preference
            requestBody.put("routingPreference", "TRAFFIC_AWARE");
            
            // Set units
            requestBody.put("units", "METRIC");
            
            // Set response field mask to get distance
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("X-Goog-FieldMask", "routes.distanceMeters,routes.duration");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Sending request to Routes API: {}", ROUTES_API_URL);
            
            ResponseEntity<String> response = restTemplate.exchange(
                ROUTES_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                RoutesApiResponse routesResponse = objectMapper.readValue(response.getBody(), RoutesApiResponse.class);
                
                if (routesResponse.getRoutes() != null && !routesResponse.getRoutes().isEmpty()) {
                    Route route = routesResponse.getRoutes().get(0);
                    if (route.getDistanceMeters() != null) {
                        double distanceKm = route.getDistanceMeters() / 1000.0;
                        log.debug("Routes API returned distance: {} meters ({} km)", route.getDistanceMeters(), distanceKm);
                        return distanceKm;
                    }
                }
            }
            
            log.warn("No valid route found from {} to {}", origin, destination);
            return 0.0;
            
        } catch (Exception e) {
            log.error("Error calling Routes API for segment from {} to {}: {}", origin, destination, e.getMessage(), e);
            
            // Check for common API errors
            if (e.getMessage() != null) {
                if (e.getMessage().contains("API key")) {
                    log.error("API Key Error: Please check your Google Maps API key configuration");
                } else if (e.getMessage().contains("quota")) {
                    log.error("Quota Error: Google Maps API quota exceeded");
                } else if (e.getMessage().contains("billing")) {
                    log.error("Billing Error: Please enable billing for Google Maps API");
                }
            }
            
            throw new RuntimeException("Failed to calculate segment distance", e);
        }
    }
    
    /**
     * Extracts waypoints from daily plans for route calculation.
     * 
     * @param dailyPlans List of daily plans
     * @return List of LatLng waypoints
     */
    private List<LatLng> extractWaypoints(List<DailyPlan> dailyPlans) {
        List<LatLng> waypoints = new ArrayList<>();
        
        for (DailyPlan plan : dailyPlans) {
            if (plan.getAttractions() != null) {
                for (Place attraction : plan.getAttractions()) {
                    if (attraction.getLocation() != null && 
                        attraction.getLocation().getLat() != null && 
                        attraction.getLocation().getLng() != null) {
                        waypoints.add(new LatLng(
                            attraction.getLocation().getLat(),
                            attraction.getLocation().getLng()
                        ));
                    }
                }
            }
        }
        
        return waypoints;
    }
    
    /**
     * Simple LatLng class to represent coordinates.
     */
    public static class LatLng {
        public final double lat;
        public final double lng;
        
        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
        
        @Override
        public String toString() {
            return String.format("LatLng(%.6f, %.6f)", lat, lng);
        }
    }
    
    /**
     * DTO for Routes API v2 response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoutesApiResponse {
        private List<Route> routes;
        
        public List<Route> getRoutes() {
            return routes;
        }
        
        public void setRoutes(List<Route> routes) {
            this.routes = routes;
        }
    }
    
    /**
     * DTO for a single route in Routes API v2 response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        @JsonProperty("distanceMeters")
        private Integer distanceMeters;
        
        @JsonProperty("duration")
        private String duration;
        
        public Integer getDistanceMeters() {
            return distanceMeters;
        }
        
        public void setDistanceMeters(Integer distanceMeters) {
            this.distanceMeters = distanceMeters;
        }
        
        public String getDuration() {
            return duration;
        }
        
        public void setDuration(String duration) {
            this.duration = duration;
        }
    }
}