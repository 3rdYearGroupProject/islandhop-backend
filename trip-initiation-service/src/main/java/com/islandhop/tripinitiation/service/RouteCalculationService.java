package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.dto.RoutePoint;
import com.islandhop.tripinitiation.exception.RouteCalculationException;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.DirectionsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(RouteCalculationService.class);
    private final GeoApiContext geoApiContext;

    @Autowired
    public RouteCalculationService(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public List<RoutePoint> calculateOptimalRoute(TripPlan tripPlan) {
        List<RoutePoint> routePoints = new ArrayList<>();
        try {
            // Prepare waypoints from tripPlan's dailyPlans
            // Assuming dailyPlans contains cities and attractions with their locations
            for (var dailyPlan : tripPlan.getDailyPlans()) {
                if (dailyPlan.getUserSelected()) {
                    routePoints.add(new RoutePoint(dailyPlan.getCity(), dailyPlan.getLocation()));
                    for (var attraction : dailyPlan.getAttractions()) {
                        routePoints.add(new RoutePoint(attraction.getName(), attraction.getLocation()));
                    }
                }
            }

            // Call Google Maps Directions API
            DirectionsResult directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .origin(routePoints.get(0).getLocation())
                    .destination(routePoints.get(routePoints.size() - 1).getLocation())
                    .waypoints(routePoints.subList(1, routePoints.size() - 1).stream()
                            .map(RoutePoint::getLocation)
                            .toArray(com.google.maps.model.LatLng[]::new))
                    .await();

            logger.info("Route calculated successfully: {}", directionsResult);
            // Process directionsResult to extract route details as needed

        } catch (Exception e) {
            logger.error("Error calculating route: {}", e.getMessage(), e);
            throw new RouteCalculationException("Failed to calculate the optimal route", e);
        }
        return routePoints;
    }
}