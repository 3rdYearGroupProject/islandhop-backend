package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.model.TravelSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelTimeService {
    
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
}
