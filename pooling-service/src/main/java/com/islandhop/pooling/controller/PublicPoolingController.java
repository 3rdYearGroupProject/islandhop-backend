package com.islandhop.pooling.controller;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.service.PublicPoolingService;
import jakarta.validation.Valid;
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
 * REST controller for public pooling group operations.
 * Handles creation of public pooling groups with trip comparison and suggestions.
 * Separate from GroupController to maintain clean separation of concerns.
 */
@RestController
@RequestMapping("/api/v1/public-pooling")
@RequiredArgsConstructor
@Slf4j
public class PublicPoolingController {
    
    private final PublicPoolingService publicPoolingService;
    
    /**
     * Pre-checks for compatible public groups before creating a new one.
     * Allows users to see existing options before starting their own group.
     */
    @PostMapping("/pre-check")
    public ResponseEntity<PreCheckResponse> preCheckGroups(@Valid @RequestBody PreCheckRequest request) {
        try {
            log.info("Pre-checking compatible groups for user '{}' in '{}' from {} to {}", 
                    request.getUserId(), request.getBaseCity(), request.getStartDate(), request.getEndDate());
            
            PreCheckResponse response = publicPoolingService.preCheckGroups(request);
            
            if ("error".equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Unexpected error in pre-check for user {}: {}", request.getUserId(), e.getMessage(), e);
            
            PreCheckResponse errorResponse = new PreCheckResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Failed to pre-check groups: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Creates a new public pooling group.
     * This endpoint allows trip planning first, then suggests similar groups before saving.
     *
     * @param request The public pooling group creation request
     * @return ResponseEntity with the created group details
     */
    @PostMapping("/groups")
    public ResponseEntity<CreatePublicPoolingGroupResponse> createPublicPoolingGroup(
            @Valid @RequestBody CreatePublicPoolingGroupRequest request) {
        try {
            log.info("Creating public pooling group '{}' for user '{}'", request.getGroupName(), request.getUserId());
            CreatePublicPoolingGroupResponse response = publicPoolingService.createPublicPoolingGroup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GroupCreationException e) {
            log.warn("Public pooling group creation failed for user {}: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating public pooling group for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new GroupCreationException("Failed to create public pooling group: " + e.getMessage());
        }
    }
    
    /**
     * Saves a trip and gets suggestions for similar public pooling groups.
     * This is called after trip planning is complete to check for compatible groups.
     *
     * @param groupId The ID of the group
     * @param request The save trip request
     * @return ResponseEntity with suggestions or confirmation
     */
    @PostMapping("/groups/{groupId}/save-trip")
    public ResponseEntity<SaveTripWithSuggestionsResponse> saveTripWithSuggestions(
            @PathVariable String groupId,
            @Valid @RequestBody SaveTripRequest request) {
        try {
            log.info("Saving trip for public pooling group '{}' by user '{}'", groupId, request.getUserId());
            SaveTripWithSuggestionsResponse response = publicPoolingService.saveTripWithSuggestions(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Public pooling group not found for save trip: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to save trip: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving trip for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to save trip: " + e.getMessage());
        }
    }
    
    /**
     * Joins an existing public pooling group (discards the new group).
     * This is called when user chooses to join a suggested group.
     *
     * @param groupId The ID of the group to discard
     * @param request The join existing group request
     * @return ResponseEntity with join confirmation
     */
    @PostMapping("/groups/{groupId}/join-existing")
    public ResponseEntity<JoinExistingGroupResponse> joinExistingGroup(
            @PathVariable String groupId,
            @Valid @RequestBody JoinExistingGroupRequest request) {
        try {
            log.info("User '{}' joining existing group '{}' and discarding group '{}'", 
                     request.getUserId(), request.getTargetGroupId(), groupId);
            JoinExistingGroupResponse response = publicPoolingService.joinExistingGroup(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for join existing: {}", e.getMessage());
            throw e;
        } catch (InvalidGroupOperationException e) {
            log.warn("Invalid join existing operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error joining existing group: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to join existing group: " + e.getMessage());
        }
    }
    
    /**
     * Finalizes a public pooling group (keeps the new group).
     * This is called when user chooses to proceed with their new group.
     *
     * @param groupId The ID of the group to finalize
     * @param request The finalize group request
     * @return ResponseEntity with finalization confirmation
     */
    @PostMapping("/groups/{groupId}/finalize")
    public ResponseEntity<FinalizeGroupResponse> finalizeGroup(
            @PathVariable String groupId,
            @Valid @RequestBody FinalizeGroupRequest request) {
        try {
            log.info("Finalizing public pooling group '{}' by user '{}'", groupId, request.getUserId());
            FinalizeGroupResponse response = publicPoolingService.finalizeGroup(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for finalization: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to finalize group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error finalizing group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to finalize group: " + e.getMessage());
        }
    }
    
    /**
     * Gets compatible public pooling groups for a trip.
     * This endpoint can be used to manually check compatibility.
     *
     * @param tripId The ID of the trip to check compatibility for
     * @param userId The requesting user's ID
     * @return ResponseEntity with list of compatible groups
     */
    @GetMapping("/groups/compatible/{tripId}")
    public ResponseEntity<List<CompatibleGroupResponse>> getCompatibleGroups(
            @PathVariable String tripId,
            @RequestParam String userId) {
        try {
            log.info("Getting compatible groups for trip '{}' by user '{}'", tripId, userId);
            List<CompatibleGroupResponse> response = publicPoolingService.getCompatibleGroups(tripId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error getting compatible groups for trip {}: {}", tripId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get compatible groups: " + e.getMessage());
        }
    }
    
    /**
     * Gets comprehensive trip details including itinerary and joined group members.
     * This endpoint is publicly accessible and provides trip information for both logged-in and anonymous users.
     * Does not include sensitive information like invitations or join requests.
     *
     * @param tripId The ID of the trip
     * @param userId The ID of the user making the request (optional, for personalization)
     * @return ResponseEntity with ComprehensiveTripResponse or error details
     */
    @GetMapping("/trips/{tripId}/comprehensive")
    public ResponseEntity<?> getComprehensiveTripDetails(
            @PathVariable String tripId,
            @RequestParam(required = false) String userId) {
        try {
            log.info("Getting comprehensive trip details for trip '{}' requested by user '{}'", tripId, userId != null ? userId : "anonymous");
            ComprehensiveTripResponse response = publicPoolingService.getComprehensiveTripDetails(tripId, userId);
            return ResponseEntity.ok(response);
        } catch (TripNotFoundException e) {
            log.warn("Trip not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error getting comprehensive trip details for trip {}: {}", tripId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve comprehensive trip details. Please try again later."
            ));
        }
    }

    /**
     * Health check endpoint for Public Pooling Service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("service", "public-pooling-service");
        return ResponseEntity.ok(status);
    }
    
    /**
     * Handles validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", "error");
        errors.put("message", "Validation failed");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errors.put("errors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Handles group creation exceptions.
     */
    @ExceptionHandler(GroupCreationException.class)
    public ResponseEntity<Map<String, String>> handleGroupCreationException(GroupCreationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Handles group not found exceptions.
     */
    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleGroupNotFoundException(GroupNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles unauthorized access exceptions.
     */
    @ExceptionHandler(UnauthorizedGroupAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedAccessException(UnauthorizedGroupAccessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    /**
     * Handles invalid group operation exceptions.
     */
    @ExceptionHandler(InvalidGroupOperationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidGroupOperationException(InvalidGroupOperationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "An unexpected error occurred");
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
