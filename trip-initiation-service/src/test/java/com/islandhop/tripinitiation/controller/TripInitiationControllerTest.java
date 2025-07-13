package com.islandhop.tripinitiation.controller;

import com.islandhop.tripinitiation.dto.TripInitiationRequest;
import com.islandhop.tripinitiation.dto.TripInitiationResponse;
import com.islandhop.tripinitiation.service.TripInitiationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TripInitiationControllerTest {

    @InjectMocks
    private TripInitiationController tripInitiationController;

    @Mock
    private TripInitiationService tripInitiationService;

    private TripInitiationRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new TripInitiationRequest();
        request.setUserId("testUserId");
        request.setTripId("testTripId");
        request.setSetDriver(1);
        request.setSetGuide(1);
        request.setPreferredVehicleTypeId("testVehicleTypeId");
    }

    @Test
    public void testInitiateTrip() {
        TripInitiationResponse response = new TripInitiationResponse();
        response.setTripId("testTripId");
        response.setUserId("testUserId");
        response.setAverageTripDistance(100.0);
        response.setAverageDriverCost(200.0);
        response.setAverageGuideCost(150.0);
        response.setVehicleType("testVehicleType");

        when(tripInitiationService.initiateTrip(any(TripInitiationRequest.class))).thenReturn(response);

        ResponseEntity<TripInitiationResponse> result = tripInitiationController.initiateTrip(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void testInitiateTrip_NotFound() {
        when(tripInitiationService.initiateTrip(any(TripInitiationRequest.class))).thenThrow(new RuntimeException("Trip not found"));

        ResponseEntity<TripInitiationResponse> result = tripInitiationController.initiateTrip(request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testInitiateTrip_BadRequest() {
        request.setTripId(null); // Invalid request

        ResponseEntity<TripInitiationResponse> result = tripInitiationController.initiateTrip(request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}