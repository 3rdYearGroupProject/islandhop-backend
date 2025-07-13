package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.repository.TripPlanRepository;
import com.islandhop.tripinitiation.exception.TripNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteCalculationServiceTest {

    @InjectMocks
    private RouteCalculationService routeCalculationService;

    @Mock
    private TripPlanRepository tripPlanRepository;

    private TripPlan tripPlan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tripPlan = new TripPlan();
        tripPlan.setUserId("testUserId");
        tripPlan.setTripName("Test Trip");
        tripPlan.setStartDate("2025-07-20");
        tripPlan.setEndDate("2025-07-22");
        // Initialize other fields as necessary
    }

    @Test
    void testRetrieveTripPlan_Success() {
        when(tripPlanRepository.findById("testTripId")).thenReturn(java.util.Optional.of(tripPlan));

        TripPlan retrievedTripPlan = routeCalculationService.retrieveTripPlan("testTripId", "testUserId");

        assertNotNull(retrievedTripPlan);
        assertEquals("Test Trip", retrievedTripPlan.getTripName());
        verify(tripPlanRepository, times(1)).findById("testTripId");
    }

    @Test
    void testRetrieveTripPlan_TripNotFound() {
        when(tripPlanRepository.findById("invalidTripId")).thenReturn(java.util.Optional.empty());

        assertThrows(TripNotFoundException.class, () -> {
            routeCalculationService.retrieveTripPlan("invalidTripId", "testUserId");
        });
        verify(tripPlanRepository, times(1)).findById("invalidTripId");
    }

    // Additional tests for route calculation logic can be added here
}