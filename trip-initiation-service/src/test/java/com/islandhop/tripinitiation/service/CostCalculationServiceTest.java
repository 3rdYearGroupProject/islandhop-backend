package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.model.mongo.InitiatedTrip;
import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.repository.InitiatedTripRepository;
import com.islandhop.tripinitiation.repository.TripPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class CostCalculationServiceTest {

    @InjectMocks
    private CostCalculationService costCalculationService;

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private InitiatedTripRepository initiatedTripRepository;

    private TripPlan tripPlan;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tripPlan = new TripPlan();
        tripPlan.setUserId("user123");
        tripPlan.setTripName("Test Trip");
        tripPlan.setStartDate("2025-07-20");
        tripPlan.setEndDate("2025-07-22");
        tripPlan.setDailyPlans(new ArrayList<>());
        // Add more setup as needed
    }

    @Test
    public void testCalculateDriverCost() {
        when(tripPlanRepository.findById("tripId123")).thenReturn(Optional.of(tripPlan));
        double distance = 100.0; // Example distance
        double pricePerKm = 10.0; // Example price per km
        double driverFeePerDay = 50.0; // Example driver fee per day
        int numberOfDays = 3; // Example number of trip days

        double expectedCost = (distance * pricePerKm) + (driverFeePerDay * numberOfDays);
        double actualCost = costCalculationService.calculateDriverCost(distance, pricePerKm, driverFeePerDay, numberOfDays);

        assertEquals(expectedCost, actualCost);
    }

    @Test
    public void testCalculateGuideCost() {
        // Implement test for guide cost calculation
    }

    // Add more tests as needed
}