package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.GroupAction;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.model.Invitation;
import com.islandhop.pooling.repository.GroupRepository;
import com.islandhop.pooling.repository.InvitationRepository;
import com.islandhop.pooling.client.ItineraryServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing groups and their operations.
 * Follows the same patterns as TripService for consistency.
 * Enhanced with invitation and approval system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final InvitationRepository invitationRepository;
    private final ItineraryServiceClient itineraryServiceClient;
    private final TripCompatibilityService tripCompatibilityService;
    
    /**
     * Creates a new travel group for an existing trip.
     * Groups are transparent to users - they only see trip collaboration.
     *
     * @param request The group creation request
     * @return CreateGroupResponse with group details
     * @throws GroupCreationException if validation fails
     */
    public CreateGroupResponse createGroup(CreateGroupRequest request) {
        log.info("Creating collaboration group for trip '{}' by user {}", request.getTripId(), request.getUserId());
        
        try {
            // Validate input
            validateCreateGroupRequest(request);
            
            // Generate group ID
            String groupId = UUID.randomUUID().toString();
            
            // Create group entity
            Group group = new Group();
            group.setId(groupId);
            group.setTripId(request.getTripId());
            group.setVisibility(request.getVisibility());
            group.setPreferences(request.getPreferences());
            group.setUserIds(List.of(request.getUserId()));
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Add creation action
            GroupAction createAction = GroupAction.create(
                request.getUserId(),
                "COLLABORATION_STARTED",
                "Started trip collaboration for trip: " + request.getTripId()
            );
            group.setActions(List.of(createAction));
            
            // Save group
            Group savedGroup = groupRepository.save(group);
            
            // Create response
            CreateGroupResponse response = new CreateGroupResponse();
            response.setStatus("success");
            response.setGroupId(savedGroup.getId());
            response.setTripId(savedGroup.getTripId());
            response.setMessage("Trip collaboration started successfully");
            
            log.info("Trip collaboration group created with ID: {}", groupId);
            return response;
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid group creation request: {}", e.getMessage());
            throw new GroupCreationException("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating group: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to create group: " + e.getMessage());
        }
    }
    
    /**
     * Sends an invitation to a user to join a private group.
     * Creates a pending invitation that requires user acceptance.
     */
    public InviteUserResponse inviteUser(String groupId, InviteUserRequest request) {
        log.info("Creating invitation for group '{}' by user '{}'", groupId, request.getUserId());
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Validate permissions
            if (!group.isMember(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("User is not a member of this group");
            }
            
            if (group.isPublic()) {
                throw new InvalidGroupOperationException("Cannot invite users to public groups");
            }
            
            if (group.isFull()) {
                throw new InvalidGroupOperationException("Group is full (max " + group.getMaxMembers() + " members)");
            }
            
            // Validate invitation target
            String invitedUserId = request.getInvitedUserId();
            String invitedEmail = request.getInvitedEmail();
            
            if (invitedUserId == null && invitedEmail == null) {
                throw new InvalidGroupOperationException("Either user ID or email must be provided for invitation");
            }
            
            // Check if user is already a member (if user ID provided)
            if (invitedUserId != null && group.isMember(invitedUserId)) {
                throw new InvalidGroupOperationException("User is already a member of this group");
            }
            
            // Check for existing pending invitation
            if (invitedUserId != null) {
                Optional<Invitation> existingInvitation = invitationRepository
                    .findByGroupIdAndInvitedUserId(groupId, invitedUserId);
                if (existingInvitation.isPresent() && existingInvitation.get().isPending()) {
                    throw new InvalidGroupOperationException("User already has a pending invitation to this group");
                }
            }
            
            // Create invitation
            Invitation invitation = new Invitation();
            invitation.setId(UUID.randomUUID().toString());
            invitation.setGroupId(groupId);
            invitation.setTripId(group.getTripId());
            invitation.setTripName("Trip " + group.getTripId()); // TODO: Fetch actual trip name from trip service
            invitation.setInviterUserId(request.getUserId());
            invitation.setInvitedUserId(invitedUserId);
            invitation.setInvitedEmail(invitedEmail);
            invitation.setMessage(request.getMessage());
            invitation.setStatus("pending");
            invitation.setInvitedAt(Instant.now());
            invitation.setExpiresAt(Instant.now().plus(request.getExpirationDays(), ChronoUnit.DAYS));
            
            // Save invitation
            invitationRepository.save(invitation);
            
            // Add action to group
            GroupAction inviteAction = GroupAction.create(
                request.getUserId(),
                "COLLABORATION_INVITE_SENT",
                "Trip collaboration invite sent to: " + (invitedEmail != null ? invitedEmail : invitedUserId)
            );
            group.getActions().add(inviteAction);
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            // Create response
            InviteUserResponse response = new InviteUserResponse();
            response.setStatus("success");
            response.setGroupId(groupId);
            response.setInvitedUserId(invitedUserId);
            response.setMessage("Trip collaboration invite sent successfully");
            
            log.info("Trip collaboration invite sent for trip '{}' to '{}'", group.getTripId(), 
                invitedEmail != null ? invitedEmail : invitedUserId);
            return response;
            
        } catch (GroupNotFoundException | UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending invitation for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to send invitation: " + e.getMessage());
        }
    }
    
    /**
     * Requests to join a public trip collaboration.
     * Creates a join request that may require approval.
     */
    public JoinGroupResponse joinGroup(String groupId, JoinGroupRequest request) {
        log.info("User '{}' requesting to join trip collaboration '{}'", request.getUserId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Validate group is public
            if (!group.isPublic()) {
                throw new InvalidGroupOperationException("Cannot join private groups");
            }
            
            // Check if user is already a member
            if (group.isMember(request.getUserId())) {
                throw new InvalidGroupOperationException("User is already a member of this group");
            }
            
            // Check if group is full
            if (group.isFull()) {
                throw new InvalidGroupOperationException("Group is full (max " + group.getMaxMembers() + " members)");
            }
            
            // Check if user already has a pending join request
            if (group.hasPendingJoinRequest(request.getUserId())) {
                throw new InvalidGroupOperationException("User already has a pending join request for this group");
            }
            
            JoinGroupResponse response = new JoinGroupResponse();
            response.setGroupId(groupId);
            
            if (group.isRequiresApproval()) {
                // Create join request for approval
                JoinRequest joinRequest = new JoinRequest();
                joinRequest.setId(UUID.randomUUID().toString());
                joinRequest.setUserId(request.getUserId());
                joinRequest.setUserEmail(request.getUserEmail());
                joinRequest.setUserName(request.getUserName());
                joinRequest.setUserProfile(request.getUserProfile());
                joinRequest.setMessage(request.getMessage());
                joinRequest.setStatus("pending");
                joinRequest.setRequestedAt(Instant.now());
                
                group.addJoinRequest(joinRequest);
                
                // Add action
                GroupAction joinAction = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_SUBMITTED",
                    "Join request submitted for approval"
                );
                group.getActions().add(joinAction);
                
                response.setStatus("pending");
                response.setMessage("Join request submitted and pending approval from group administrators");
                
            } else {
                // Add user directly to group (no approval required)
                group.addUser(request.getUserId());
                
                // Add action
                GroupAction joinAction = GroupAction.create(
                    request.getUserId(),
                    "USER_JOINED",
                    "User joined the group"
                );
                group.getActions().add(joinAction);
                
                response.setStatus("success");
                response.setMessage("Successfully joined the group");
            }
            
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            log.info("User '{}' join request for group '{}' - Status: {}", 
                request.getUserId(), groupId, response.getStatus());
            return response;
            
        } catch (GroupNotFoundException | InvalidGroupOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error joining group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to join group: " + e.getMessage());
        }
    }
    
    /**
     * Gets group details.
     */
    public GroupDetailsResponse getGroupDetails(String groupId, String userId) {
        log.info("Getting group details for '{}' by user '{}'", groupId, userId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions
            if (!group.isMember(userId) && !group.isPublic()) {
                throw new UnauthorizedGroupAccessException("User is not authorized to view this group");
            }
            
            // Create response
            GroupDetailsResponse response = new GroupDetailsResponse();
            response.setGroupId(group.getId());
            response.setGroupName(group.getGroupName());
            response.setTripId(group.getTripId());
            response.setVisibility(group.getVisibility());
            response.setUserIds(group.getUserIds());
            response.setPreferences(group.getPreferences());
            response.setCreatedAt(group.getCreatedAt());
            response.setLastUpdated(group.getLastUpdated());
            
            log.info("Group details retrieved for '{}' successfully", groupId);
            return response;
            
        } catch (GroupNotFoundException | UnauthorizedGroupAccessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting group details for {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get group details: " + e.getMessage());
        }
    }
    
    /**
     * Gets list of public groups.
     */
    public List<PublicGroupResponse> getPublicGroups(String userId) {
        log.info("Getting public groups for user '{}'", userId);
        
        try {
            // Find public groups
            List<Group> publicGroups = groupRepository.findByVisibility("public");
            
            // Convert to response DTOs
            List<PublicGroupResponse> responses = publicGroups.stream()
                .map(this::convertToPublicGroupResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} public groups for user '{}'", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Unexpected error getting public groups for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get public groups: " + e.getMessage());
        }
    }
    
    /**
     * Validates create group request.
     */
    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getTripId() == null || request.getTripId().trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID is required");
        }
        
        if (request.getVisibility() == null) {
            request.setVisibility("private");
        }
        
        if (!Arrays.asList("private", "public").contains(request.getVisibility())) {
            throw new IllegalArgumentException("Visibility must be 'private' or 'public'");
        }
    }
    
    /**
     * Converts Group entity to PublicGroupResponse DTO.
     * Shows trip information instead of group names.
     */
    private PublicGroupResponse convertToPublicGroupResponse(Group group) {
        PublicGroupResponse response = new PublicGroupResponse();
        response.setGroupId(group.getId());
        response.setTripId(group.getTripId());
        response.setTripName("Trip " + group.getTripId()); // TODO: Fetch actual trip name from trip service
        response.setPreferences(group.getPreferences());
        response.setCollaboratorCount(group.getUserIds().size());
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }
    
    /**
     * Responds to an invitation (accept or reject).
     */
    public InvitationListResponse respondToInvitation(InvitationResponseRequest request) {
        log.info("User '{}' responding to invitation '{}'", request.getUserId(), request.getInvitationId());
        
        try {
            // Find invitation
            Invitation invitation = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new JoinRequestNotFoundException("Invitation not found: " + request.getInvitationId()));
            
            // Validate user can respond
            if (!invitation.getInvitedUserId().equals(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("User is not the intended recipient of this invitation");
            }
            
            if (!invitation.isPending()) {
                throw new InvalidGroupOperationException("Invitation is no longer pending");
            }
            
            // Find group
            Group group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + invitation.getGroupId()));
            
            InvitationListResponse response = new InvitationListResponse();
            
            if ("accept".equals(request.getAction())) {
                if (group.isFull()) {
                    throw new InvalidGroupOperationException("Group is full");
                }
                
                invitation.accept();
                group.addUser(request.getUserId());
                
                // Add action
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "INVITATION_ACCEPTED",
                    "User accepted invitation and joined the group"
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Invitation accepted successfully. You are now a member of the group.");
                
            } else if ("reject".equals(request.getAction())) {
                invitation.reject();
                
                // Add action
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "INVITATION_REJECTED",
                    "User rejected invitation"
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Invitation rejected successfully.");
            } else {
                throw new InvalidGroupOperationException("Invalid action. Must be 'accept' or 'reject'");
            }
            
            // Save changes
            invitationRepository.save(invitation);
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error responding to invitation: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to respond to invitation: " + e.getMessage());
        }
    }
    
    /**
     * Approves or rejects a join request.
     */
    public JoinGroupResponse approveJoinRequest(String groupId, ApproveJoinRequestRequest request) {
        log.info("User '{}' reviewing join request '{}' for group '{}'", 
            request.getUserId(), request.getJoinRequestId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions (only group creator or admin can approve)
            if (!group.isCreator(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("Only group creator can approve join requests");
            }
            
            // Find join request
            JoinRequest joinRequest = group.getJoinRequests().stream()
                .filter(jr -> jr.getId().equals(request.getJoinRequestId()))
                .findFirst()
                .orElseThrow(() -> new JoinRequestNotFoundException("Join request not found"));
            
            if (!joinRequest.isPending()) {
                throw new InvalidGroupOperationException("Join request is no longer pending");
            }
            
            JoinGroupResponse response = new JoinGroupResponse();
            response.setGroupId(groupId);
            
            if ("approve".equals(request.getAction())) {
                if (group.isFull()) {
                    throw new InvalidGroupOperationException("Group is full");
                }
                
                joinRequest.approve(request.getUserId());
                group.addUser(joinRequest.getUserId());
                
                // Add action
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_APPROVED",
                    "Join request approved for user: " + joinRequest.getUserId()
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Join request approved successfully");
                
            } else if ("reject".equals(request.getAction())) {
                joinRequest.reject(request.getUserId(), request.getReason());
                
                // Add action
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_REJECTED",
                    "Join request rejected for user: " + joinRequest.getUserId()
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Join request rejected successfully");
            } else {
                throw new InvalidGroupOperationException("Invalid action. Must be 'approve' or 'reject'");
            }
            
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error approving join request: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to process join request: " + e.getMessage());
        }
    }
    
    /**
     * Gets pending invitations for a user.
     */
    public InvitationListResponse getUserInvitations(String userId) {
        log.info("Getting invitations for user '{}'", userId);
        
        try {
            List<Invitation> invitations = invitationRepository.findPendingInvitationsByUserId(userId);
            
            List<InvitationListResponse.InvitationSummary> summaries = invitations.stream()
                .map(invitation -> {
                    InvitationListResponse.InvitationSummary summary = new InvitationListResponse.InvitationSummary();
                    summary.setInvitationId(invitation.getId());
                    summary.setGroupId(invitation.getGroupId());
                    summary.setTripId(invitation.getTripId());
                    summary.setTripName(invitation.getTripName());
                    summary.setInviterName(invitation.getInviterName());
                    summary.setInviterEmail(invitation.getInviterEmail());
                    summary.setMessage(invitation.getMessage());
                    summary.setInvitedAt(invitation.getInvitedAt());
                    summary.setExpiresAt(invitation.getExpiresAt());
                    summary.setStatus(invitation.getStatus());
                    return summary;
                })
                .collect(Collectors.toList());
            
            InvitationListResponse response = new InvitationListResponse();
            response.setStatus("success");
            response.setInvitations(summaries);
            response.setMessage("Found " + summaries.size() + " pending invitations");
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting user invitations: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to get invitations: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new public pooling group with trip planning.
     * This method creates both the group and the trip simultaneously.
     *
     * @param request The group with trip creation request
     * @return CreateGroupWithTripResponse with group and trip details
     * @throws GroupCreationException if creation fails
     */
    public CreateGroupWithTripResponse createGroupWithTrip(CreateGroupWithTripRequest request) {
        log.info("Creating group with trip for user {}", request.getUserId());
        
        try {
            // Validate input
            validateCreateGroupWithTripRequest(request);
            
            // Create trip first
            Map<String, Object> tripData = buildTripData(request);
            Map<String, Object> tripResponse = itineraryServiceClient.createTripPlan(request.getUserId(), tripData).block();
            
            if (tripResponse == null || !"success".equals(tripResponse.get("status"))) {
                throw new GroupCreationException("Failed to create trip plan");
            }
            
            String tripId = (String) tripResponse.get("tripId");
            
            // Generate group ID
            String groupId = UUID.randomUUID().toString();
            
            // Create group entity
            Group group = new Group();
            group.setId(groupId);
            group.setGroupName(request.getGroupName());
            group.setTripId(tripId);
            group.setVisibility(request.getVisibility());
            group.setStatus("draft"); // Start as draft for public pooling
            group.setMaxMembers(request.getMaxMembers());
            group.setRequiresApproval(request.getRequiresApproval());
            group.setUserIds(List.of(request.getUserId()));
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Store trip preferences for compatibility matching
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("tripName", request.getTripName());
            preferences.put("startDate", request.getStartDate());
            preferences.put("endDate", request.getEndDate());
            preferences.put("baseCity", request.getBaseCity());
            preferences.put("arrivalTime", request.getArrivalTime());
            preferences.put("multiCityAllowed", request.getMultiCityAllowed());
            preferences.put("activityPacing", request.getActivityPacing());
            preferences.put("budgetLevel", request.getBudgetLevel());
            preferences.put("preferredTerrains", request.getPreferredTerrains());
            preferences.put("preferredActivities", request.getPreferredActivities());
            preferences.putAll(request.getAdditionalPreferences() != null ? request.getAdditionalPreferences() : new HashMap<>());
            group.setPreferences(preferences);
            
            // Add creation action
            GroupAction createAction = GroupAction.create(
                request.getUserId(),
                "GROUP_WITH_TRIP_CREATED",
                "Created public pooling group with trip: " + request.getTripName()
            );
            group.setActions(List.of(createAction));
            
            // Save group
            Group savedGroup = groupRepository.save(group);
            
            // Create response
            CreateGroupWithTripResponse response = new CreateGroupWithTripResponse();
            response.setStatus("success");
            response.setGroupId(savedGroup.getId());
            response.setTripId(tripId);
            response.setMessage("Group with trip created successfully");
            response.setDraft(true);
            
            log.info("Group with trip created successfully: groupId={}, tripId={}", groupId, tripId);
            return response;
            
        } catch (Exception e) {
            log.error("Error creating group with trip: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to create group with trip: " + e.getMessage());
        }
    }
    
    /**
     * Gets trip suggestions for a group based on compatibility.
     * This method analyzes existing public groups and finds compatible ones.
     *
     * @param groupId The ID of the group
     * @param userId The requesting user's ID
     * @return TripSuggestionsResponse with compatible groups
     * @throws GroupCreationException if operation fails
     */
    public TripSuggestionsResponse getTripSuggestions(String groupId, String userId) {
        log.info("Getting trip suggestions for group {} by user {}", groupId, userId);
        
        try {
            // Get group
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Verify user is creator
            if (!group.isCreator(userId)) {
                throw new UnauthorizedGroupAccessException("Only group creator can get trip suggestions");
            }
            
            // Get trip data from itinerary service
            Map<String, Object> tripData = itineraryServiceClient.getTripPlan(group.getTripId(), userId).block();
            
            if (tripData == null) {
                throw new GroupCreationException("Could not retrieve trip data");
            }
            
            // Find compatible groups
            List<TripSuggestionsResponse.CompatibleGroup> compatibleGroups = 
                    tripCompatibilityService.findCompatibleGroups(group, tripData);
            
            // Create response
            TripSuggestionsResponse response = new TripSuggestionsResponse();
            response.setStatus("success");
            response.setGroupId(groupId);
            response.setTripId(group.getTripId());
            response.setSuggestions(compatibleGroups);
            response.setMessage(compatibleGroups.isEmpty() ? 
                    "No compatible groups found. You can proceed with your trip." : 
                    "Found " + compatibleGroups.size() + " compatible groups.");
            
            log.info("Found {} compatible groups for group {}", compatibleGroups.size(), groupId);
            return response;
            
        } catch (Exception e) {
            log.error("Error getting trip suggestions: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to get trip suggestions: " + e.getMessage());
        }
    }
    
    /**
     * Finalizes a trip or joins an existing group.
     * Handles the user's decision after seeing trip suggestions.
     *
     * @param groupId The ID of the group
     * @param request The finalize trip request
     * @return FinalizeTripResponse with result
     * @throws GroupCreationException if operation fails
     */
    public FinalizeTripResponse finalizeTrip(String groupId, FinalizeTripRequest request) {
        log.info("Finalizing trip for group {} by user {} with action {}", groupId, request.getUserId(), request.getAction());
        
        try {
            // Get group
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Verify user is creator
            if (!group.isCreator(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("Only group creator can finalize trip");
            }
            
            FinalizeTripResponse response = new FinalizeTripResponse();
            response.setGroupId(groupId);
            response.setTripId(group.getTripId());
            
            if ("finalize".equals(request.getAction())) {
                // Finalize current group
                group.finalize();
                
                // Add finalization action
                GroupAction finalizeAction = GroupAction.create(
                    request.getUserId(),
                    "TRIP_FINALIZED",
                    "Finalized trip and made group active"
                );
                group.getActions().add(finalizeAction);
                
                groupRepository.save(group);
                
                response.setStatus("success");
                response.setMessage("Trip finalized successfully");
                response.setAction("finalized");
                response.setSuccess(true);
                
                log.info("Trip finalized for group {}", groupId);
                
            } else if ("join".equals(request.getAction())) {
                // Join existing group
                if (request.getTargetGroupId() == null) {
                    throw new InvalidGroupOperationException("Target group ID is required for join action");
                }
                
                // Get target group
                Group targetGroup = groupRepository.findById(request.getTargetGroupId())
                        .orElseThrow(() -> new GroupNotFoundException("Target group not found: " + request.getTargetGroupId()));
                
                // Add user to target group
                targetGroup.addUser(request.getUserId());
                
                // Add join action to target group
                GroupAction joinAction = GroupAction.create(
                    request.getUserId(),
                    "USER_JOINED_FROM_SUGGESTION",
                    "Joined group from trip compatibility suggestion"
                );
                targetGroup.getActions().add(joinAction);
                
                groupRepository.save(targetGroup);
                
                // Delete current group and its trip
                // Note: In a real implementation, you might want to soft-delete
                groupRepository.delete(group);
                
                response.setStatus("success");
                response.setMessage("Successfully joined existing group");
                response.setAction("joined");
                response.setGroupId(targetGroup.getId());
                response.setTripId(targetGroup.getTripId());
                response.setSuccess(true);
                
                log.info("User {} joined existing group {} and discarded group {}", request.getUserId(), request.getTargetGroupId(), groupId);
                
            } else {
                throw new InvalidGroupOperationException("Invalid action: " + request.getAction());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error finalizing trip: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to finalize trip: " + e.getMessage());
        }
    }
    
    /**
     * Validates create group with trip request.
     */
    private void validateCreateGroupWithTripRequest(CreateGroupWithTripRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        
        if (request.getTripName() == null || request.getTripName().trim().isEmpty()) {
            throw new IllegalArgumentException("Trip name is required");
        }
        
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (request.getBaseCity() == null || request.getBaseCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Base city is required");
        }
        
        if (request.getMaxMembers() < 2 || request.getMaxMembers() > 20) {
            throw new IllegalArgumentException("Maximum members must be between 2 and 20");
        }
    }
    
    /**
     * Builds trip data for itinerary service.
     */
    private Map<String, Object> buildTripData(CreateGroupWithTripRequest request) {
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("userId", request.getUserId());
        tripData.put("tripName", request.getTripName());
        tripData.put("startDate", request.getStartDate());
        tripData.put("endDate", request.getEndDate());
        tripData.put("baseCity", request.getBaseCity());
        tripData.put("arrivalTime", request.getArrivalTime());
        tripData.put("multiCityAllowed", request.getMultiCityAllowed());
        tripData.put("activityPacing", request.getActivityPacing());
        tripData.put("budgetLevel", request.getBudgetLevel());
        tripData.put("preferredTerrains", request.getPreferredTerrains());
        tripData.put("preferredActivities", request.getPreferredActivities());
        tripData.put("type", "group"); // Mark as group trip
        return tripData;
    }
}