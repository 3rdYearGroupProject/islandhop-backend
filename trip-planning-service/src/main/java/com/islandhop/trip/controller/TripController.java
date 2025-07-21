package com.islandhop.trip.controller;

import com.islandhop.trip.dto.AddPlaceResponse;
import com.islandhop.trip.dto.CreateTripRequest;
import com.islandhop.trip.dto.CreateTripResponse;
import com.islandhop.trip.dto.SuggestionErrorResponse;
import com.islandhop.trip.dto.SuggestionResponse;
import com.islandhop.trip.dto.TripPlanResponse;
import com.islandhop.trip.dto.TripSummaryResponse;
import com.islandhop.trip.dto.UpdateCityRequest;
import com.islandhop.trip.dto.UpdateCityResponse;
import com.islandhop.trip.exception.InvalidDayException;
import com.islandhop.trip.exception.InvalidTypeException;
import com.islandhop.trip.exception.TripNotFoundException;
import com.islandhop.trip.exception.UnauthorizedTripAccessException;
import com.islandhop.trip.service.TripService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Global exception handler for trip not found errors.
     *
     * @param ex The trip not found exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(TripNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<CreateTripResponse> handleTripNotFoundException(TripNotFoundException ex) {
        log.warn("Trip not found: {}", ex.getMessage());
        CreateTripResponse errorResponse = new CreateTripResponse("error", null, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Global exception handler for unauthorized trip access errors.
     *
     * @param ex The unauthorized access exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(UnauthorizedTripAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<CreateTripResponse> handleUnauthorizedTripAccessException(UnauthorizedTripAccessException ex) {
        log.warn("Unauthorized trip access: {}", ex.getMessage());
        CreateTripResponse errorResponse = new CreateTripResponse("error", null, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Global exception handler for invalid day errors.
     *
     * @param ex The invalid day exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(InvalidDayException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CreateTripResponse> handleInvalidDayException(InvalidDayException ex) {
        log.warn("Invalid day: {}", ex.getMessage());
        CreateTripResponse errorResponse = new CreateTripResponse("error", null, ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Updates the city for a specific day in a trip itinerary.
     * Validates trip ownership and day validity before updating.
     *
     * @param tripId The ID of the trip to update
     * @param day The day number to update (1-based)
     * @param request The update request containing userId and new city
     * @return ResponseEntity with the updated city details
     */
    @PostMapping("/{tripId}/day/{day}/city")
    public ResponseEntity<?> updateCity(
            @PathVariable String tripId,
            @PathVariable int day,
            @Valid @RequestBody UpdateCityRequest request) {
        
        log.info("Received city update request for trip: {}, day: {}, user: {}, city: {}", 
                tripId, day, request.getUserId(), request.getCity());

        try {
            UpdateCityResponse response = tripService.updateCity(tripId, day, request.getUserId(), request);
            log.info("Successfully updated city for trip: {}, day: {}, new city: {}", 
                    tripId, day, request.getCity());
            return ResponseEntity.ok(response);
            
        } catch (TripNotFoundException e) {
            log.warn("Trip not found: {} for user: {}", tripId, request.getUserId());
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (UnauthorizedTripAccessException e) {
            log.warn("Unauthorized access attempt: user {} for trip {}", request.getUserId(), tripId);
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (InvalidDayException e) {
            log.warn("Invalid day number: {} for trip: {}", day, tripId);
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating city for trip {}: {}", tripId, e.getMessage());
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error updating city for trip {}: {}", tripId, e.getMessage(), e);
            CreateTripResponse errorResponse = new CreateTripResponse("error", null, "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gets preference-based suggestions for a specific day and type.
     * Returns filtered suggestions based on user preferences and proximity to the selected city.
     *
     * @param tripId The ID of the trip
     * @param day The day number (1-based)
     * @param type The type of suggestions (attractions, hotels, restaurants)
     * @param userId The ID of the user making the request
     * @return ResponseEntity with list of suggestions
     */
    @GetMapping("/{tripId}/day/{day}/suggestions/{type}")
    public ResponseEntity<?> getSuggestions(
            @PathVariable String tripId,
            @PathVariable int day,
            @PathVariable String type,
            @RequestParam String userId) {
        
        log.info("Received suggestions request for trip: {}, day: {}, type: {}, user: {}", 
                tripId, day, type, userId);

        try {
            List<SuggestionResponse> suggestions = tripService.getSuggestions(tripId, day, type, userId);
            
            log.info("Successfully retrieved {} {} suggestions for trip: {}, day: {}", 
                    suggestions.size(), type, tripId, day);
            
            return ResponseEntity.ok(suggestions);
            
        } catch (TripNotFoundException e) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    "Trip not found with ID: " + tripId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (UnauthorizedTripAccessException e) {
            log.warn("Unauthorized access attempt: user {} for trip {}", userId, tripId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    "You are not authorized to access this trip");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (InvalidDayException e) {
            log.warn("Invalid day number: {} for trip: {}", day, tripId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error getting suggestions for trip {}: {}", tripId, e.getMessage());
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error getting suggestions for trip {}: {}", tripId, e.getMessage(), e);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    "Failed to retrieve suggestions. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Adds a selected place to a specific day and type in the trip itinerary.
     * Allows users to add attractions, hotels, or restaurants to their trip plans.
     *
     * @param tripId The unique ID of the trip
     * @param day The day number (1-based, 1-30)
     * @param type The type of place (attractions, hotels, restaurants)
     * @param userId Query parameter for user authentication
     * @param place The place details to add to the itinerary
     * @return ResponseEntity with AddPlaceResponse or error details
     */
    @PostMapping("/{tripId}/day/{day}/{type}")
    public ResponseEntity<?> addPlaceToItinerary(
            @PathVariable String tripId,
            @PathVariable @Min(value = 1, message = "Day must be at least 1") 
                        @Max(value = 30, message = "Day cannot exceed 30") int day,
            @PathVariable String type,
            @RequestParam String userId,
            @Valid @RequestBody SuggestionResponse place) {
        
        log.info("Received request to add place to itinerary - Trip: {}, Day: {}, Type: {}, User: {}, Place: {}", 
                tripId, day, type, userId, place.getName());

        try {
            // Add place to itinerary through service layer
            AddPlaceResponse response = tripService.addPlaceToItinerary(tripId, day, type, userId, place);
            
            log.info("Successfully added place {} to {} for trip: {}, day: {}", 
                    place.getName(), type, tripId, day);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (TripNotFoundException e) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (UnauthorizedTripAccessException e) {
            log.warn("Unauthorized access attempt: user {} for trip {}", userId, tripId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    "You are not authorized to modify this trip");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (InvalidDayException e) {
            log.warn("Invalid day number: {} for trip: {}", day, tripId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (InvalidTypeException e) {
            log.warn("Invalid type: {} for trip: {}", type, tripId);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error adding place to trip {}: {}", tripId, e.getMessage());
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error adding place to trip {}: {}", tripId, e.getMessage(), e);
            SuggestionErrorResponse errorResponse = new SuggestionErrorResponse("error", tripId, day, type, 
                    "Failed to add place to itinerary. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retrieves the complete trip plan for a given trip ID.
     * Returns all trip details including daily plans with their places.
     *
     * @param tripId The unique ID of the trip to retrieve
     * @param userId Query parameter for user authentication
     * @return ResponseEntity with TripPlanResponse or error details
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTripPlan(
            @PathVariable String tripId,
            @RequestParam String userId) {
        
        log.info("Received request to retrieve trip plan - Trip: {}, User: {}", tripId, userId);

        try {
            // Retrieve trip plan through service layer
            TripPlanResponse response = tripService.getTripPlan(tripId, userId);
            
            log.info("Successfully retrieved trip plan for trip: {} with {} daily plans", 
                    tripId, response.getDailyPlans().size());
            
            return ResponseEntity.ok(response);
            
        } catch (TripNotFoundException e) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            TripPlanResponse errorResponse = new TripPlanResponse("error", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (UnauthorizedTripAccessException e) {
            log.warn("Unauthorized access attempt: user {} for trip {}", userId, tripId);
            TripPlanResponse errorResponse = new TripPlanResponse("error", tripId, 
                    "You are not authorized to access this trip");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error retrieving trip {}: {}", tripId, e.getMessage());
            TripPlanResponse errorResponse = new TripPlanResponse("error", tripId, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error retrieving trip {}: {}", tripId, e.getMessage(), e);
            TripPlanResponse errorResponse = new TripPlanResponse("error", tripId, 
                    "Failed to retrieve trip plan. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retrieves all trips for a specific user.
     * Returns a list of trip summaries without detailed daily plans.
     *
     * @param userId Query parameter for user identification
     * @return ResponseEntity with list of TripSummaryResponse or error details
     */
    @GetMapping
    public ResponseEntity<?> getUserTrips(@RequestParam String userId) {
        
        log.info("Received request to retrieve all trips for user: {}", userId);

        try {
            // Retrieve all trips for the user through service layer
            List<TripSummaryResponse> userTrips = tripService.getUserTrips(userId);
            
            log.info("Successfully retrieved {} trips for user: {}", userTrips.size(), userId);
            
            return ResponseEntity.ok(userTrips);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error retrieving trips for user {}: {}", userId, e.getMessage());
            TripSummaryResponse errorResponse = new TripSummaryResponse("error", null, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error retrieving trips for user {}: {}", userId, e.getMessage(), e);
            TripSummaryResponse errorResponse = new TripSummaryResponse("error", null, 
                    "Failed to retrieve trips. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
