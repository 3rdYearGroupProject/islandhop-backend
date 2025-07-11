package com.islandhop.pooling.controller;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for group management operations.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Group Management", description = "APIs for managing travel groups and collaborative trip planning")
public class GroupController {
    
    private final GroupService groupService;
    
    /**
     * Create a new group.
     */
    @PostMapping
    @Operation(summary = "Create a new group", description = "Create a private or public group, linking to an existing or new TripPlan")
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            CreateGroupResponse response = groupService.createGroup(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GroupCreationException e) {
            log.warn("Group creation failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PoolingErrorResponse("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating group for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", "Internal server error"));
        }
    }
    
    /**
     * Invite a user to a private group.
     */
    @PostMapping("/{groupId}/invite")
    @Operation(summary = "Invite user to private group", description = "Invite a known user to a private group")
    public ResponseEntity<?> inviteUser(
            @PathVariable String groupId,
            @Valid @RequestBody InviteUserRequest request,
            @Parameter(description = "Group creator's user ID") @RequestParam String userId) {
        
        try {
            InviteUserResponse response = groupService.inviteUser(groupId, request, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for invite: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            log.warn("Unauthorized group access or invalid operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error inviting user to group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * Add a place to group itinerary.
     */
    @PostMapping("/{groupId}/itinerary/day/{day}/{type}")
    @Operation(summary = "Add place to group itinerary", description = "Add a place (attraction, hotel, restaurant) to a specific day in the group's TripPlan")
    public ResponseEntity<?> addPlaceToItinerary(
            @PathVariable String groupId,
            @PathVariable int day,
            @PathVariable String type,
            @Valid @RequestBody SuggestionResponse place,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            GroupItineraryResponse response = groupService.addPlaceToGroupItinerary(groupId, day, type, place, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for itinerary update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized group access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (InvalidDayException | InvalidTypeException e) {
            log.warn("Invalid day or type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (GroupItineraryException e) {
            log.error("Group itinerary error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error adding place to group {} itinerary: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * Update city in group itinerary.
     */
    @PatchMapping("/{groupId}/itinerary/day/{day}/city")
    @Operation(summary = "Update city in group itinerary", description = "Update the city for a specific day in the group's TripPlan")
    public ResponseEntity<?> updateCity(
            @PathVariable String groupId,
            @PathVariable int day,
            @Valid @RequestBody UpdateCityRequest request,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            GroupItineraryResponse response = groupService.updateCityInGroupItinerary(groupId, day, request, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for city update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized group access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (InvalidDayException e) {
            log.warn("Invalid day: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (GroupItineraryException e) {
            log.error("Group itinerary error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating city for group {} itinerary: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * Get group details.
     */
    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details", description = "Retrieve group details, including members, preferences, and actions")
    public ResponseEntity<?> getGroupDetails(
            @PathVariable String groupId,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            GroupDetailsResponse response = groupService.getGroupDetails(groupId, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized group access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting group {} details: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * List public groups.
     */
    @GetMapping("/public")
    @Operation(summary = "List public groups", description = "List public groups for users to browse and join")
    public ResponseEntity<?> listPublicGroups(
            @Parameter(description = "User ID from JWT token") @RequestParam String userId,
            @Parameter(description = "Optional filters") @RequestParam(required = false) Map<String, String> filters) {
        
        try {
            List<PublicGroupResponse> response = groupService.listPublicGroups(userId, filters);
            if (response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new PoolingErrorResponse("error", "No public groups found"));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error listing public groups: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", "Internal server error"));
        }
    }
    
    /**
     * Request to join a public group.
     */
    @PostMapping("/{groupId}/join")
    @Operation(summary = "Request to join public group", description = "Submit a join request for a public group with user preferences")
    public ResponseEntity<?> requestToJoinGroup(
            @PathVariable String groupId,
            @Valid @RequestBody JoinGroupRequest request,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            JoinGroupResponse response = groupService.requestToJoinGroup(groupId, request, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for join request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (InvalidGroupOperationException e) {
            log.warn("Invalid group operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error joining group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * Approve or reject join request.
     */
    @PatchMapping("/{groupId}/join/{joinerUserId}")
    @Operation(summary = "Approve/reject join request", description = "Approve or reject a join request for a public group")
    public ResponseEntity<?> processJoinRequest(
            @PathVariable String groupId,
            @PathVariable String joinerUserId,
            @Valid @RequestBody JoinRequestDecisionRequest request,
            @Parameter(description = "Group creator's user ID") @RequestParam String creatorUserId) {
        
        try {
            JoinRequestDecisionResponse response = groupService.processJoinRequest(groupId, joinerUserId, request, creatorUserId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException | JoinRequestNotFoundException e) {
            log.warn("Group or join request not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized group access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PoolingErrorResponse("error", groupId, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing join request for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", groupId, "Internal server error"));
        }
    }
    
    /**
     * Get scored trip suggestions.
     */
    @PostMapping("/public/suggestions")
    @Operation(summary = "Get scored trip suggestions", description = "Suggest public trips based on user preferences and travel dates, ranked by compatibility score")
    public ResponseEntity<?> getScoredTripSuggestions(
            @Valid @RequestBody TripSuggestionsRequest request,
            @Parameter(description = "User ID from JWT token") @RequestParam String userId) {
        
        try {
            List<TripSuggestionResponse> response = groupService.getScoredTripSuggestions(request, userId);
            if (response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new PoolingErrorResponse("error", "No matching trips found"));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error getting trip suggestions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PoolingErrorResponse("error", "Internal server error"));
        }
    }
}
