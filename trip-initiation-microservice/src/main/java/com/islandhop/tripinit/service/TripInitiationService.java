package com.islandhop.tripinit.service;

import com.islandhop.tripinit.dto.TripInitiationRequest;
import com.islandhop.tripinit.dto.TripInitiationResponse;
import com.islandhop.tripinit.dto.RouteSummary;
import com.islandhop.tripinit.exception.TripNotFoundException;
import com.islandhop.tripinit.exception.VehicleTypeNotFoundException;
import com.islandhop.tripinit.model.mongodb.*;
import com.islandhop.tripinit.model.postgresql.GuideFee;
import com.islandhop.tripinit.model.postgresql.VehicleType;
import com.islandhop.tripinit.repository.mongodb.InitiatedTripRepository;
import com.islandhop.tripinit.repository.mongodb.TripPlanRepository;
import com.islandhop.tripinit.repository.postgresql.GuideFeeRepository;
import com.islandhop.tripinit.repository.postgresql.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for trip initiation business logic.
 * Handles route calculation, cost estimation, and trip saving.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripInitiationService {
    
    private final TripPlanRepository tripPlanRepository;
    private final InitiatedTripRepository initiatedTripRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final GuideFeeRepository guideFeeRepository;
    private final GoogleMapsService googleMapsService;
    
    // Fixed driver fee per day (configurable)
    private static final Double DRIVER_FEE_PER_DAY = 50.0;
    
    /**
     * Initiates a trip based on the provided request.
     * Calculates route, costs, and saves initiated trip to MongoDB.
     * 
     * @param request Trip initiation request
     * @return Trip initiation response with costs and route summary
     */
    @Transactional
    public TripInitiationResponse initiateTrip(TripInitiationRequest request) {
        log.info("Initiating trip for user: {}, tripId: {}", request.getUserId(), request.getTripId());
        
        // Retrieve trip plan from MongoDB
        TripPlan tripPlan = tripPlanRepository.findByIdAndUserId(request.getTripId(), request.getUserId())
                .orElseThrow(() -> new TripNotFoundException("Trip not found with ID: " + request.getTripId()));
        
        // Get vehicle type from PostgreSQL
        VehicleType vehicleType = vehicleTypeRepository.findById(Long.valueOf(request.getPreferredVehicleTypeId()))
                .orElseThrow(() -> new VehicleTypeNotFoundException("Vehicle type not found with ID: " + request.getPreferredVehicleTypeId()));
        
        // Calculate total distance using Google Maps API
        Double totalDistance = googleMapsService.calculateTotalDistance(tripPlan.getDailyPlans());
        
        // Calculate trip duration in days
        int tripDays = calculateTripDays(tripPlan.getStartDate(), tripPlan.getEndDate());
        
        // Calculate driver and guide costs
        Double driverCost = calculateDriverCost(totalDistance, vehicleType.getPricePerKm(), tripDays, request.getSetDriver());
        Double guideCost = calculateGuideCost(tripPlan.getDailyPlans(), tripDays, request.getSetGuide());
        
        // Save initiated trip to MongoDB
        InitiatedTrip initiatedTrip = createInitiatedTrip(tripPlan, request, totalDistance, driverCost, guideCost, vehicleType.getTypeName());
        initiatedTripRepository.save(initiatedTrip);
        
        // Build route summary for response
        List<RouteSummary> routeSummary = buildRouteSummary(tripPlan.getDailyPlans());
        
        log.info("Trip initiated successfully for tripId: {}", request.getTripId());
        
        return TripInitiationResponse.builder()
                .tripId(request.getTripId())
                .userId(request.getUserId())
                .averageTripDistance(totalDistance)
                .averageDriverCost(driverCost)
                .averageGuideCost(guideCost)
                .vehicleType(vehicleType.getTypeName())
                .routeSummary(routeSummary)
                .build();
    }
    
    /**
     * Calculates the number of days for the trip using java.time.
     * 
     * @param startDate Start date in YYYY-MM-DD format
     * @param endDate End date in YYYY-MM-DD format
     * @return Number of trip days
     */
    private int calculateTripDays(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }
    
    /**
     * Calculates driver cost based on distance and trip duration.
     * 
     * @param totalDistance Total trip distance in kilometers
     * @param pricePerKm Vehicle price per kilometer
     * @param tripDays Number of trip days
     * @param setDriver Whether driver is needed (1) or not (0)
     * @return Total driver cost
     */
    private Double calculateDriverCost(Double totalDistance, Double pricePerKm, int tripDays, Integer setDriver) {
        if (setDriver == 0) {
            return 0.0;
        }
        
        Double distanceCost = totalDistance * pricePerKm;
        Double dailyCost = DRIVER_FEE_PER_DAY * tripDays;
        
        return distanceCost + dailyCost;
    }
    
    /**
     * Calculates guide cost based on cities and trip duration.
     * 
     * @param dailyPlans List of daily plans with cities
     * @param tripDays Number of trip days
     * @param setGuide Whether guide is needed (1) or not (0)
     * @return Total guide cost
     */
    private Double calculateGuideCost(List<DailyPlan> dailyPlans, int tripDays, Integer setGuide) {
        if (setGuide == 0) {
            return 0.0;
        }
        
        Double totalGuideCost = 0.0;
        
        for (DailyPlan plan : dailyPlans) {
            if (plan.getCity() != null && !plan.getCity().isEmpty()) {
                GuideFee guideFee = guideFeeRepository.findByCity(plan.getCity())
                        .orElse(null);
                
                if (guideFee != null) {
                    totalGuideCost += guideFee.getPricePerDay();
                }
            }
        }
        
        return totalGuideCost;
    }
    
    /**
     * Creates an initiated trip from the trip plan with additional fields.
     * 
     * @param tripPlan Original trip plan
     * @param request Trip initiation request
     * @param totalDistance Calculated total distance
     * @param driverCost Calculated driver cost
     * @param guideCost Calculated guide cost
     * @param vehicleTypeName Vehicle type name
     * @return Initiated trip entity
     */
    private InitiatedTrip createInitiatedTrip(TripPlan tripPlan, TripInitiationRequest request, 
            Double totalDistance, Double driverCost, Double guideCost, String vehicleTypeName) {
        
        InitiatedTrip initiatedTrip = new InitiatedTrip();
        
        // Copy all fields from trip plan
        initiatedTrip.setId(tripPlan.getId());
        initiatedTrip.setUserId(tripPlan.getUserId());
        initiatedTrip.setTripName(tripPlan.getTripName());
        initiatedTrip.setStartDate(tripPlan.getStartDate());
        initiatedTrip.setEndDate(tripPlan.getEndDate());
        initiatedTrip.setArrivalTime(tripPlan.getArrivalTime());
        initiatedTrip.setBaseCity(tripPlan.getBaseCity());
        initiatedTrip.setMultiCityAllowed(tripPlan.getMultiCityAllowed());
        initiatedTrip.setActivityPacing(tripPlan.getActivityPacing());
        initiatedTrip.setBudgetLevel(tripPlan.getBudgetLevel());
        initiatedTrip.setPreferredTerrains(tripPlan.getPreferredTerrains());
        initiatedTrip.setPreferredActivities(tripPlan.getPreferredActivities());
        initiatedTrip.setDailyPlans(tripPlan.getDailyPlans());
        initiatedTrip.setMapData(tripPlan.getMapData());
        initiatedTrip.setCreatedAt(tripPlan.getCreatedAt());
        initiatedTrip.setLastUpdated(Instant.now());
        
        // Set additional fields for initiated trip
        initiatedTrip.setDriverNeeded(request.getSetDriver());
        initiatedTrip.setGuideNeeded(request.getSetGuide());
        initiatedTrip.setAverageTripDistance(totalDistance);
        initiatedTrip.setAverageDriverCost(driverCost);
        initiatedTrip.setAverageGuideCost(guideCost);
        initiatedTrip.setVehicleType(vehicleTypeName);
        
        return initiatedTrip;
    }
    
    /**
     * Builds route summary from daily plans for response.
     * 
     * @param dailyPlans List of daily plans
     * @return List of route summaries
     */
    private List<RouteSummary> buildRouteSummary(List<DailyPlan> dailyPlans) {
        return dailyPlans.stream()
                .map(plan -> RouteSummary.builder()
                        .day(plan.getDay())
                        .city(plan.getCity())
                        .attractions(plan.getAttractions() != null ? 
                                plan.getAttractions().stream()
                                        .map(attraction -> RouteSummary.AttractionSummary.builder()
                                                .name(attraction.getName())
                                                .location(RouteSummary.LocationSummary.builder()
                                                        .lat(attraction.getLocation().getLat())
                                                        .lng(attraction.getLocation().getLng())
                                                        .build())
                                                .build())
                                        .collect(Collectors.toList()) : List.of())
                        .build())
                .collect(Collectors.toList());
    }
}