package com.islandhop.trip.controller;

import com.islandhop.trip.dto.CreateTripRequest;
import com.islandhop.trip.dto.CreateTripResponse;
import com.islandhop.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for trip planning operations.
 * Handles HTTP requests for creating and managing trip itineraries.
 */
@RestController
@RequestMapping("/v1/itinerary")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    /**
     * Creates a new trip itinerary.
     * Initializes a trip plan with empty daily plans for the specified date range.
     *
     * @param request The trip creation request containing user input
     * @return ResponseEntity with the created trip details
     */
    @PostMapping("/initiate")
    public ResponseEntity<CreateTripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        log.info("Received trip creation request for user: {} with trip name: {}", 
                request.getUserId(), request.getTripName());

        try {
            CreateTripResponse response = tripService.createTrip(request);
            log.info("Successfully created trip with ID: {} for user: {}", 
                    response.getTripId(), request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in trip creation for user {}: {}", request.getUserId(), e.getMessage());
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error creating trip for user {}: {}", request.getUserId(), e.getMessage(), e);
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Global exception handler for validation errors.
     * Handles Jakarta validation annotation failures.
     *
     * @param ex The validation exception
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);

        log.warn("Validation errors: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Global exception handler for runtime errors.
     * Handles unexpected server errors.
     *
     * @param ex The runtime exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CreateTripResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error in trip controller: {}", ex.getMessage(), ex);
        CreateTripResponse errorResponse = new CreateTripResponse("error", null, "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
