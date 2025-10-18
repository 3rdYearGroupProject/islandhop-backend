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
import com.islandhop.pooling.client.UserServiceClient;
import com.islandhop.pooling.client.TripServiceClient;
import com.islandhop.pooling.service.TripCompatibilityService;
import com.islandhop.pooling.util.DateUtils;
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
    private final UserServiceClient userServiceClient;
    private final TripServiceClient tripServiceClient;
    
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
            group.setCreatorUserId(request.getUserId());
            group.setCreatedBy(request.getUserId()); // Set both fields for consistency
            group.setCreatorEmail(request.getUserEmail()); // Store creator email for name lookup
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
            
            // Fetch invited person's complete profile details by email (from user service)
            UserServiceClient.UserProfile invitedProfile = null;
            String resolvedInvitedUserId = invitedUserId; // Will be populated if not provided
            
            if (invitedEmail != null) {
                try {
                    // Get user profile by email
                    invitedProfile = userServiceClient.getUserByEmail(invitedEmail);
                    if (invitedProfile == null) {
                        log.warn("Could not fetch invited user profile for email: {}", invitedEmail);
                        throw new InvalidGroupOperationException("User with email " + invitedEmail + " not found in system");
                    }
                    
                    // Get the Firebase UID for this email if not already provided
                    if (resolvedInvitedUserId == null) {
                        resolvedInvitedUserId = userServiceClient.getUidByEmail(invitedEmail);
                        if (resolvedInvitedUserId == null) {
                            log.warn("Could not fetch UID for email: {}", invitedEmail);
                        } else {
                            log.info("Resolved UID '{}' for email '{}'", resolvedInvitedUserId, invitedEmail);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching invited user profile for email {}: {}", invitedEmail, e.getMessage());
                    throw new InvalidGroupOperationException("Failed to fetch user profile for " + invitedEmail);
                }
            } else if (resolvedInvitedUserId != null) {
                // If only user ID provided, try to fetch by UID
                try {
                    invitedProfile = userServiceClient.getUserByUid(resolvedInvitedUserId);
                    if (invitedProfile == null) {
                        log.warn("Could not fetch invited user profile for user ID: {}", resolvedInvitedUserId);
                        throw new InvalidGroupOperationException("User with ID " + resolvedInvitedUserId + " not found in system");
                    }
                } catch (Exception e) {
                    log.error("Error fetching invited user profile for user ID {}: {}", resolvedInvitedUserId, e.getMessage());
                    throw new InvalidGroupOperationException("Failed to fetch user profile for user ID " + resolvedInvitedUserId);
                }
            } else {
                throw new InvalidGroupOperationException("Either invited user ID or email must be provided");
            }
            
            // Check if user is already a member (now that we have the UID)
            if (resolvedInvitedUserId != null && group.isMember(resolvedInvitedUserId)) {
                throw new InvalidGroupOperationException("User is already a member of this group");
            }
            
            // Check for existing pending invitation using resolved UID
            if (resolvedInvitedUserId != null) {
                Optional<Invitation> existingInvitation = invitationRepository
                    .findByGroupIdAndInvitedUserId(groupId, resolvedInvitedUserId);
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
            
            // Set inviter details from request (provided by frontend)
            invitation.setInviterEmail(request.getInviterEmail());
            invitation.setInviterDisplayName(request.getInviterDisplayName());
            
            // Set invited person's complete profile details from user service
            invitation.setInvitedUserId(resolvedInvitedUserId); // Firebase UID (resolved from email if needed)
            invitation.setInvitedEmail(invitedProfile.getEmail());
            invitation.setInvitedDisplayName(invitedProfile.getFullName());
            invitation.setInvitedFirstName(invitedProfile.getFirstName());
            invitation.setInvitedLastName(invitedProfile.getLastName());
            invitation.setInvitedNationality(invitedProfile.getNationality());
            invitation.setInvitedDob(invitedProfile.getDob());
            invitation.setInvitedProfileCompletion(invitedProfile.getProfileCompletion());
            invitation.setInvitedLanguages(invitedProfile.getLanguages());
            
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
     * Always creates a pending join request that requires approval from all group members.
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
            
            // Always create a pending join request that requires approval from all members
            JoinRequest joinRequest = new JoinRequest();
            joinRequest.setId(UUID.randomUUID().toString());
            joinRequest.setUserId(request.getUserId());
            joinRequest.setUserEmail(request.getUserEmail());
            joinRequest.setUserName(request.getUserName());
            joinRequest.setMessage(request.getMessage());
            joinRequest.setStatus("pending");
            joinRequest.setRequestedAt(Instant.now());
            joinRequest.setRequiresAllMemberApproval(true); // Require approval from all members
            
            // Fetch and store complete user profile details
            try {
                log.info("Fetching user profile for join request by email '{}'", request.getUserEmail());
                UserServiceClient.UserProfile userProfile = userServiceClient.getUserByEmail(request.getUserEmail());
                
                if (userProfile != null) {
                    log.info("Successfully fetched profile for joining user: {} {} ({})", 
                        userProfile.getFirstName(), userProfile.getLastName(), userProfile.getEmail());
                    
                    // Store complete profile data in the join request
                    Map<String, Object> profileData = new HashMap<>();
                    profileData.put("firstName", userProfile.getFirstName());
                    profileData.put("lastName", userProfile.getLastName());
                    profileData.put("email", userProfile.getEmail());
                    profileData.put("nationality", userProfile.getNationality());
                    profileData.put("languages", userProfile.getLanguages());
                    profileData.put("dob", userProfile.getDob());
                    profileData.put("profileCompletion", userProfile.getProfileCompletion());
                    profileData.put("fetchedAt", Instant.now().toString());
                    
                    joinRequest.setUserProfile(profileData);
                    
                    // Use the full name from profile if userName is not provided
                    if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
                        String fullName = (userProfile.getFirstName() != null ? userProfile.getFirstName() : "") + 
                                         " " + (userProfile.getLastName() != null ? userProfile.getLastName() : "");
                        joinRequest.setUserName(fullName.trim().isEmpty() ? userProfile.getEmail() : fullName.trim());
                    }
                    
                } else {
                    log.warn("Could not fetch profile for joining user '{}' (email: '{}'), using provided data", 
                        request.getUserId(), request.getUserEmail());
                    // Use provided userProfile data as fallback
                    joinRequest.setUserProfile(request.getUserProfile());
                }
            } catch (Exception e) {
                log.error("Error fetching profile for joining user '{}': {}", request.getUserId(), e.getMessage());
                // Use provided userProfile data as fallback
                joinRequest.setUserProfile(request.getUserProfile());
            }
            
            group.addJoinRequest(joinRequest);
            
            // Add action
            GroupAction joinAction = GroupAction.create(
                request.getUserId(),
                "JOIN_REQUEST_SUBMITTED",
                "Join request submitted and pending approval from all group members"
            );
            group.getActions().add(joinAction);
            
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            JoinGroupResponse response = new JoinGroupResponse();
            response.setGroupId(groupId);
            response.setStatus("pending");
            response.setMessage("Join request submitted and pending approval from all group members (" + 
                group.getUserIds().size() + " member(s) need to vote)");
            
            log.info("User '{}' join request for group '{}' - Status: {} - Requires approval from {} members", 
                request.getUserId(), groupId, response.getStatus(), group.getUserIds().size());
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
     * Gets list of public groups with optional filtering.
     * Enhanced to support filtering by preferences and compatibility scoring.
     */
    public List<PublicGroupResponse> getPublicGroups(String userId, String baseCity, String startDate, 
                                                     String endDate, String budgetLevel, List<String> preferredActivities) {
        log.info("Getting public groups for user '{}' with filters: baseCity={}, startDate={}, endDate={}, budgetLevel={}, activities={}", 
                userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
        
        try {
            List<Group> publicGroups;
            
            // Apply repository-level filtering if multiple criteria provided
            if (baseCity != null && budgetLevel != null && startDate != null && endDate != null) {
                publicGroups = groupRepository.findPublicGroupsWithFilters(baseCity, budgetLevel, startDate, endDate);
                log.debug("Applied repository filtering, found {} groups", publicGroups.size());
            } else {
                // Get all public groups and filter programmatically
                publicGroups = groupRepository.findByVisibility("public");
                publicGroups = applyFilters(publicGroups, baseCity, startDate, endDate, budgetLevel, preferredActivities);
                log.debug("Applied programmatic filtering, found {} groups", publicGroups.size());
            }
            
            // Convert to response DTOs
            List<PublicGroupResponse> responses = publicGroups.stream()
                .map(this::convertToPublicGroupResponse)
                .collect(Collectors.toList());
            
            // If user preferences are provided, calculate compatibility scores and sort
            if (hasUserPreferences(baseCity, startDate, endDate, budgetLevel, preferredActivities)) {
                responses = addCompatibilityScores(responses, userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
                
                // Sort by compatibility score descending
                responses.sort((a, b) -> {
                    Double scoreA = a.getCompatibilityScore() != null ? a.getCompatibilityScore() : 0.0;
                    Double scoreB = b.getCompatibilityScore() != null ? b.getCompatibilityScore() : 0.0;
                    return Double.compare(scoreB, scoreA);
                });
            }
            
            log.info("Found {} public groups for user '{}' after filtering", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Unexpected error getting public groups for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get public groups: " + e.getMessage());
        }
    }

    /**
     * Gets all groups (public and private) created by a specific user and groups where the user is a participant.
     * Includes creator information, member details, and enhanced trip information.
     */
    public List<EnhancedPublicGroupResponse> getGroupsCreatedByUser(String userId) {
        log.info("Getting all groups created by user '{}' and groups where user is a participant", userId);
        
        try {
            // Find groups by creator user ID
            List<Group> createdGroups = groupRepository.findByCreatorUserId(userId);
            
            // Also check createdBy field for backward compatibility
            List<Group> createdByGroups = groupRepository.findByCreatedBy(userId);
            
            // Find groups where user is a participant (includes created groups + joined groups)
            List<Group> participantGroups = groupRepository.findByUserIdsContaining(userId);
            
            // Combine and deduplicate
            Set<String> groupIds = new HashSet<>();
            List<Group> allGroups = new ArrayList<>();
            
            // Add groups created by user (creatorUserId field)
            for (Group group : createdGroups) {
                if (!groupIds.contains(group.getId())) {
                    groupIds.add(group.getId());
                    allGroups.add(group);
                }
            }
            
            // Add groups created by user (createdBy field - backward compatibility)
            for (Group group : createdByGroups) {
                if (!groupIds.contains(group.getId())) {
                    groupIds.add(group.getId());
                    allGroups.add(group);
                }
            }
            
            // Add groups where user is a participant (avoiding duplicates)
            for (Group group : participantGroups) {
                if (!groupIds.contains(group.getId())) {
                    groupIds.add(group.getId());
                    allGroups.add(group);
                }
            }
            
            log.info("Found {} groups for user '{}' (created: {}, createdBy: {}, participant: {}, total unique: {})", 
                userId, allGroups.size(), createdGroups.size(), createdByGroups.size(), participantGroups.size(), allGroups.size());
            
            // Convert to enhanced response DTOs with full details
            List<EnhancedPublicGroupResponse> responses = allGroups.stream()
                .map(this::convertToEnhancedPublicGroupResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());
            
            // Sort by creation date (newest first)
            responses.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            log.info("Successfully converted {} groups created by user '{}'", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Unexpected error getting groups created by user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get groups created by user: " + e.getMessage());
        }
    }

    /**
     * Gets all pending join requests for groups where the user is a member.
     * Provides a consolidated view across all groups for efficient request management.
     */
    public AllPendingRequestsResponse getAllPendingRequestsForUser(String userId) {
        log.info("Getting all pending join requests for user '{}'", userId);
        
        try {
            // Find all groups where the user is a member
            List<Group> userGroups = groupRepository.findByUserIdsContaining(userId);
            log.info("Found {} groups where user '{}' is a member", userGroups.size(), userId);
            
            AllPendingRequestsResponse response = new AllPendingRequestsResponse();
            response.setStatus("success");
            
            List<AllPendingRequestsResponse.GroupWithPendingRequests> groupsWithRequests = new ArrayList<>();
            int totalPendingRequests = 0;
            
            for (Group group : userGroups) {
                // Get pending join requests for this group
                List<JoinRequest> pendingRequests = group.getJoinRequests().stream()
                    .filter(JoinRequest::isPending)
                    .collect(Collectors.toList());
                
                if (!pendingRequests.isEmpty()) {
                    AllPendingRequestsResponse.GroupWithPendingRequests groupWithRequests = 
                        new AllPendingRequestsResponse.GroupWithPendingRequests();
                    
                    // Basic group information
                    groupWithRequests.setGroupId(group.getId());
                    groupWithRequests.setGroupName(group.getGroupName());
                    groupWithRequests.setVisibility(group.getVisibility());
                    groupWithRequests.setCurrentMembers(group.getUserIds().size());
                    groupWithRequests.setMaxMembers(group.getMaxMembers());
                    groupWithRequests.setPendingRequestsCount(pendingRequests.size());
                    
                    // Get trip name if available
                    try {
                        if (group.getTripId() != null) {
                            TripServiceClient.TripDetails tripDetails = tripServiceClient.getTripDetails(group.getTripId(), userId);
                            groupWithRequests.setTripName(tripDetails != null ? tripDetails.getTripName() : group.getGroupName());
                        } else {
                            groupWithRequests.setTripName(group.getGroupName());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to get trip details for group {}: {}", group.getId(), e.getMessage());
                        groupWithRequests.setTripName(group.getGroupName());
                    }
                    
                    // Convert join requests to response format
                    List<AllPendingRequestsResponse.PendingJoinRequestInfo> requestInfos = pendingRequests.stream()
                        .map(joinRequest -> convertToPendingJoinRequestInfo(joinRequest, group, userId))
                        .collect(Collectors.toList());
                    
                    groupWithRequests.setPendingRequests(requestInfos);
                    groupsWithRequests.add(groupWithRequests);
                    totalPendingRequests += pendingRequests.size();
                }
            }
            
            response.setGroups(groupsWithRequests);
            response.setTotalGroups(groupsWithRequests.size());
            response.setTotalPendingRequests(totalPendingRequests);
            
            if (totalPendingRequests == 0) {
                response.setMessage("No pending join requests at this time");
            } else {
                response.setMessage(String.format("Found %d pending join requests across %d groups", 
                    totalPendingRequests, groupsWithRequests.size()));
            }
            
            log.info("Successfully retrieved {} pending requests across {} groups for user '{}'", 
                totalPendingRequests, groupsWithRequests.size(), userId);
            
            return response;
            
        } catch (Exception e) {
            log.error("Unexpected error getting pending requests for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get pending requests: " + e.getMessage());
        }
    }

    /**
     * Gets ALL pending items requiring user action - both invitations received and join requests to vote on.
     * This comprehensive method combines invitations and voting requests into a single response.
     * 
     * @param userId The Firebase UID of the user
     * @param userEmail The email of the user (used to find invitations)
     * @return ComprehensivePendingItemsResponse with all pending items
     */
    public ComprehensivePendingItemsResponse getAllPendingItems(String userId, String userEmail) {
        log.info("Getting ALL pending items (invitations + voting requests) for user '{}' with email '{}'", userId, userEmail);
        
        try {
            ComprehensivePendingItemsResponse response = new ComprehensivePendingItemsResponse();
            response.setStatus("success");
            
            // 1. Get pending invitations the user has received BY EMAIL (from invitation collection)
            List<Invitation> userInvitations = invitationRepository.findPendingInvitationsByEmail(userEmail);
            log.info("Found {} pending invitations for email '{}'", userInvitations.size(), userEmail);
            
            // Convert invitations to response DTOs with group details
            List<ComprehensivePendingItemsResponse.PendingInvitation> pendingInvitations = userInvitations.stream()
                    .map(invitation -> convertToPendingInvitationWithGroupDetails(invitation))
                    .collect(Collectors.toList());
            
            // 2. Get join requests that need the user's vote (for groups they're a member of)
            List<Group> userGroups = groupRepository.findByUserIdsContaining(userId);
            List<ComprehensivePendingItemsResponse.PendingVoteRequest> pendingVotes = new ArrayList<>();
            
            for (Group group : userGroups) {
                List<JoinRequest> pendingRequests = group.getJoinRequests().stream()
                    .filter(JoinRequest::isPending)
                    .collect(Collectors.toList());
                
                for (JoinRequest joinRequest : pendingRequests) {
                    ComprehensivePendingItemsResponse.PendingVoteRequest voteRequest = 
                        convertToPendingVoteRequest(joinRequest, group, userId);
                    pendingVotes.add(voteRequest);
                }
            }
            
            // Set response data
            response.setPendingInvitations(pendingInvitations);
            response.setTotalInvitations(pendingInvitations.size());
            response.setPendingVotes(pendingVotes);
            response.setTotalVoteRequests(pendingVotes.size());
            response.setTotalPendingItems(pendingInvitations.size() + pendingVotes.size());
            
            // Set appropriate message
            if (response.getTotalPendingItems() == 0) {
                response.setMessage("No pending items at this time");
            } else {
                response.setMessage(String.format("You have %d invitations and %d join requests requiring your attention", 
                    pendingInvitations.size(), pendingVotes.size()));
            }
            
            log.info("Successfully retrieved {} invitations and {} voting requests for user '{}'", 
                    pendingInvitations.size(), pendingVotes.size(), userId);
            
            return response;
            
        } catch (Exception e) {
            log.error("Unexpected error getting all pending items for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get all pending items: " + e.getMessage());
        }
    }

    /**
     * Gets list of enhanced public groups with detailed trip and creator information.
     * This version provides comprehensive details for UI display like trip names, creator names,
     * cities, dates, and top attractions. Supports both authenticated and anonymous access.
     */
    public List<EnhancedPublicGroupResponse> getEnhancedPublicGroups(String userId, String baseCity, String startDate, 
                                                                     String endDate, String budgetLevel, List<String> preferredActivities) {
        log.info("Getting enhanced public groups for user '{}' (anonymous: {}) with filters: baseCity={}, startDate={}, endDate={}, budgetLevel={}, activities={}", 
                userId, userId == null ? "yes" : "no", baseCity, startDate, endDate, budgetLevel, preferredActivities);
        
        try {
            List<Group> publicGroups;
            
            // First check total groups in database
            List<Group> allGroups = groupRepository.findAll();
            log.info("DEBUG: Total groups in database: {}", allGroups.size());
            
            // Check public groups specifically
            List<Group> allPublicGroups = groupRepository.findByVisibility("public");
            log.info("DEBUG: Total public groups in database: {}", allPublicGroups.size());
            
            if (allPublicGroups.size() > 0) {
                log.info("DEBUG: First public group details - ID: {}, Name: {}, Visibility: {}", 
                    allPublicGroups.get(0).getId(), 
                    allPublicGroups.get(0).getGroupName(), 
                    allPublicGroups.get(0).getVisibility());
            }
            
            // Apply repository-level filtering if multiple criteria provided
            if (baseCity != null && budgetLevel != null && startDate != null && endDate != null) {
                publicGroups = groupRepository.findPublicGroupsWithFilters(baseCity, budgetLevel, startDate, endDate);
                log.debug("Applied repository filtering, found {} groups", publicGroups.size());
            } else {
                // Get all public groups and filter programmatically
                publicGroups = groupRepository.findByVisibility("public");
                log.info("DEBUG: Before programmatic filtering: {} groups", publicGroups.size());
                publicGroups = applyFilters(publicGroups, baseCity, startDate, endDate, budgetLevel, preferredActivities);
                log.info("DEBUG: After programmatic filtering: {} groups", publicGroups.size());
            }
            
            // Convert to enhanced response DTOs with member details
            List<EnhancedPublicGroupResponse> responses = publicGroups.stream()
                .map(this::convertToEnhancedPublicGroupResponse)
                .filter(response -> response != null) // Filter out groups with missing data
                .collect(Collectors.toList());
            
            log.info("DEBUG: After DTO conversion and null filtering: {} responses", responses.size());
            
            // If user preferences are provided, calculate compatibility scores and sort
            if (hasUserPreferences(baseCity, startDate, endDate, budgetLevel, preferredActivities)) {
                responses = addEnhancedCompatibilityScores(responses, userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
                
                // Sort by compatibility score descending
                responses.sort((a, b) -> {
                    Double scoreA = a.getCompatibilityScore() != null ? a.getCompatibilityScore() : 0.0;
                    Double scoreB = b.getCompatibilityScore() != null ? b.getCompatibilityScore() : 0.0;
                    return Double.compare(scoreB, scoreA);
                });
            }
            
            log.info("Found {} enhanced public groups for user '{}' after filtering", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Unexpected error getting enhanced public groups for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get enhanced public groups: " + e.getMessage());
        }
    }
    
    /**
     * Applies programmatic filters to groups when repository filtering is not used.
     */
    private List<Group> applyFilters(List<Group> groups, String baseCity, String startDate, 
                                     String endDate, String budgetLevel, List<String> preferredActivities) {
        return groups.stream()
            .filter(group -> matchesFilters(group, baseCity, startDate, endDate, budgetLevel, preferredActivities))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a group matches the provided filters.
     */
    private boolean matchesFilters(Group group, String baseCity, String startDate, 
                                   String endDate, String budgetLevel, List<String> preferredActivities) {
        Map<String, Object> preferences = group.getPreferences();
        if (preferences == null) {
            return false;
        }
        
        // Base city filter
        if (baseCity != null) {
            String groupBaseCity = (String) preferences.get("baseCity");
            if (groupBaseCity == null || !groupBaseCity.equalsIgnoreCase(baseCity)) {
                return false;
            }
        }
        
        // Date filters
        if (startDate != null) {
            String groupStartDate = (String) preferences.get("startDate");
            if (groupStartDate == null || !groupStartDate.equals(startDate)) {
                return false;
            }
        }
        
        if (endDate != null) {
            String groupEndDate = (String) preferences.get("endDate");
            if (groupEndDate == null || !groupEndDate.equals(endDate)) {
                return false;
            }
        }
        
        // Budget filter
        if (budgetLevel != null) {
            String groupBudgetLevel = (String) preferences.get("budgetLevel");
            if (groupBudgetLevel == null || !groupBudgetLevel.equalsIgnoreCase(budgetLevel)) {
                return false;
            }
        }
        
        // Activity filter (at least one common activity)
        if (preferredActivities != null && !preferredActivities.isEmpty()) {
            List<String> groupActivities = (List<String>) preferences.get("preferredActivities");
            if (groupActivities == null || groupActivities.isEmpty()) {
                return false;
            }
            
            boolean hasCommonActivity = groupActivities.stream()
                .anyMatch(activity -> preferredActivities.contains(activity));
            if (!hasCommonActivity) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if user preferences are provided for compatibility scoring.
     */
    private boolean hasUserPreferences(String baseCity, String startDate, String endDate, 
                                       String budgetLevel, List<String> preferredActivities) {
        return baseCity != null || startDate != null || endDate != null || 
               budgetLevel != null || (preferredActivities != null && !preferredActivities.isEmpty());
    }
    
    /**
     * Adds compatibility scores to public group responses.
     */
    private List<PublicGroupResponse> addCompatibilityScores(List<PublicGroupResponse> responses, 
                                                             String userId, String baseCity, String startDate, 
                                                             String endDate, String budgetLevel, List<String> preferredActivities) {
        // Create user preferences map
        Map<String, Object> userPreferences = new HashMap<>();
        if (baseCity != null) userPreferences.put("baseCity", baseCity);
        if (startDate != null) userPreferences.put("startDate", startDate);
        if (endDate != null) userPreferences.put("endDate", endDate);
        if (budgetLevel != null) userPreferences.put("budgetLevel", budgetLevel);
        if (preferredActivities != null) userPreferences.put("preferredActivities", preferredActivities);
        
        // Calculate compatibility scores
        responses.forEach(response -> {
            try {
                Group group = groupRepository.findById(response.getGroupId()).orElse(null);
                if (group != null) {
                    double score = tripCompatibilityService.calculatePreCheckCompatibilityScore(userPreferences, group);
                    response.setCompatibilityScore(Math.round(score * 100.0) / 100.0);
                    log.debug("Calculated compatibility score {} for group {}", score, group.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to calculate compatibility score for group {}: {}", response.getGroupId(), e.getMessage());
                response.setCompatibilityScore(0.0);
            }
        });
        
        return responses;
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
     * Enhanced with additional fields for filtering and compatibility.
     */
    private PublicGroupResponse convertToPublicGroupResponse(Group group) {
        PublicGroupResponse response = new PublicGroupResponse();
        response.setGroupId(group.getId());
        response.setTripId(group.getTripId());
        response.setGroupName(group.getGroupName());
        response.setTripName(group.getTripName() != null ? group.getTripName() : "Trip " + group.getTripId());
        response.setPreferences(group.getPreferences());
        response.setCollaboratorCount(group.getUserIds().size());
        response.setMaxMembers(group.getMaxMembers());
        response.setCreatedAt(group.getCreatedAt());
        response.setStatus(group.getStatus());
        
        // Extract enhanced fields from preferences
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            response.setBaseCity((String) preferences.get("baseCity"));
            response.setStartDate((String) preferences.get("startDate"));
            response.setEndDate((String) preferences.get("endDate"));
            response.setBudgetLevel((String) preferences.get("budgetLevel"));
            response.setActivityPacing((String) preferences.get("activityPacing"));
            
            // Handle list fields safely
            Object activitiesObj = preferences.get("preferredActivities");
            if (activitiesObj instanceof List) {
                response.setPreferredActivities((List<String>) activitiesObj);
            }
            
            Object terrainsObj = preferences.get("preferredTerrains");
            if (terrainsObj instanceof List) {
                response.setPreferredTerrains((List<String>) terrainsObj);
            }
        }
        
        return response;
    }

    /**
     * Converts Group entity to EnhancedPublicGroupResponse DTO with detailed trip and creator information.
     * Fetches additional data from trip service and user service.
     */
    private EnhancedPublicGroupResponse convertToEnhancedPublicGroupResponse(Group group) {
        try {
            log.info("DEBUG: Converting group to enhanced response - Group ID: {}, Trip ID: {}", group.getId(), group.getTripId());
            
            EnhancedPublicGroupResponse response = new EnhancedPublicGroupResponse();
            
            // Basic group information
            response.setGroupId(group.getId());
            response.setTripId(group.getTripId());
            response.setGroupName(group.getGroupName());
            response.setStatus(group.getStatus());
            response.setVisibility(group.getVisibility()); // "public" or "private"
            response.setCreatedAt(group.getCreatedAt());
            response.setMemberCount(group.getUserIds().size());
            response.setMaxMembers(group.getMaxMembers());
            response.setMemberCountText(group.getUserIds().size() + " participants / " + group.getMaxMembers());
            
            // Creator information
            String creatorUserId = group.getCreatorUserId();
            response.setCreatorUserId(creatorUserId);
            
            // Get creator name from stored member data if available
            String creatorName = "User " + creatorUserId; // fallback
            if (group.getMembers() != null) {
                Group.Member creator = group.getMemberByUserId(creatorUserId);
                if (creator != null) {
                    creatorName = creator.getFullName();
                    log.info("Using stored creator name: '{}' for user '{}'", creatorName, creatorUserId);
                } else {
                    log.warn("Creator member data not found for user '{}', using fallback", creatorUserId);
                    creatorName = getCreatorName(creatorUserId, group.getCreatorEmail());
                }
            } else {
                log.warn("No member data stored for group '{}', using fallback creator name", group.getId());
                creatorName = getCreatorName(creatorUserId, group.getCreatorEmail());
            }
            response.setCreatorName(creatorName);
            
            // Build member details with stored user data
            List<ComprehensiveTripResponse.MemberSummary> memberDetails = buildMemberSummaries(group);
            response.setMembers(memberDetails);
            
            // Get trip details if tripId is available
            if (group.getTripId() != null) {
                log.info("DEBUG: Fetching trip details for trip ID: {}", group.getTripId());
                TripServiceClient.TripDetails tripDetails = tripServiceClient.getTripDetails(group.getTripId(), creatorUserId);
                if (tripDetails != null) {
                    log.info("DEBUG: Trip details found for trip ID: {}, Trip name: {}", group.getTripId(), tripDetails.getTripName());
                    response.setTripName(tripDetails.getTripName());
                    response.setBaseCity(tripDetails.getBaseCity());
                    response.setStartDate(tripDetails.getStartDate());
                    response.setEndDate(tripDetails.getEndDate());
                    response.setBudgetLevel(tripDetails.getBudgetLevel());
                    response.setActivityPacing(tripDetails.getActivityPacing());
                    response.setPreferredActivities(tripDetails.getPreferredActivities());
                    response.setPreferredTerrains(tripDetails.getPreferredTerrains());
                    response.setCities(tripDetails.getCities());
                    response.setTopAttractions(tripDetails.getTopAttractions());
                    
                    // Calculate trip duration and format date range
                    if (tripDetails.getStartDate() != null && tripDetails.getEndDate() != null) {
                        response.setTripDurationDays(DateUtils.calculateTripDuration(tripDetails.getStartDate(), tripDetails.getEndDate()));
                        response.setFormattedDateRange(DateUtils.formatDateRange(tripDetails.getStartDate(), tripDetails.getEndDate()));
                    }
                } else {
                    log.warn("Trip details not found for trip ID: {}, using fallback data", group.getTripId());
                    // Use fallback data from group preferences instead of returning null
                    Map<String, Object> preferences = group.getPreferences();
                    if (preferences != null) {
                        response.setBaseCity((String) preferences.get("baseCity"));
                        response.setStartDate((String) preferences.get("startDate"));
                        response.setEndDate((String) preferences.get("endDate"));
                        response.setBudgetLevel((String) preferences.get("budgetLevel"));
                        response.setActivityPacing((String) preferences.get("activityPacing"));
                        
                        Object activitiesObj = preferences.get("preferredActivities");
                        if (activitiesObj instanceof List) {
                            response.setPreferredActivities((List<String>) activitiesObj);
                        }
                        
                        Object terrainsObj = preferences.get("preferredTerrains");
                        if (terrainsObj instanceof List) {
                            response.setPreferredTerrains((List<String>) terrainsObj);
                        }
                    }
                }
            } else {
                // Fallback to preferences if no trip ID
                Map<String, Object> preferences = group.getPreferences();
                if (preferences != null) {
                    response.setBaseCity((String) preferences.get("baseCity"));
                    response.setStartDate((String) preferences.get("startDate"));
                    response.setEndDate((String) preferences.get("endDate"));
                    response.setBudgetLevel((String) preferences.get("budgetLevel"));
                    response.setActivityPacing((String) preferences.get("activityPacing"));
                    
                    Object activitiesObj = preferences.get("preferredActivities");
                    if (activitiesObj instanceof List) {
                        response.setPreferredActivities((List<String>) activitiesObj);
                    }
                    
                    Object terrainsObj = preferences.get("preferredTerrains");
                    if (terrainsObj instanceof List) {
                        response.setPreferredTerrains((List<String>) terrainsObj);
                    }
                    
                    // Calculate duration and format dates from preferences
                    String startDate = (String) preferences.get("startDate");
                    String endDate = (String) preferences.get("endDate");
                    if (startDate != null && endDate != null) {
                        response.setTripDurationDays(DateUtils.calculateTripDuration(startDate, endDate));
                        response.setFormattedDateRange(DateUtils.formatDateRange(startDate, endDate));
                    }
                }
            }
            
            // Set default trip name if not available
            if (response.getTripName() == null || response.getTripName().isEmpty()) {
                response.setTripName(group.getGroupName() != null ? group.getGroupName() : "Adventure Trip");
            }
            
            log.info("DEBUG: Successfully converted group {} to enhanced response", group.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error converting group {} to enhanced response: {}", group.getId(), e.getMessage(), e);
            log.error("DEBUG: Returning null for group {}", group.getId());
            return null;
        }
    }
    
    /**
     * Gets creator name from stored member data in the group.
     * No longer makes external calls to UserServiceClient since we store member details locally.
     */
    private String getCreatorName(String creatorUserId, String creatorEmail) {
        log.info("Getting creator name for userId: '{}', email: '{}'", creatorUserId, creatorEmail);
        
        // This method is called from convertToEnhancedPublicGroupResponse which has the group object
        // For now, return a fallback. We'll update the calling method to pass the group object
        // or get the name directly from the group's members list.
        log.info("Using fallback creator name method. Consider updating to use stored member data.");
        
        // Fallback to email or UID as display name
        if (creatorEmail != null && !creatorEmail.trim().isEmpty()) {
            return creatorEmail;
        }
        
        return "User " + creatorUserId;
    }
    
    /**
     * Adds compatibility scores to enhanced public group responses.
     */
    private List<EnhancedPublicGroupResponse> addEnhancedCompatibilityScores(List<EnhancedPublicGroupResponse> responses, 
            String userId, String baseCity, String startDate, String endDate, String budgetLevel, List<String> preferredActivities) {
        
        for (EnhancedPublicGroupResponse response : responses) {
            try {
                // Create preferences map for compatibility scoring
                Map<String, Object> userPreferences = new HashMap<>();
                if (baseCity != null) userPreferences.put("baseCity", baseCity);
                if (startDate != null) userPreferences.put("startDate", startDate);
                if (endDate != null) userPreferences.put("endDate", endDate);
                if (budgetLevel != null) userPreferences.put("budgetLevel", budgetLevel);
                if (preferredActivities != null) userPreferences.put("preferredActivities", preferredActivities);
                
                // Create group preferences map
                Map<String, Object> groupPreferences = new HashMap<>();
                if (response.getBaseCity() != null) groupPreferences.put("baseCity", response.getBaseCity());
                if (response.getStartDate() != null) groupPreferences.put("startDate", response.getStartDate());
                if (response.getEndDate() != null) groupPreferences.put("endDate", response.getEndDate());
                if (response.getBudgetLevel() != null) groupPreferences.put("budgetLevel", response.getBudgetLevel());
                if (response.getPreferredActivities() != null) groupPreferences.put("preferredActivities", response.getPreferredActivities());
                
                // Calculate compatibility score
                double score = tripCompatibilityService.calculateCompatibilityScore(userPreferences, groupPreferences);
                response.setCompatibilityScore(score);
                
            } catch (Exception e) {
                log.warn("Failed to calculate compatibility score for group {}: {}", response.getGroupId(), e.getMessage());
                response.setCompatibilityScore(0.0);
            }
        }
        
        return responses;
    }
    
    /**
     * Responds to an invitation (accept or reject).
     * When accepting, adds the user to the group with their complete profile details.
     */
    public InvitationListResponse respondToInvitation(InvitationResponseRequest request) {
        log.info("User '{}' (email: '{}') responding to invitation '{}'", 
                request.getUserId(), request.getUserEmail(), request.getInvitationId());
        
        try {
            // Find invitation
            Invitation invitation = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new JoinRequestNotFoundException("Invitation not found: " + request.getInvitationId()));
            
            // Validate invitation is for this user (by email)
            if (!invitation.getInvitedEmail().equalsIgnoreCase(request.getUserEmail())) {
                throw new UnauthorizedGroupAccessException(
                    "This invitation was sent to " + invitation.getInvitedEmail() + " but you are trying to respond with " + request.getUserEmail());
            }
            
            if (!invitation.isPending()) {
                throw new InvalidGroupOperationException("Invitation is no longer pending (status: " + invitation.getStatus() + ")");
            }
            
            // Check if invitation has expired
            if (invitation.isExpired()) {
                invitation.markExpired();
                invitationRepository.save(invitation);
                throw new InvalidGroupOperationException("Invitation has expired");
            }
            
            // Find group
            Group group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + invitation.getGroupId()));
            
            InvitationListResponse response = new InvitationListResponse();
            
            if ("accept".equalsIgnoreCase(request.getAction())) {
                // Validate group has space
                if (group.isFull()) {
                    throw new InvalidGroupOperationException("Group is full (max " + group.getMaxMembers() + " members)");
                }
                
                // Check if user is already a member
                if (group.isMember(request.getUserId())) {
                    throw new InvalidGroupOperationException("You are already a member of this group");
                }
                
                // Use invitation data if available, otherwise fetch from user service
                String firstName = invitation.getInvitedFirstName();
                String lastName = invitation.getInvitedLastName();
                String nationality = invitation.getInvitedNationality();
                String dob = invitation.getInvitedDob();
                List<String> languages = invitation.getInvitedLanguages();
                int profileCompletion = invitation.getInvitedProfileCompletion() != null ? 
                    invitation.getInvitedProfileCompletion() : 0;
                
                // Use profile data already stored in the invitation
                if (firstName != null && lastName != null) {
                    log.info("Using profile data from invitation for user '{}'", request.getUserId());
                } else {
                    // Fallback: Fetch fresh profile data from user service
                    log.warn("Invitation missing profile data, fetching from user service for email: {}", request.getUserEmail());
                    try {
                        UserServiceClient.UserProfile userProfile = userServiceClient.getUserByEmail(request.getUserEmail());
                        if (userProfile != null) {
                            firstName = userProfile.getFirstName();
                            lastName = userProfile.getLastName();
                            nationality = userProfile.getNationality();
                            dob = userProfile.getDob();
                            languages = userProfile.getLanguages();
                            profileCompletion = userProfile.getProfileCompletion() != null ? 
                                userProfile.getProfileCompletion() : 0;
                        } else {
                            log.error("Could not fetch user profile for email: {}", request.getUserEmail());
                            throw new InvalidGroupOperationException("Could not retrieve user profile");
                        }
                    } catch (Exception e) {
                        log.error("Error fetching user profile: {}", e.getMessage());
                        throw new InvalidGroupOperationException("Failed to retrieve user profile: " + e.getMessage());
                    }
                }
                
                // Create member using factory method (same pattern as join request acceptance)
                Group.Member newMember = Group.Member.createFromUserProfile(
                    request.getUserId(),
                    invitation.getInvitedEmail(),
                    firstName,
                    lastName,
                    nationality,
                    languages != null ? languages : new ArrayList<>(),
                    dob != null ? dob : "",
                    profileCompletion,
                    false // not creator
                );
                
                // Add member to group
                group.addMember(newMember);
                group.getUserIds().add(request.getUserId());
                
                // Update invitation status
                invitation.accept();
                
                // Add action to group
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "INVITATION_ACCEPTED",
                    newMember.getFirstName() + " " + newMember.getLastName() + " accepted invitation and joined the group"
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Invitation accepted successfully. You are now a member of " + group.getGroupName() + "!");
                
                log.info("User '{}' successfully joined group '{}' via invitation acceptance", 
                        request.getUserId(), group.getId());
                
            } else if ("reject".equalsIgnoreCase(request.getAction())) {
                // Reject invitation
                invitation.reject();
                
                // Add action to group
                String displayName = invitation.getInvitedDisplayName() != null ? 
                    invitation.getInvitedDisplayName() : request.getUserEmail();
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "INVITATION_REJECTED",
                    displayName + " rejected invitation" + 
                    (request.getMessage() != null ? ": " + request.getMessage() : "")
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setMessage("Invitation rejected.");
                
                log.info("User '{}' rejected invitation '{}'", request.getUserId(), invitation.getId());
                
            } else {
                throw new InvalidGroupOperationException("Invalid action. Must be 'accept' or 'reject'");
            }
            
            // Save changes
            invitationRepository.save(invitation);
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            return response;
            
        } catch (GroupNotFoundException | JoinRequestNotFoundException | 
                 UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error responding to invitation: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to respond to invitation: " + e.getMessage());
        }
    }
    
    /**
     * Approves or rejects a join request.
     * Legacy method - now supports both single-admin and multi-member approval.
     */
    public JoinGroupResponse approveJoinRequest(String groupId, ApproveJoinRequestRequest request) {
        log.info("User '{}' reviewing join request '{}' for group '{}'", 
            request.getUserId(), request.getJoinRequestId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions (only group members can approve)
            if (!group.isMember(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("Only group members can vote on join requests");
            }
            
            // Find join request
            JoinRequest joinRequest = group.getJoinRequests().stream()
                .filter(jr -> jr.getId().equals(request.getJoinRequestId()))
                .findFirst()
                .orElseThrow(() -> new JoinRequestNotFoundException("Join request not found"));
            
            if (!joinRequest.isPending()) {
                throw new InvalidGroupOperationException("Join request is no longer pending");
            }
            
            // Check if user has already voted
            if (joinRequest.hasMemberResponded(request.getUserId())) {
                throw new InvalidGroupOperationException("You have already voted on this join request");
            }
            
            JoinGroupResponse response = new JoinGroupResponse();
            response.setGroupId(groupId);
            
            if ("approve".equals(request.getAction())) {
                joinRequest.addMemberApproval(request.getUserId(), "approve", null);
                
                // Check if all members have now approved
                if (joinRequest.hasAllMembersApproved(group.getUserIds())) {
                    if (group.isFull()) {
                        throw new InvalidGroupOperationException("Group is full");
                    }
                    
                    joinRequest.finalizeBasedOnApprovals(group.getUserIds());
                    group.addUser(joinRequest.getUserId());
                    
                    // Fetch approved user profile details and add as member
                    try {
                        // Try to get profile by email first (more reliable), then fallback to UID
                        UserServiceClient.UserProfile userProfile = null;
                        
                        if (joinRequest.getUserEmail() != null && !joinRequest.getUserEmail().trim().isEmpty()) {
                            log.info("Fetching profile for approved user by email '{}'", joinRequest.getUserEmail());
                            userProfile = userServiceClient.getUserByEmail(joinRequest.getUserEmail());
                        }
                        
                        if (userProfile != null) {
                            log.info("Successfully fetched profile for approved user: {} {} ({})", 
                                userProfile.getFirstName(), userProfile.getLastName(), userProfile.getEmail());
                            
                            Group.Member member = Group.Member.createFromUserProfile(
                                joinRequest.getUserId(),
                                userProfile.getEmail(),
                                userProfile.getFirstName(),
                                userProfile.getLastName(),
                                userProfile.getNationality(),
                                userProfile.getLanguages(), // Now available from enhanced UserProfile
                                userProfile.getDob() != null ? userProfile.getDob() : "",
                                userProfile.getProfileCompletion() != null ? userProfile.getProfileCompletion() : 0,
                                false // isCreator
                            );
                            group.addMember(member);
                        } else {
                            log.warn("Could not fetch profile for approved user '{}' (email: '{}'), using fallback data", 
                                joinRequest.getUserId(), joinRequest.getUserEmail());
                            // Fallback: create member with available data from join request
                            Group.Member member = Group.Member.createFromUserProfile(
                                joinRequest.getUserId(),
                                joinRequest.getUserEmail(), // Use email from join request
                                joinRequest.getUserName(), // Use name from join request if available
                                null, // lastName not available
                                null, // nationality not available
                                null, // languages not available
                                "", // dob not available
                                0, // profileCompletion not available
                                false // isCreator
                            );
                            group.addMember(member);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching profile for approved user '{}': {}", joinRequest.getUserId(), e.getMessage());
                        // Fallback: create member with available data from join request
                        Group.Member member = Group.Member.createFromUserProfile(
                            joinRequest.getUserId(),
                            joinRequest.getUserEmail(), // Use email from join request
                            joinRequest.getUserName(), // Use name from join request if available
                            null, // lastName not available
                            null, // nationality not available
                            null, // languages not available
                            "", // dob not available
                            0, // profileCompletion not available
                            false // isCreator
                        );
                        group.addMember(member);
                    }
                    
                    // Add action
                    GroupAction action = GroupAction.create(
                        request.getUserId(),
                        "JOIN_REQUEST_APPROVED_ALL_MEMBERS",
                        "Join request approved by all members for user: " + joinRequest.getUserId()
                    );
                    group.getActions().add(action);
                    
                    response.setStatus("success");
                    response.setMessage("Join request approved by all members. User has been added to the group.");
                } else {
                    // Add action for individual approval
                    GroupAction action = GroupAction.create(
                        request.getUserId(),
                        "JOIN_REQUEST_MEMBER_APPROVED",
                        "Member approved join request for user: " + joinRequest.getUserId()
                    );
                    group.getActions().add(action);
                    
                    response.setStatus("pending");
                    response.setMessage("Your approval recorded. Waiting for approval from remaining members.");
                }
                
            } else if ("reject".equals(request.getAction())) {
                joinRequest.addMemberApproval(request.getUserId(), "reject", request.getReason());
                joinRequest.finalizeBasedOnApprovals(group.getUserIds());
                
                // Add action
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_REJECTED_BY_MEMBER",
                    "Join request rejected by member for user: " + joinRequest.getUserId()
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
     * Creates a new public pooling group with trip planning.
     * This method creates both the group and the trip simultaneously.
     *
     * @param request The group with trip creation request
     * @return CreateGroupWithTripResponse with group and trip details
     * @throws GroupCreationException if creation fails
     */
    public CreateGroupWithTripResponse createGroupWithTrip(CreateGroupWithTripRequest request) {
        log.info("Creating {} group with trip for user {}", 
                request.getVisibility() != null ? request.getVisibility() : "private", request.getUserId());
        
        try {
            // Validate input
            validateCreateGroupWithTripRequest(request);
            
            // Set default visibility if not specified
            if (request.getVisibility() == null || request.getVisibility().trim().isEmpty()) {
                request.setVisibility("private");
            }
            
            // Set default max members based on configurable limits
            if (request.getMaxMembers() == null || request.getMaxMembers() < 2 || request.getMaxMembers() > 20) {
                request.setMaxMembers(6); // Default value
            }
            
            // Create trip first with group type
            Map<String, Object> tripData = buildTripData(request);
            tripData.put("type", "group"); // Mark as group trip
            String groupId = UUID.randomUUID().toString();
            tripData.put("groupId", groupId); // Include group ID in trip data
            Map<String, Object> tripResponse = itineraryServiceClient.createTripPlan(request.getUserId(), tripData).block();
            
            if (tripResponse == null || !"success".equals(tripResponse.get("status"))) {
                throw new GroupCreationException("Failed to create trip plan");
            }
            
            String tripId = (String) tripResponse.get("tripId");
            
            // Create group entity
            Group group = new Group();
            group.setId(groupId);
            group.setGroupName(request.getGroupName());
            group.setTripId(tripId);
            group.setTripName(request.getTripName());
            group.setVisibility(request.getVisibility());
            group.setCreatorUserId(request.getUserId());
            group.setCreatedBy(request.getUserId()); // Set both fields for consistency
            group.setCreatorEmail(request.getEmail()); // Store creator email for name lookup
            group.setMaxMembers(request.getMaxMembers());
            group.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : false);
            group.setUserIds(List.of(request.getUserId()));
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Fetch creator profile details and add as member
            try {
                // Try to get profile by email first (more reliable), then fallback to UID
                UserServiceClient.UserProfile creatorProfile = null;
                
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    log.info("Fetching creator profile by email '{}'", request.getEmail());
                    creatorProfile = userServiceClient.getUserByEmail(request.getEmail());
                }
                
                // Fallback to UID if email lookup failed
                if (creatorProfile == null) {
                    log.info("Email lookup failed, trying UID lookup for creator '{}'", request.getUserId());
                    creatorProfile = userServiceClient.getUserByUid(request.getUserId());
                }
                
                if (creatorProfile != null) {
                    log.info("Successfully fetched profile for creator: {} {} ({})", 
                        creatorProfile.getFirstName(), creatorProfile.getLastName(), creatorProfile.getEmail());
                    
                    Group.Member creatorMember = Group.Member.createFromUserProfile(
                        request.getUserId(),
                        creatorProfile.getEmail(),
                        creatorProfile.getFirstName(),
                        creatorProfile.getLastName(),
                        creatorProfile.getNationality(),
                        creatorProfile.getLanguages(), // Now available from enhanced UserProfile
                        creatorProfile.getDob() != null ? creatorProfile.getDob() : "",
                        creatorProfile.getProfileCompletion() != null ? creatorProfile.getProfileCompletion() : 0,
                        true // isCreator
                    );
                    group.addMember(creatorMember);
                } else {
                    log.warn("Could not fetch profile for creator '{}' (email: '{}'), using fallback data", 
                        request.getUserId(), request.getEmail());
                    // Fallback: create member with available data
                    Group.Member creatorMember = Group.Member.createFromUserProfile(
                        request.getUserId(),
                        request.getEmail(),
                        null, // firstName not available
                        null, // lastName not available
                        null, // nationality not available
                        null, // languages not available
                        "", // dob not available
                        0, // profileCompletion not available
                        true // isCreator
                    );
                    group.addMember(creatorMember);
                }
            } catch (Exception e) {
                log.error("Error fetching creator profile for user '{}': {}", request.getUserId(), e.getMessage());
                // Fallback: create member with minimal data
                Group.Member creatorMember = Group.Member.createFromUserProfile(
                    request.getUserId(),
                    request.getEmail(),
                    null, // firstName not available
                    null, // lastName not available
                    null, // nationality not available  
                    null, // languages not available
                    "", // dob not available
                    0, // profileCompletion not available
                    true // isCreator
                );
                group.addMember(creatorMember);
            }
            
            // Set status based on visibility for hybrid workflow
            if ("public".equals(request.getVisibility())) {
                group.setStatus("draft"); // Public groups start as draft for suggestions
            } else {
                group.setStatus("finalized"); // Private groups are immediately finalized
            }
            
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
            preferences.put("preferredTerrains", request.getPreferredTerrains() != null ? request.getPreferredTerrains() : new ArrayList<>());
            preferences.put("preferredActivities", request.getPreferredActivities() != null ? request.getPreferredActivities() : new ArrayList<>());
            preferences.putAll(request.getAdditionalPreferences() != null ? request.getAdditionalPreferences() : new HashMap<>());
            group.setPreferences(preferences);
            
            // Add creation action
            GroupAction createAction = GroupAction.create(
                request.getUserId(),
                "GROUP_WITH_TRIP_CREATED",
                String.format("Created %s group with trip: %s", request.getVisibility(), request.getTripName())
            );
            group.setActions(List.of(createAction));
            
            // Save group
            Group savedGroup = groupRepository.save(group);
            
            // Create response
            CreateGroupWithTripResponse response = new CreateGroupWithTripResponse();
            response.setStatus("success");
            response.setGroupId(savedGroup.getId());
            response.setTripId(tripId);
            response.setDraft("public".equals(request.getVisibility())); // Only public groups are drafts
            
            if ("public".equals(request.getVisibility())) {
                response.setMessage("Public group created as draft. Complete trip planning to get suggestions or finalize directly.");
            } else {
                response.setMessage("Private group created and finalized successfully.");
            }
            
            log.info("Group with trip created successfully: groupId={}, tripId={}, visibility={}, status={}", 
                    groupId, tripId, request.getVisibility(), group.getStatus());
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
            
            // Log creator information for debugging
            log.debug("Group creator validation - Group ID: {}, Requesting User: {}, CreatorUserId: {}, CreatedBy: {}", 
                     groupId, request.getUserId(), group.getCreatorUserId(), group.getCreatedBy());
            
            // Verify user is creator
            if (!group.isCreator(request.getUserId())) {
                log.warn("User {} is not authorized to finalize group {}. Creator: {} / {}", 
                        request.getUserId(), groupId, group.getCreatorUserId(), group.getCreatedBy());
                throw new UnauthorizedGroupAccessException("Only the group creator can finalize the group");
            }
            
            FinalizeTripResponse response = new FinalizeTripResponse();
            response.setGroupId(groupId);
            response.setTripId(group.getTripId());
            
            if ("finalize".equals(request.getAction())) {
                // Finalize current group directly (for private groups or when user chooses to proceed)
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
                
            } else if ("checkSuggestions".equals(request.getAction())) {
                // Get suggestions for public groups only
                if (!"public".equals(group.getVisibility())) {
                    throw new InvalidGroupOperationException("Suggestions are only available for public groups");
                }
                
                if (!"draft".equals(group.getStatus())) {
                    throw new InvalidGroupOperationException("Group must be in draft status to check suggestions");
                }
                
                // Get trip suggestions using compatibility service
                try {
                    // For now, we'll call the existing getTripSuggestions method
                    // In a real implementation, this might call the trip-planning service for full trip data
                    Map<String, Object> mockTripData = new HashMap<>();
                    List<TripSuggestionsResponse.CompatibleGroup> suggestions = 
                        tripCompatibilityService.findCompatibleGroups(group, mockTripData);
                    
                    response.setStatus("success");
                    response.setAction("suggestions");
                    response.setSuccess(true);
                    
                    if (suggestions.isEmpty()) {
                        response.setMessage("No compatible groups found. You can finalize your trip.");
                        response.setSuggestions(new ArrayList<>());
                    } else {
                        response.setMessage(String.format("Found %d compatible group(s)", suggestions.size()));
                        response.setSuggestions(suggestions);
                    }
                    
                    log.info("Generated {} suggestions for group {}", suggestions.size(), groupId);
                    
                } catch (Exception e) {
                    log.warn("Failed to get suggestions for group {}: {}", groupId, e.getMessage());
                    response.setStatus("success");
                    response.setAction("suggestions");
                    response.setSuccess(true);
                    response.setMessage("Unable to find suggestions at this time. You can finalize your trip.");
                    response.setSuggestions(new ArrayList<>());
                }
                
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
                throw new InvalidGroupOperationException("Invalid action: " + request.getAction() + 
                    ". Supported actions: finalize, checkSuggestions, join");
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
     * Allows a group member to vote on a join request.
     * Supports the multi-member approval system.
     */
    public MemberVoteResponse voteOnJoinRequest(String groupId, MemberVoteRequest request) {
        log.info("Member '{}' voting '{}' on join request '{}' for group '{}'", 
            request.getUserId(), request.getAction(), request.getJoinRequestId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions (only group members can vote)
            if (!group.isMember(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("Only group members can vote on join requests");
            }
            
            // Find join request
            JoinRequest joinRequest = group.getJoinRequests().stream()
                .filter(jr -> jr.getId().equals(request.getJoinRequestId()))
                .findFirst()
                .orElseThrow(() -> new JoinRequestNotFoundException("Join request not found"));
            
            if (!joinRequest.isPending()) {
                throw new InvalidGroupOperationException("Join request is no longer pending");
            }
            
            // Check if user has already voted
            if (joinRequest.hasMemberResponded(request.getUserId())) {
                throw new InvalidGroupOperationException("You have already voted on this join request");
            }
            
            // Add the member's vote
            joinRequest.addMemberApproval(request.getUserId(), request.getAction(), request.getReason());
            
            MemberVoteResponse response = new MemberVoteResponse();
            response.setGroupId(groupId);
            response.setJoinRequestId(request.getJoinRequestId());
            
            // Check if all members have now responded
            if ("reject".equals(request.getAction()) || joinRequest.hasAnyMemberRejected()) {
                // If any member rejects, the request is rejected
                joinRequest.finalizeBasedOnApprovals(group.getUserIds());
                
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_REJECTED_BY_MEMBER",
                    "Join request rejected by member for user: " + joinRequest.getUserId()
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setRequestStatus("rejected");
                response.setMessage("Join request rejected");
                
            } else if (joinRequest.hasAllMembersApproved(group.getUserIds())) {
                // All members have approved
                if (group.isFull()) {
                    throw new InvalidGroupOperationException("Group is full");
                }
                
                joinRequest.finalizeBasedOnApprovals(group.getUserIds());
                group.addUserFromJoinRequest(joinRequest);
                
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_APPROVED_ALL_MEMBERS",
                    "Join request approved by all members for user: " + joinRequest.getUserId()
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setRequestStatus("approved");
                response.setMessage("Join request approved by all members. User has been added to the group.");
                
            } else {
                // Still pending more votes
                GroupAction action = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_MEMBER_VOTED",
                    "Member voted on join request for user: " + joinRequest.getUserId()
                );
                group.getActions().add(action);
                
                response.setStatus("success");
                response.setRequestStatus("pending");
                response.setMessage("Your vote recorded. Waiting for votes from remaining members.");
            }
            
            // Set additional response information
            response.setPendingMembers(joinRequest.getPendingMemberIds(group.getUserIds()));
            response.setTotalVotesReceived(joinRequest.getMemberApprovals().size());
            response.setTotalMembersRequired(group.getUserIds().size());
            
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            return response;
            
        } catch (GroupNotFoundException | InvalidGroupOperationException | UnauthorizedGroupAccessException | JoinRequestNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing vote for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to process vote: " + e.getMessage());
        }
    }
    
    /**
     * Gets pending join requests for a group that require member votes.
     */
    public PendingJoinRequestsResponse getPendingJoinRequests(String groupId, String userId) {
        log.info("Getting pending join requests for group '{}' by user '{}'", groupId, userId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions (only group members can see pending requests)
            if (!group.isMember(userId)) {
                throw new UnauthorizedGroupAccessException("Only group members can view pending join requests");
            }
            
            PendingJoinRequestsResponse response = new PendingJoinRequestsResponse();
            response.setStatus("success");
            
            List<PendingJoinRequestsResponse.PendingJoinRequestInfo> pendingInfos = group.getJoinRequests().stream()
                .filter(JoinRequest::isPending)
                .map(joinRequest -> {
                    PendingJoinRequestsResponse.PendingJoinRequestInfo info = new PendingJoinRequestsResponse.PendingJoinRequestInfo();
                    info.setJoinRequestId(joinRequest.getId());
                    info.setUserId(joinRequest.getUserId());
                    info.setUserName(joinRequest.getUserName());
                    info.setUserEmail(joinRequest.getUserEmail());
                    info.setMessage(joinRequest.getMessage());
                    info.setRequestedAt(joinRequest.getRequestedAt().toString());
                    info.setPendingMembers(joinRequest.getPendingMemberIds(group.getUserIds()));
                    info.setTotalVotesReceived(joinRequest.getMemberApprovals().size());
                    info.setTotalMembersRequired(group.getUserIds().size());
                    info.setHasCurrentUserVoted(joinRequest.hasMemberResponded(userId));
                    return info;
                })
                .toList();
            
            response.setPendingRequests(pendingInfos);
            
            return response;
            
        } catch (GroupNotFoundException | UnauthorizedGroupAccessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting pending join requests for group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get pending join requests: " + e.getMessage());
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
    
    /**
     * Builds member summaries with real user names from UserServiceClient.
     */
    /**
     * Builds member summaries using stored member data from the group.
     * No longer makes external calls to UserServiceClient since we store member details locally.
     */
    private List<ComprehensiveTripResponse.MemberSummary> buildMemberSummaries(Group group) {
        log.info("Building member summaries for group '{}' with {} stored members", group.getId(), group.getMembers().size());
        
        // If we have stored member data, use it
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            return group.getMembers().stream()
                .map(member -> {
                    log.info("Using stored member data - userId: '{}', name: '{}', email: '{}'", 
                        member.getUserId(), member.getFullName(), member.getEmail());
                    
                    return ComprehensiveTripResponse.MemberSummary.builder()
                        .userId(member.getUserId())
                        .name(member.getFullName())
                        .email(member.getEmail() != null ? member.getEmail() : member.getUserId() + "@example.com")
                        .role(member.isCreator() ? "leader" : "member")
                        .joinedAt(member.getJoinedAt() != null ? member.getJoinedAt() : group.getCreatedAt())
                        .status("active")
                        .preferences(extractMemberPreferences(group))
                        .build();
                })
                .collect(Collectors.toList());
        }
        
        // Fallback: if no stored member data, use userIds with minimal info
        log.warn("No stored member data found for group '{}', using fallback method", group.getId());
        return group.getUserIds().stream()
            .map(userId -> {
                boolean isLeader = userId.equals(group.getCreatorUserId()) || userId.equals(group.getCreatedBy());
                
                log.info("Using fallback member data - userId: '{}'", userId);
                
                return ComprehensiveTripResponse.MemberSummary.builder()
                    .userId(userId)
                    .name("User " + userId)
                    .email(userId + "@example.com")
                    .role(isLeader ? "leader" : "member")
                    .joinedAt(group.getCreatedAt())
                    .status("active")
                    .preferences(extractMemberPreferences(group))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Extract member preferences from group preferences.
     */
    private ComprehensiveTripResponse.TravelPreferences extractMemberPreferences(Group group) {
        Map<String, Object> groupPrefs = group.getPreferences();
        if (groupPrefs == null) {
            return ComprehensiveTripResponse.TravelPreferences.builder()
                .budgetLevel("Medium")
                .preferredActivities(new ArrayList<>())
                .preferredTerrains(new ArrayList<>())
                .activityPacing("Normal")
                .build();
        }
        
        return ComprehensiveTripResponse.TravelPreferences.builder()
            .budgetLevel((String) groupPrefs.getOrDefault("budgetLevel", "Medium"))
            .preferredActivities((List<String>) groupPrefs.getOrDefault("preferredActivities", new ArrayList<>()))
            .preferredTerrains((List<String>) groupPrefs.getOrDefault("preferredTerrains", new ArrayList<>()))
            .activityPacing((String) groupPrefs.getOrDefault("activityPacing", "Normal"))
            .build();
    }
    
    /**
     * Converts a JoinRequest to PendingJoinRequestInfo for the consolidated response.
     */
    private AllPendingRequestsResponse.PendingJoinRequestInfo convertToPendingJoinRequestInfo(
            JoinRequest joinRequest, Group group, String currentUserId) {
        
        AllPendingRequestsResponse.PendingJoinRequestInfo info = new AllPendingRequestsResponse.PendingJoinRequestInfo();
        
        // Basic request information
        info.setJoinRequestId(joinRequest.getId());
        info.setUserId(joinRequest.getUserId());
        info.setUserName(joinRequest.getUserName());
        info.setUserEmail(joinRequest.getUserEmail());
        info.setMessage(joinRequest.getMessage());
        info.setRequestedAt(joinRequest.getRequestedAt().toString());
        
        // Voting information
        info.setPendingMembers(joinRequest.getPendingMemberIds(group.getUserIds()));
        info.setTotalVotesReceived(joinRequest.getMemberApprovals().size());
        info.setTotalMembersRequired(group.getUserIds().size());
        info.setHasCurrentUserVoted(joinRequest.hasMemberResponded(currentUserId));
        
        // Calculate urgency level based on request age
        long hoursSinceRequest = java.time.Duration.between(joinRequest.getRequestedAt(), Instant.now()).toHours();
        if (hoursSinceRequest > 48) {
            info.setUrgencyLevel("high");
        } else if (hoursSinceRequest > 24) {
            info.setUrgencyLevel("medium");
        } else {
            info.setUrgencyLevel("low");
        }
        
        // Extract user profile if available
        if (joinRequest.getUserProfile() != null) {
            AllPendingRequestsResponse.UserProfileSummary profileSummary = new AllPendingRequestsResponse.UserProfileSummary();
            Map<String, Object> profile = joinRequest.getUserProfile();
            
            profileSummary.setAge((Integer) profile.get("age"));
            profileSummary.setNationality((String) profile.get("nationality"));
            profileSummary.setInterests((List<String>) profile.get("interests"));
            profileSummary.setTravelExperience((String) profile.get("travelExperience"));
            profileSummary.setBudgetLevel((String) profile.get("budgetLevel"));
            profileSummary.setActivityPacing((String) profile.get("activityPacing"));
            profileSummary.setPreferredTerrains((List<String>) profile.get("preferredTerrains"));
            profileSummary.setPreferredActivities((List<String>) profile.get("preferredActivities"));
            
            info.setUserProfile(profileSummary);
        }
        
        return info;
    }
    
    /**
     * Allows a member to vote on a join request by user ID.
     * This method provides a cleaner API where the requesting user ID is in the path.
     *
     * @param groupId The ID of the group
     * @param requestUserId The ID of the user who made the join request
     * @param request The vote request containing voter info and decision
     * @return JoinRequestVoteResponse with the vote result
     */
    public JoinRequestVoteResponse voteOnJoinRequestByUserId(String groupId, String requestUserId, JoinRequestVoteRequest request) {
        log.info("Processing vote on join request from user '{}' for group '{}' by member '{}'", 
                 requestUserId, groupId, request.getUserId());
        
        try {
            // Find the group
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));
            
            // Verify voter is a member
            if (!group.getUserIds().contains(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("User is not a member of this group");
            }
            
            // Find the join request
            JoinRequest joinRequest = group.getJoinRequests().stream()
                    .filter(jr -> jr.getUserId().equals(requestUserId))
                    .findFirst()
                    .orElseThrow(() -> new JoinRequestNotFoundException("No pending join request found for user: " + requestUserId));
            
            // Check if user has already voted
            if (joinRequest.hasMemberResponded(request.getUserId())) {
                throw new InvalidGroupOperationException("User has already voted on this join request");
            }
            
            // Record the vote
            String action = request.isApproved() ? "approve" : "reject";
            joinRequest.addMemberApproval(request.getUserId(), action, request.getComment());
            
            JoinRequestVoteResponse response = new JoinRequestVoteResponse();
            response.setVoterUserId(request.getUserId());
            response.setRequestUserId(requestUserId);
            response.setGroupId(groupId);
            response.setVoteDecision(request.isApproved() ? "approved" : "rejected");
            response.setComment(request.getComment());
            response.setVotedAt(Instant.now());
            
            // Check if any member has rejected or all have approved
            if (joinRequest.hasAnyMemberRejected()) {
                // Remove the join request
                group.getJoinRequests().remove(joinRequest);
                
                GroupAction action1 = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_REJECTED",
                    "Join request rejected for user: " + requestUserId
                );
                group.getActions().add(action1);
                
                response.setRequestStatus("rejected");
                response.setMessage("Join request has been rejected by all members and removed");
                
            } else if (joinRequest.hasAllMembersApproved(group.getUserIds())) {
                // All members have approved - add user to group
                if (group.isFull()) {
                    throw new InvalidGroupOperationException("Group is full");
                }
                
                // Remove the join request and add user
                group.getJoinRequests().remove(joinRequest);
                group.addUserFromJoinRequest(joinRequest);
                
                GroupAction approvalAction = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_APPROVED_ALL_MEMBERS",
                    "Join request approved by all members for user: " + requestUserId
                );
                group.getActions().add(approvalAction);
                
                response.setRequestStatus("approved");
                response.setMessage("Join request has been approved by all members. User added to group.");
                
            } else {
                // Still pending more votes
                GroupAction voteAction = GroupAction.create(
                    request.getUserId(),
                    "JOIN_REQUEST_MEMBER_VOTED",
                    "Member voted on join request for user: " + requestUserId
                );
                group.getActions().add(voteAction);
                
                response.setRequestStatus("pending");
                response.setMessage("Vote recorded. Waiting for votes from remaining members.");
            }
            
            // Set additional response information
            response.setTotalVotesReceived(joinRequest.getMemberApprovals().size());
            response.setTotalMembersRequired(group.getUserIds().size());
            response.setPendingMemberIds(joinRequest.getPendingMemberIds(group.getUserIds()));
            
            group.setLastUpdated(Instant.now());
            groupRepository.save(group);
            
            log.info("Vote processed successfully: {} vote by '{}' for request from '{}'", 
                     request.isApproved() ? "APPROVE" : "REJECT", request.getUserId(), requestUserId);
            
            return response;
            
        } catch (GroupNotFoundException | InvalidGroupOperationException | UnauthorizedGroupAccessException | JoinRequestNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing vote on join request: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to process vote: " + e.getMessage());
        }
    }
    
    /**
     * Get all invitations for a specific user.
     * Returns all pending invitations sent to the user across all groups.
     *
     * @param userId The ID of the user to get invitations for
     * @return UserInvitationsResponse with all invitations for the user
     */
    public UserInvitationsResponse getUserInvitations(String userId) {
        try {
            log.info("Getting all invitations for user '{}'", userId);
            
            // Find all invitations for this user across all groups
            List<Invitation> userInvitations = invitationRepository.findByInvitedUserIdAndStatus(userId, "pending");
            
            List<InvitationDetail> invitationDetails = userInvitations.stream()
                    .map(this::convertToInvitationDetail)
                    .collect(Collectors.toList());
            
            UserInvitationsResponse response = new UserInvitationsResponse();
            response.setInvitations(invitationDetails);
            response.setTotalInvitations(invitationDetails.size());
            
            log.info("Successfully retrieved {} invitations for user '{}'", invitationDetails.size(), userId);
            return response;
            
        } catch (Exception e) {
            log.error("Unexpected error getting invitations for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get invitations: " + e.getMessage());
        }
    }
    
    /**
     * Convert an Invitation entity to InvitationDetail DTO.
     */
    private InvitationDetail convertToInvitationDetail(Invitation invitation) {
        InvitationDetail detail = new InvitationDetail();
        detail.setInvitationId(invitation.getId());
        detail.setGroupId(invitation.getGroupId());
        detail.setInviterEmail(invitation.getInviterEmail());
        detail.setInvitedAt(invitation.getInvitedAt());
        detail.setMessage(invitation.getMessage());
        detail.setStatus(invitation.getStatus());
        
        // Get group details
        Group group = groupRepository.findById(invitation.getGroupId()).orElse(null);
        if (group != null) {
            detail.setGroupName(group.getGroupName());
            detail.setGroupDescription(group.getTripName() != null ? group.getTripName() : "Trip Group");
            detail.setMemberCount(group.getUserIds().size());
            detail.setMaxMembers(group.getMaxMembers());
            detail.setBaseCity("Colombo"); // Default city, could be extracted from preferences
            
            if (group.getPreferences() != null) {
                @SuppressWarnings("unchecked")
                List<String> activities = (List<String>) group.getPreferences().get("preferredActivities");
                detail.setPreferredActivities(activities != null ? activities : List.of("Cultural Tours", "Food Experiences"));
            } else {
                detail.setPreferredActivities(List.of("Cultural Tours", "Food Experiences"));
            }
            
            // Get inviter name from user service
            String inviterName = userServiceClient.getUserNameByUid(group.getCreatorUserId());
            if (inviterName == null || inviterName.equals(group.getCreatorUserId())) {
                inviterName = userServiceClient.getUserNameByEmail(group.getCreatorEmail());
            }
            if (inviterName == null || inviterName.equals(group.getCreatorEmail())) {
                inviterName = "Group Admin";
            }
            detail.setInviterName(inviterName);
            
            // Get trip dates from trip service
            try {
                if (group.getTripId() != null) {
                    // This would require calling the trip service to get trip details
                    // For now, setting placeholder dates
                    detail.setTripStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
                    detail.setTripEndDate(Instant.now().plus(37, ChronoUnit.DAYS));
                }
            } catch (Exception e) {
                log.warn("Could not fetch trip dates for trip ID: {}", group.getTripId());
                // Set fallback dates
                detail.setTripStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
                detail.setTripEndDate(Instant.now().plus(37, ChronoUnit.DAYS));
            }
        }
        
        return detail;
    }
    
    /**
     * Convert an Invitation entity to PendingInvitation DTO for comprehensive response.
     */
    private ComprehensivePendingItemsResponse.PendingInvitation convertToPendingInvitation(Invitation invitation) {
        ComprehensivePendingItemsResponse.PendingInvitation pendingInvitation = 
            new ComprehensivePendingItemsResponse.PendingInvitation();
        
        pendingInvitation.setInvitationId(invitation.getId());
        pendingInvitation.setGroupId(invitation.getGroupId());
        pendingInvitation.setInviterEmail(invitation.getInviterEmail());
        pendingInvitation.setMessage(invitation.getMessage());
        pendingInvitation.setInvitedAt(invitation.getInvitedAt());
        
        // Calculate expiration (30 days from invitation)
        Instant expiresAt = invitation.getInvitedAt().plus(30, ChronoUnit.DAYS);
        pendingInvitation.setExpiresAt(expiresAt);
        
        // Calculate urgency
        long daysRemaining = ChronoUnit.DAYS.between(Instant.now(), expiresAt);
        pendingInvitation.setDaysRemaining((int) daysRemaining);
        
        if (daysRemaining <= 2) {
            pendingInvitation.setUrgencyLevel("high");
        } else if (daysRemaining <= 7) {
            pendingInvitation.setUrgencyLevel("medium");
        } else {
            pendingInvitation.setUrgencyLevel("low");
        }
        
        // Get group details
        Group group = groupRepository.findById(invitation.getGroupId()).orElse(null);
        if (group != null) {
            pendingInvitation.setGroupName(group.getGroupName());
            pendingInvitation.setTripName(group.getTripName() != null ? group.getTripName() : group.getGroupName());
            pendingInvitation.setCurrentMembers(group.getUserIds().size());
            pendingInvitation.setMaxMembers(group.getMaxMembers());
            
            // Get group preferences
            if (group.getPreferences() != null) {
                pendingInvitation.setBaseCity((String) group.getPreferences().get("baseCity"));
                @SuppressWarnings("unchecked")
                List<String> activities = (List<String>) group.getPreferences().get("preferredActivities");
                pendingInvitation.setPreferredActivities(activities != null ? activities : List.of());
            }
            
            // Get inviter name
            String inviterName = userServiceClient.getUserNameByUid(group.getCreatorUserId());
            if (inviterName == null || inviterName.equals(group.getCreatorUserId())) {
                inviterName = userServiceClient.getUserNameByEmail(group.getCreatorEmail());
            }
            if (inviterName == null) {
                inviterName = "Group Admin";
            }
            pendingInvitation.setInviterName(inviterName);
            
            // Get trip dates (placeholder for now)
            pendingInvitation.setTripStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
            pendingInvitation.setTripEndDate(Instant.now().plus(37, ChronoUnit.DAYS));
        }
        
        return pendingInvitation;
    }
    
    /**
     * Convert an Invitation entity to PendingInvitation DTO with group details.
     * This method fetches group name from the groups collection using the groupId.
     * Uses data already stored in the invitation (inviter display name, trip name, expiry date).
     */
    private ComprehensivePendingItemsResponse.PendingInvitation convertToPendingInvitationWithGroupDetails(Invitation invitation) {
        ComprehensivePendingItemsResponse.PendingInvitation pendingInvitation = 
            new ComprehensivePendingItemsResponse.PendingInvitation();
        
        pendingInvitation.setInvitationId(invitation.getId());
        pendingInvitation.setGroupId(invitation.getGroupId());
        
        // Use inviter display name from invitation (already stored)
        pendingInvitation.setInviterName(invitation.getInviterDisplayName());
        pendingInvitation.setInviterEmail(invitation.getInviterEmail());
        
        // Use message from invitation
        pendingInvitation.setMessage(invitation.getMessage());
        
        // Use dates from invitation
        pendingInvitation.setInvitedAt(invitation.getInvitedAt());
        pendingInvitation.setExpiresAt(invitation.getExpiresAt());
        
        // Calculate urgency based on expiry date
        long daysRemaining = ChronoUnit.DAYS.between(Instant.now(), invitation.getExpiresAt());
        pendingInvitation.setDaysRemaining((int) daysRemaining);
        
        if (daysRemaining <= 2) {
            pendingInvitation.setUrgencyLevel("high");
        } else if (daysRemaining <= 7) {
            pendingInvitation.setUrgencyLevel("medium");
        } else {
            pendingInvitation.setUrgencyLevel("low");
        }
        
        // Get group details from groups collection using groupId
        Group group = groupRepository.findById(invitation.getGroupId()).orElse(null);
        if (group != null) {
            // Use group name from groups collection
            pendingInvitation.setGroupName(group.getGroupName());
            
            // Use trip name from invitation (already stored)
            pendingInvitation.setTripName(invitation.getTripName());
            
            // Set member counts
            pendingInvitation.setCurrentMembers(group.getUserIds().size());
            pendingInvitation.setMaxMembers(group.getMaxMembers());
            
            // Get group preferences
            if (group.getPreferences() != null) {
                pendingInvitation.setBaseCity((String) group.getPreferences().get("baseCity"));
                @SuppressWarnings("unchecked")
                List<String> activities = (List<String>) group.getPreferences().get("preferredActivities");
                pendingInvitation.setPreferredActivities(activities != null ? activities : List.of());
            }
            
            // Get trip dates from group preferences if available
            if (group.getPreferences() != null) {
                String startDate = (String) group.getPreferences().get("startDate");
                String endDate = (String) group.getPreferences().get("endDate");
                
                if (startDate != null && endDate != null) {
                    try {
                        pendingInvitation.setTripStartDate(Instant.parse(startDate + "T00:00:00Z"));
                        pendingInvitation.setTripEndDate(Instant.parse(endDate + "T00:00:00Z"));
                    } catch (Exception e) {
                        log.warn("Could not parse trip dates from group preferences: {}", e.getMessage());
                        // Fallback to placeholder dates
                        pendingInvitation.setTripStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
                        pendingInvitation.setTripEndDate(Instant.now().plus(37, ChronoUnit.DAYS));
                    }
                } else {
                    // Fallback to placeholder dates
                    pendingInvitation.setTripStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
                    pendingInvitation.setTripEndDate(Instant.now().plus(37, ChronoUnit.DAYS));
                }
            }
        } else {
            log.warn("Group not found for invitation with groupId: {}", invitation.getGroupId());
            // Use data from invitation as fallback
            pendingInvitation.setGroupName("Unknown Group");
            pendingInvitation.setTripName(invitation.getTripName());
            pendingInvitation.setCurrentMembers(0);
            pendingInvitation.setMaxMembers(5);
            pendingInvitation.setPreferredActivities(List.of());
        }
        
        return pendingInvitation;
    }
    
    /**
     * Convert a JoinRequest entity to PendingVoteRequest DTO for comprehensive response.
     */
    private ComprehensivePendingItemsResponse.PendingVoteRequest convertToPendingVoteRequest(
            JoinRequest joinRequest, Group group, String currentUserId) {
        
        ComprehensivePendingItemsResponse.PendingVoteRequest voteRequest = 
            new ComprehensivePendingItemsResponse.PendingVoteRequest();
        
        voteRequest.setJoinRequestId(joinRequest.getId());
        voteRequest.setGroupId(group.getId());
        voteRequest.setGroupName(group.getGroupName());
        voteRequest.setTripName(group.getTripName() != null ? group.getTripName() : group.getGroupName());
        voteRequest.setRequestingUserId(joinRequest.getUserId());
        voteRequest.setRequestingUserEmail(joinRequest.getUserEmail());
        voteRequest.setRequestMessage(joinRequest.getMessage());
        voteRequest.setRequestedAt(joinRequest.getRequestedAt());
        
        // Get requesting user's name and profile
        String userName = userServiceClient.getUserNameByUid(joinRequest.getUserId());
        if (userName == null || userName.equals(joinRequest.getUserId())) {
            userName = userServiceClient.getUserNameByEmail(joinRequest.getUserEmail());
        }
        if (userName == null) {
            userName = joinRequest.getUserEmail();
        }
        voteRequest.setRequestingUserName(userName);
        
        // Get complete user profile
        UserServiceClient.UserProfile userProfile = userServiceClient.getUserByEmail(joinRequest.getUserEmail());
        if (userProfile != null) {
            ComprehensivePendingItemsResponse.UserProfile profile = 
                new ComprehensivePendingItemsResponse.UserProfile();
            profile.setUserId(joinRequest.getUserId()); // Use join request user ID since UserProfile doesn't have userId
            profile.setName(userProfile.getFullName()); // Use getFullName() method
            profile.setEmail(userProfile.getEmail());
            profile.setNationality(userProfile.getNationality());
            profile.setLanguages(userProfile.getLanguages());
            profile.setProfileImageUrl(null); // UserProfile doesn't have profile image URL, only byte array
            profile.setProfileComplete(userProfile.getProfileCompletion() != null && userProfile.getProfileCompletion() >= 100);
            voteRequest.setRequestingUserProfile(profile);
        }
        
        // Calculate voting status
        boolean hasCurrentUserVoted = joinRequest.getMemberApprovals().stream()
            .anyMatch(approval -> approval.getMemberId().equals(currentUserId));
        voteRequest.setHasCurrentUserVoted(hasCurrentUserVoted);
        
        if (hasCurrentUserVoted) {
            String vote = joinRequest.getMemberApprovals().stream()
                .filter(approval -> approval.getMemberId().equals(currentUserId))
                .map(approval -> "approve".equals(approval.getAction()) ? "APPROVED" : "REJECTED")
                .findFirst()
                .orElse(null);
            voteRequest.setCurrentUserVote(vote);
        }
        
        voteRequest.setTotalVotesReceived(joinRequest.getMemberApprovals().size());
        voteRequest.setTotalVotesRequired(group.getUserIds().size());
        
        // Find members who haven't voted
        List<String> pendingMemberIds = group.getUserIds().stream()
            .filter(memberId -> joinRequest.getMemberApprovals().stream()
                .noneMatch(approval -> approval.getMemberId().equals(memberId)))
            .collect(Collectors.toList());
        voteRequest.setPendingMemberIds(pendingMemberIds);
        
        // Calculate urgency
        long daysPending = ChronoUnit.DAYS.between(joinRequest.getRequestedAt(), Instant.now());
        voteRequest.setDaysPending((int) daysPending);
        
        if (daysPending >= 7) {
            voteRequest.setUrgencyLevel("high");
        } else if (daysPending >= 3) {
            voteRequest.setUrgencyLevel("medium");
        } else {
            voteRequest.setUrgencyLevel("low");
        }
        
        return voteRequest;
    }
}