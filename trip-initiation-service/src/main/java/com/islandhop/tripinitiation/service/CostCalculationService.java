package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.model.postgres.VehicleType;
import com.islandhop.tripinitiation.model.postgres.GuideFee;
import com.islandhop.tripinitiation.repository.VehicleTypeRepository;
import com.islandhop.tripinitiation.repository.GuideFeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CostCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(CostCalculationService.class);
    
    private final VehicleTypeRepository vehicleTypeRepository;
    private final GuideFeeRepository guideFeeRepository;

    public CostCalculationService(VehicleTypeRepository vehicleTypeRepository, GuideFeeRepository guideFeeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.guideFeeRepository = guideFeeRepository;
    }

    public double calculateDriverCost(double totalDistance, String vehicleTypeId, double driverFeePerDay, int tripDays) {
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle type not found"));
        double vehicleCost = totalDistance * vehicleType.getPricePerKm();
        double totalDriverCost = vehicleCost + (driverFeePerDay * tripDays);
        logger.debug("Calculated driver cost: {}", totalDriverCost);
        return totalDriverCost;
    }

    public double calculateGuideCost(List<String> cities, boolean setGuide) {
        if (!setGuide) {
            return 0;
        }
        double totalGuideCost = 0;
        for (String city : cities) {
            GuideFee guideFee = guideFeeRepository.findByCity(city)
                    .orElseThrow(() -> new IllegalArgumentException("Guide fee not found for city: " + city));
            totalGuideCost += guideFee.getPricePerDay();
        }
        logger.debug("Calculated guide cost: {}", totalGuideCost);
        return totalGuideCost;
    }
}