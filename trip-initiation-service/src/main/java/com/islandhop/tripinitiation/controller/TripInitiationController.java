package com.islandhop.tripinitiation.controller;

import com.islandhop.tripinitiation.dto.TripInitiationRequest;
import com.islandhop.tripinitiation.dto.TripInitiationResponse;
import com.islandhop.tripinitiation.service.TripInitiationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
public class TripInitiationController {

    private static final Logger logger = LoggerFactory.getLogger(TripInitiationController.class);
    private final TripInitiationService tripInitiationService;

    public TripInitiationController(TripInitiationService tripInitiationService) {
        this.tripInitiationService = tripInitiationService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<TripInitiationResponse> initiateTrip(@RequestBody TripInitiationRequest request) {
        logger.info("Initiating trip for userId: {}", request.getUserId());
        try {
            TripInitiationResponse response = tripInitiationService.initiateTrip(request);
            logger.info("Trip initiated successfully for tripId: {}", response.getTripId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error initiating trip: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}