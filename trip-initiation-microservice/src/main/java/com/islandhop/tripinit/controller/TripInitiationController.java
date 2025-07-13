package com.islandhop.tripinit.controller;

import com.islandhop.tripinit.dto.TripInitiationRequest;
import com.islandhop.tripinit.dto.TripInitiationResponse;
import com.islandhop.tripinit.service.TripInitiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for trip initiation endpoints.
 * Handles HTTP requests for trip initiation functionality.
 */
@RestController
@RequestMapping("/v1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripInitiationController {
    
    private final TripInitiationService tripInitiationService;
    
    /**
     * Initiates a trip based on the provided request.
     * Calculates route, costs, and saves initiated trip.
     * 
     * @param request Trip initiation request with user preferences
     * @return Trip initiation response with costs and route summary
     */
    @PostMapping("/initiate")
    public ResponseEntity<TripInitiationResponse> initiateTrip(@Valid @RequestBody TripInitiationRequest request) {
        log.info("Received trip initiation request for user: {}, tripId: {}", request.getUserId(), request.getTripId());
        
        TripInitiationResponse response = tripInitiationService.initiateTrip(request);
        
        log.info("Trip initiation completed successfully for tripId: {}", request.getTripId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}