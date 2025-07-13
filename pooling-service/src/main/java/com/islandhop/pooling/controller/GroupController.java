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
@RequestMapping("/v1/groups")
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
     * Gets list of public groups.
     *
     * @param userId The requesting user's ID
     * @return ResponseEntity with list of public groups
     */
    @GetMapping("/public")
    public ResponseEntity<List<PublicGroupResponse>> getPublicGroups(@RequestParam String userId) {
        try {
            log.info("Getting public groups for user '{}'", userId);
            List<PublicGroupResponse> response = groupService.getPublicGroups(userId);
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
