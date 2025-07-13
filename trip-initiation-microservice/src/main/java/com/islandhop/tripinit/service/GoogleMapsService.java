package com.islandhop.tripinit.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;
import com.islandhop.tripinit.model.mongodb.DailyPlan;
import com.islandhop.tripinit.model.mongodb.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Google Maps API integration.
 * Handles route calculation and distance computation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsService {
    
    private final GeoApiContext geoApiContext;
    
    /**
     * Calculates the total distance for a trip route using Google Maps API.
     * 
     * @param dailyPlans List of daily plans with attractions
     * @return Total distance in kilometers
     */
    public Double calculateTotalDistance(List<DailyPlan> dailyPlans) {
        try {
            List<LatLng> waypoints = extractWaypoints(dailyPlans);
            
            if (waypoints.size() < 2) {
                log.warn("Not enough waypoints to calculate distance");
                return 0.0;
            }
            
            double totalDistance = 0.0;
            
            // Calculate distance between consecutive waypoints
            for (int i = 0; i < waypoints.size() - 1; i++) {
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(waypoints.get(i))
                        .destination(waypoints.get(i + 1))
                        .await();
                
                if (result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    totalDistance += route.legs[0].distance.inMeters / 1000.0; // Convert to kilometers
                }
            }
            
            log.info("Calculated total distance: {} km", totalDistance);
            return totalDistance;
            
        } catch (Exception e) {
            log.error("Error calculating distance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate route distance", e);
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
}