package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.dto.TripInitiationRequest;
import com.islandhop.tripinitiation.dto.TripInitiationResponse;
import com.islandhop.tripinitiation.exception.TripNotFoundException;
import com.islandhop.tripinitiation.exception.VehicleTypeNotFoundException;
import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.model.mongo.InitiatedTrip;
import com.islandhop.tripinitiation.repository.TripPlanRepository;
import com.islandhop.tripinitiation.repository.InitiatedTripRepository;
import com.islandhop.tripinitiation.repository.VehicleTypeRepository;
import com.islandhop.tripinitiation.service.RouteCalculationService;
import com.islandhop.tripinitiation.service.CostCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TripInitiationService {

    private static final Logger logger = LoggerFactory.getLogger(TripInitiationService.class);

    private final TripPlanRepository tripPlanRepository;
    private final InitiatedTripRepository initiatedTripRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final RouteCalculationService routeCalculationService;
    private final CostCalculationService costCalculationService;

    public TripInitiationService(TripPlanRepository tripPlanRepository,
                                  InitiatedTripRepository initiatedTripRepository,
                                  VehicleTypeRepository vehicleTypeRepository,
                                  RouteCalculationService routeCalculationService,
                                  CostCalculationService costCalculationService) {
        this.tripPlanRepository = tripPlanRepository;
        this.initiatedTripRepository = initiatedTripRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.routeCalculationService = routeCalculationService;
        this.costCalculationService = costCalculationService;
    }

    @Transactional
    public TripInitiationResponse initiateTrip(TripInitiationRequest request) {
        logger.info("Initiating trip for userId: {}", request.getUserId());

        TripPlan tripPlan = tripPlanRepository.findById(request.getTripId())
                .orElseThrow(() -> {
                    logger.error("Trip not found for tripId: {}", request.getTripId());
                    return new TripNotFoundException("Trip not found");
                });

        // Calculate route and costs
        var routeSummary = routeCalculationService.calculateRoute(tripPlan);
        var costs = costCalculationService.calculateCosts(request, tripPlan);

        // Save initiated trip
        InitiatedTrip initiatedTrip = new InitiatedTrip();
        initiatedTrip.setUserId(request.getUserId());
        initiatedTrip.setTripId(request.getTripId());
        initiatedTrip.setDriverNeeded(request.getSetDriver());
        initiatedTrip.setGuideNeeded(request.getSetGuide());
        initiatedTrip.setAverageTripDistance(costs.getAverageTripDistance());
        initiatedTrip.setAverageDriverCost(costs.getAverageDriverCost());
        initiatedTrip.setAverageGuideCost(costs.getAverageGuideCost());
        initiatedTrip.setVehicleType(request.getPreferredVehicleTypeId());
        initiatedTrip.setRouteSummary(routeSummary);

        initiatedTripRepository.save(initiatedTrip);
        logger.info("Trip initiated successfully for userId: {}", request.getUserId());

        return new TripInitiationResponse(initiatedTrip);
    }
}