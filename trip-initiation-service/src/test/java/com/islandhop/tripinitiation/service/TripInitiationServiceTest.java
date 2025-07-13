package com.islandhop.tripinitiation.service;

import com.islandhop.tripinitiation.dto.TripInitiationRequest;
import com.islandhop.tripinitiation.dto.TripInitiationResponse;
import com.islandhop.tripinitiation.exception.TripNotFoundException;
import com.islandhop.tripinitiation.exception.VehicleTypeNotFoundException;
import com.islandhop.tripinitiation.model.mongo.TripPlan;
import com.islandhop.tripinitiation.model.postgres.VehicleType;
import com.islandhop.tripinitiation.repository.TripPlanRepository;
import com.islandhop.tripinitiation.repository.VehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TripInitiationServiceTest {

    @InjectMocks
    private TripInitiationService tripInitiationService;

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitiateTrip_Success() {
        TripInitiationRequest request = new TripInitiationRequest("userId", "tripId", 1, 1, "vehicleTypeId");
        TripPlan tripPlan = new TripPlan(); // Assume this is populated with necessary data
        VehicleType vehicleType = new VehicleType(); // Assume this is populated with necessary data

        when(tripPlanRepository.findByIdAndUserId(request.getTripId(), request.getUserId())).thenReturn(Optional.of(tripPlan));
        when(vehicleTypeRepository.findById(request.getPreferredVehicleTypeId())).thenReturn(Optional.of(vehicleType));

        TripInitiationResponse response = tripInitiationService.initiateTrip(request);

        assertNotNull(response);
        assertEquals(request.getTripId(), response.getTripId());
        verify(tripPlanRepository, times(1)).findByIdAndUserId(request.getTripId(), request.getUserId());
        verify(vehicleTypeRepository, times(1)).findById(request.getPreferredVehicleTypeId());
    }

    @Test
    void testInitiateTrip_TripNotFound() {
        TripInitiationRequest request = new TripInitiationRequest("userId", "tripId", 1, 1, "vehicleTypeId");

        when(tripPlanRepository.findByIdAndUserId(request.getTripId(), request.getUserId())).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripInitiationService.initiateTrip(request));
    }

    @Test
    void testInitiateTrip_VehicleTypeNotFound() {
        TripInitiationRequest request = new TripInitiationRequest("userId", "tripId", 1, 1, "vehicleTypeId");
        TripPlan tripPlan = new TripPlan(); // Assume this is populated with necessary data

        when(tripPlanRepository.findByIdAndUserId(request.getTripId(), request.getUserId())).thenReturn(Optional.of(tripPlan));
        when(vehicleTypeRepository.findById(request.getPreferredVehicleTypeId())).thenReturn(Optional.empty());

        assertThrows(VehicleTypeNotFoundException.class, () -> tripInitiationService.initiateTrip(request));
    }
}