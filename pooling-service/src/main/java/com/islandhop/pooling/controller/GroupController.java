package com.islandhop.pooling.controller;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.service.GroupService;
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
 * REST controller for group management operations.
 * Handles HTTP requests for creating and managing travel groups.
 * Follows the same patterns as TripController for consistency.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {
    
    private final GroupService groupService;
    
    /**
     * Creates a new travel group.
     * Can be linked to an existing trip or create a new one.
     *
     * @param request The group creation request containing user input
     * @return ResponseEntity with the created group details
     */
    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            log.info("Creating group '{}' for user '{}'", request.getGroupName(), request.getUserId());
            CreateGroupResponse response = groupService.createGroup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GroupCreationException e) {
            log.warn("Group creation failed for user {}: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating group for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new GroupCreationException("Failed to create group: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new public pooling group with trip planning.
     * This endpoint creates a group and trip simultaneously for public pooling.
     *
     * @param request The group with trip creation request
     * @return ResponseEntity with the created group and trip details
     */
    @PostMapping("/with-trip")
    public ResponseEntity<CreateGroupWithTripResponse> createGroupWithTrip(@Valid @RequestBody CreateGroupWithTripRequest request) {
        try {
            log.info("Creating group with trip '{}' for user '{}'", request.getGroupName(), request.getUserId());
            CreateGroupWithTripResponse response = groupService.createGroupWithTrip(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GroupCreationException e) {
            log.warn("Group with trip creation failed for user {}: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating group with trip for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new GroupCreationException("Failed to create group with trip: " + e.getMessage());
        }
    }
    
    /**
     * Gets trip suggestions for a group based on compatibility.
     * This endpoint is called when user wants to finalize their trip to check for similar groups.
     *
     * @param groupId The ID of the group
     * @param userId The requesting user's ID
     * @return ResponseEntity with trip suggestions
     */
    @GetMapping("/{groupId}/trip-suggestions")
    public ResponseEntity<TripSuggestionsResponse> getTripSuggestions(
            @PathVariable String groupId,
            @RequestParam String userId) {
        try {
            log.info("Getting trip suggestions for group '{}' by user '{}'", groupId, userId);
            TripSuggestionsResponse response = groupService.getTripSuggestions(groupId, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for trip suggestions: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to trip suggestions: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting trip suggestions for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get trip suggestions: " + e.getMessage());
        }
    }
    
    /**
     * Finalizes a trip or joins an existing group.
     * This endpoint handles the user's choice after seeing trip suggestions.
     *
     * @param groupId The ID of the group
     * @param request The finalize trip request
     * @return ResponseEntity with finalization result
     */
    @PostMapping("/{groupId}/finalize-trip")
    public ResponseEntity<FinalizeTripResponse> finalizeTrip(
            @PathVariable String groupId,
            @Valid @RequestBody FinalizeTripRequest request) {
        try {
            log.info("Finalizing trip for group '{}' by user '{}' with action '{}'", groupId, request.getUserId(), request.getAction());
            FinalizeTripResponse response = groupService.finalizeTrip(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for trip finalization: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to finalize trip: {}", e.getMessage());
            throw e;
        } catch (InvalidGroupOperationException e) {
            log.warn("Invalid trip finalization operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error finalizing trip for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to finalize trip: " + e.getMessage());
        }
    }
    
    /**
     * Invites a user to a private group.
     *
     * @param groupId The ID of the group
     * @param request The invitation request
     * @return ResponseEntity with the invitation response
     */
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @PathVariable String groupId,
            @Valid @RequestBody InviteUserRequest request) {
        try {
            log.info("Inviting user to group '{}' by user '{}'", groupId, request.getUserId());
            InviteUserResponse response = groupService.inviteUser(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for invite: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            log.warn("Unauthorized group access or invalid operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error inviting user to group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to invite user: " + e.getMessage());
        }
    }
    
    /**
     * Requests to join a public group.
     *
     * @param groupId The ID of the group
     * @param request The join request
     * @return ResponseEntity with the join response
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<JoinGroupResponse> joinGroup(
            @PathVariable String groupId,
            @Valid @RequestBody JoinGroupRequest request) {
        try {
            log.info("User '{}' requesting to join group '{}'", request.getUserId(), groupId);
            JoinGroupResponse response = groupService.joinGroup(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for join request: {}", e.getMessage());
            throw e;
        } catch (InvalidGroupOperationException e) {
            log.warn("Invalid join operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error joining group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to join group: " + e.getMessage());
        }
    }
    
    /**
     * Gets group details.
     *
     * @param groupId The ID of the group
     * @param userId The requesting user's ID
     * @return ResponseEntity with group details
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(
            @PathVariable String groupId,
            @RequestParam String userId) {
        try {
            log.info("Getting group details for '{}' by user '{}'", groupId, userId);
            GroupDetailsResponse response = groupService.getGroupDetails(groupId, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting group details for {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get group details: " + e.getMessage());
        }
    }
    
    /**
     * Gets list of public groups with optional filtering.
     * Enhanced to support filtering by preferences and compatibility scoring.
     * 
     * @param userId The requesting user's ID
     * @param baseCity Optional filter by base city
     * @param startDate Optional filter by start date
     * @param endDate Optional filter by end date  
     * @param budgetLevel Optional filter by budget level
     * @param preferredActivities Optional filter by preferred activities
     * @return ResponseEntity with filtered list of public groups
     */
    @GetMapping("/public")
    public ResponseEntity<List<PublicGroupResponse>> getPublicGroups(
            @RequestParam String userId,
            @RequestParam(required = false) String baseCity,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String budgetLevel,
            @RequestParam(required = false) List<String> preferredActivities) {
        try {
            log.info("Getting public groups for user '{}' with filters: baseCity={}, startDate={}, endDate={}, budgetLevel={}, activities={}", 
                    userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
            
            List<PublicGroupResponse> response = groupService.getPublicGroups(
                userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error getting public groups for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get public groups: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint for Pooling Service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("service", "pooling-service");
        return ResponseEntity.ok(status);
    }
    
    /**
     * Responds to an invitation (accept or reject).
     *
     * @param request The invitation response request
     * @return ResponseEntity with the response details
     */
    @PostMapping("/invitations/respond")
    public ResponseEntity<InvitationListResponse> respondToInvitation(@Valid @RequestBody InvitationResponseRequest request) {
        try {
            log.info("User '{}' responding to invitation '{}'", request.getUserId(), request.getInvitationId());
            InvitationListResponse response = groupService.respondToInvitation(request);
            return ResponseEntity.ok(response);
        } catch (JoinRequestNotFoundException e) {
            log.warn("Invitation not found: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            log.warn("Invalid invitation response: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error responding to invitation: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to respond to invitation: " + e.getMessage());
        }
    }
    
    /**
     * Approves or rejects a join request.
     *
     * @param groupId The ID of the group
     * @param request The approval request
     * @return ResponseEntity with the approval response
     */
    @PostMapping("/{groupId}/requests/approve")
    public ResponseEntity<JoinGroupResponse> approveJoinRequest(
            @PathVariable String groupId,
            @Valid @RequestBody ApproveJoinRequestRequest request) {
        try {
            log.info("Approving join request for group '{}' by user '{}'", groupId, request.getUserId());
            JoinGroupResponse response = groupService.approveJoinRequest(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for approval: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException | InvalidGroupOperationException | JoinRequestNotFoundException e) {
            log.warn("Invalid join request approval: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error approving join request for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to approve join request: " + e.getMessage());
        }
    }
    
    /**
     * Gets pending invitations for a user.
     *
     * @param userId The user ID
     * @return ResponseEntity with the user's invitations
     */
    @GetMapping("/invitations/{userId}")
    public ResponseEntity<InvitationListResponse> getUserInvitations(@PathVariable String userId) {
        try {
            log.info("Getting invitations for user '{}'", userId);
            InvitationListResponse response = groupService.getUserInvitations(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error getting invitations for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get invitations: " + e.getMessage());
        }
    }
    
    /**
     * Allows a group member to vote on a join request.
     * Supports multi-member approval system where all members must approve.
     *
     * @param groupId The ID of the group
     * @param request The member vote request
     * @return ResponseEntity with the vote response
     */
    @PostMapping("/{groupId}/join-requests/vote")
    public ResponseEntity<MemberVoteResponse> voteOnJoinRequest(
            @PathVariable String groupId,
            @Valid @RequestBody MemberVoteRequest request) {
        try {
            log.info("Member '{}' voting on join request '{}' for group '{}'", 
                     request.getUserId(), request.getJoinRequestId(), groupId);
            MemberVoteResponse response = groupService.voteOnJoinRequest(groupId, request);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for member vote: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException | InvalidGroupOperationException | JoinRequestNotFoundException e) {
            log.warn("Invalid member vote operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing member vote for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to process member vote: " + e.getMessage());
        }
    }
    
    /**
     * Gets pending join requests for a group that require member votes.
     *
     * @param groupId The ID of the group
     * @param userId The requesting user's ID
     * @return ResponseEntity with pending join requests
     */
    @GetMapping("/{groupId}/join-requests/pending")
    public ResponseEntity<PendingJoinRequestsResponse> getPendingJoinRequests(
            @PathVariable String groupId,
            @RequestParam String userId) {
        try {
            log.info("Getting pending join requests for group '{}' by user '{}'", groupId, userId);
            PendingJoinRequestsResponse response = groupService.getPendingJoinRequests(groupId, userId);
            return ResponseEntity.ok(response);
        } catch (GroupNotFoundException e) {
            log.warn("Group not found for pending join requests: {}", e.getMessage());
            throw e;
        } catch (UnauthorizedGroupAccessException e) {
            log.warn("Unauthorized access to pending join requests: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting pending join requests for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get pending join requests: " + e.getMessage());
        }
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
     * Handles join request not found exceptions.
     */
    @ExceptionHandler(JoinRequestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleJoinRequestNotFoundException(JoinRequestNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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
