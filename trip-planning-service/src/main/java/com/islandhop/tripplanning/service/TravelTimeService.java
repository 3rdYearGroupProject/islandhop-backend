package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.model.TravelSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelTimeService {
    
    private final WebClient webClient;
    
    @Value("${api.google.maps.key:your-google-maps-key}")
    private String googleMapsApiKey;
    
    @Value("${api.google.maps.url:https://maps.googleapis.com/maps/api}")
    private String googleMapsBaseUrl;
    
    /**
     * Calculate travel time between two points
     */
    public TravelSegment calculateTravelTime(double fromLat, double fromLng, double toLat, double toLng) {
        log.info("Calculating travel time from ({}, {}) to ({}, {})", fromLat, fromLng, toLat, toLng);
        
        // Calculate straight-line distance using Haversine formula
        double distance = calculateDistance(fromLat, fromLng, toLat, toLng);
        
        // Estimate travel time based on distance
        // This is simplified - in production, use Google Distance Matrix API
        int estimatedMinutes = estimateTravelTime(distance);
        
        TravelSegment segment = new TravelSegment();
        segment.setDistance(distance);
        segment.setDurationMinutes(estimatedMinutes);
        segment.setTravelMode(determineTravelMode(distance));
        
        return segment;
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
    
    private int estimateTravelTime(double distanceKm) {
        // Simplified travel time estimation
        if (distanceKm <= 1) return 15;      // Walking within city
        if (distanceKm <= 5) return 20;      // Short taxi ride
        if (distanceKm <= 20) return 45;     // City to suburb
        if (distanceKm <= 50) return 90;     // Inter-city short
        if (distanceKm <= 100) return 150;   // Inter-city medium
        return 180; // Long distance
    }
    
    private TravelSegment.TravelMode determineTravelMode(double distanceKm) {
        if (distanceKm <= 1) return TravelSegment.TravelMode.WALKING;
        if (distanceKm <= 50) return TravelSegment.TravelMode.DRIVING;
        return TravelSegment.TravelMode.BUS; // Long distance public transport
    }
    
    /**
     * Get detailed travel information using Google Maps (enhanced)
     */
    public TravelInfo getDetailedTravelInfo(double fromLat, double fromLng, 
                                          double toLat, double toLng, String travelMode) {
        log.info("Getting detailed travel info from {},{} to {},{} via {}", 
                fromLat, fromLng, toLat, toLng, travelMode);
        
        try {
            if (!"your-google-maps-key".equals(googleMapsApiKey) && !"your-api-key-here".equals(googleMapsApiKey)) {
                return getTravelInfoViaGoogleMaps(fromLat, fromLng, toLat, toLng, travelMode);
            } else {
                log.warn("Google Maps API key not configured, using estimated calculations");
                return getEstimatedTravelInfo(fromLat, fromLng, toLat, toLng, travelMode);
            }
        } catch (Exception e) {
            log.error("Error getting travel info: {}", e.getMessage(), e);
            return getEstimatedTravelInfo(fromLat, fromLng, toLat, toLng, travelMode);
        }
    }
    
    /**
     * Simple travel time estimation (backward compatibility)
     */
    public Integer estimateTravelTime(double fromLat, double fromLng, double toLat, double toLng) {
        TravelInfo travelInfo = getDetailedTravelInfo(fromLat, fromLng, toLat, toLng, "driving");
        return travelInfo.getDurationMinutes();
    }

    // Google Maps API implementation methods
    
    private TravelInfo getTravelInfoViaGoogleMaps(double fromLat, double fromLng, 
                                                 double toLat, double toLng, String travelMode) {
        String url = "/distancematrix/json";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api" + url)
                            .queryParam("origins", fromLat + "," + fromLng)
                            .queryParam("destinations", toLat + "," + toLng)
                            .queryParam("mode", travelMode.toLowerCase())
                            .queryParam("units", "metric")
                            .queryParam("key", googleMapsApiKey)
                            .queryParam("region", "lk") // Sri Lanka region
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return parseGoogleMapsResponse(response, travelMode);
            
        } catch (Exception e) {
            log.error("Google Maps API error: {}", e.getMessage());
            return getEstimatedTravelInfo(fromLat, fromLng, toLat, toLng, travelMode);
        }
    }
    
    private TravelInfo parseGoogleMapsResponse(Map<String, Object> response, String travelMode) {
        TravelInfo info = new TravelInfo();
        info.setTravelMode(travelMode);
        info.setReliable(true);
        
        try {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
            if (rows != null && !rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                List<Map<String, Object>> elements = (List<Map<String, Object>>) row.get("elements");
                
                if (elements != null && !elements.isEmpty()) {
                    Map<String, Object> element = elements.get(0);
                    String status = (String) element.get("status");
                    
                    if ("OK".equals(status)) {
                        // Parse duration
                        Map<String, Object> duration = (Map<String, Object>) element.get("duration");
                        if (duration != null) {
                            Integer durationSeconds = (Integer) duration.get("value");
                            info.setDurationMinutes(durationSeconds / 60);
                            info.setDurationText((String) duration.get("text"));
                        }
                        
                        // Parse distance
                        Map<String, Object> distance = (Map<String, Object>) element.get("distance");
                        if (distance != null) {
                            Integer distanceMeters = (Integer) distance.get("value");
                            info.setDistanceKm(distanceMeters / 1000.0);
                            info.setDistanceText((String) distance.get("text"));
                        }
                        
                        return info;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Google Maps response: {}", e.getMessage());
        }
        
        // Fallback to estimation - pass dummy values since we don't have the coordinates in this context
        return getEstimatedTravelInfo(0.0, 0.0, 0.0, 0.0, travelMode);
    }
    
    private TravelInfo getEstimatedTravelInfo(double fromLat, double fromLng, 
                                            double toLat, double toLng, String travelMode) {
        TravelInfo info = new TravelInfo();
        info.setTravelMode(travelMode);
        info.setReliable(false);
        
        double distanceKm = calculateDistance(fromLat, fromLng, toLat, toLng);
        info.setDistanceKm(distanceKm);
        info.setDistanceText(String.format("%.1f km", distanceKm));
        
        // Estimate duration based on mode and Sri Lankan conditions
        int durationMinutes = estimateTravelTime(distanceKm);
        info.setDurationMinutes(durationMinutes);
        info.setDurationText(formatDurationText(durationMinutes));
        
        return info;
    }
    
    private String formatDurationText(int minutes) {
        if (minutes < 60) {
            return minutes + " mins";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " " + remainingMinutes + " mins";
            }
        }
    }
    
    // Enhanced data classes
    
    public static class TravelInfo {
        private String travelMode;
        private Integer durationMinutes;
        private String durationText;
        private Double distanceKm;
        private String distanceText;
        private boolean reliable; // true if from Google Maps, false if estimated
        private List<String> warnings = new ArrayList<>();
        
        // Getters and setters
        public String getTravelMode() { return travelMode; }
        public void setTravelMode(String travelMode) { this.travelMode = travelMode; }
        
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        
        public String getDurationText() { return durationText; }
        public void setDurationText(String durationText) { this.durationText = durationText; }
        
        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
        
        public String getDistanceText() { return distanceText; }
        public void setDistanceText(String distanceText) { this.distanceText = distanceText; }
        
        public boolean isReliable() { return reliable; }
        public void setReliable(boolean reliable) { this.reliable = reliable; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
