package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.GroupAction;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.model.Invitation;
import com.islandhop.pooling.repository.GroupRepository;
import com.islandhop.pooling.repository.InvitationRepository;
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
            
            log.info("Group '{}' created successfully with ID: {}", request.getGroupName(), groupId);
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
                    summary.setGroupName(invitation.getGroupName());
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
}