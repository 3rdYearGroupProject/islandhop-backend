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
     * Gets list of enhanced public groups with detailed trip and creator information.
     * This version provides comprehensive details for UI display like trip names, creator names,
     * cities, dates, and top attractions.
     */
    public List<EnhancedPublicGroupResponse> getEnhancedPublicGroups(String userId, String baseCity, String startDate, 
                                                                     String endDate, String budgetLevel, List<String> preferredActivities) {
        log.info("Getting enhanced public groups for user '{}' with filters: baseCity={}, startDate={}, endDate={}, budgetLevel={}, activities={}", 
                userId, baseCity, startDate, endDate, budgetLevel, preferredActivities);
        
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
            
            // Convert to enhanced response DTOs
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
            response.setCreatedAt(group.getCreatedAt());
            response.setMemberCount(group.getUserIds().size());
            response.setMaxMembers(group.getMaxMembers());
            response.setMemberCountText(group.getUserIds().size() + " participants / " + group.getMaxMembers());
            
            // Creator information
            String creatorUserId = group.getCreatorUserId();
            response.setCreatorUserId(creatorUserId);
            
            // Get creator name using stored email
            response.setCreatorName(getCreatorName(creatorUserId, group.getCreatorEmail()));
            
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
     * Gets creator name using the stored creator email.
     * Calls user service to get the user's display name.
     */
    private String getCreatorName(String creatorUserId, String creatorEmail) {
        if (creatorEmail != null && !creatorEmail.trim().isEmpty()) {
            try {
                return userServiceClient.getUserNameByEmail(creatorEmail);
            } catch (Exception e) {
                log.warn("Failed to get user name for email {}: {}", creatorEmail, e.getMessage());
            }
        }
        
        // Fallback to placeholder if email not available or lookup fails
        return "Group Creator";
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
            group.setCreatorEmail(request.getUserEmail()); // Store creator email for name lookup
            group.setMaxMembers(request.getMaxMembers());
            group.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : false);
            group.setUserIds(List.of(request.getUserId()));
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
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
            
            // Verify user is creator
            if (!group.isCreator(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("Only group creator can finalize trip");
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
                group.addUser(joinRequest.getUserId());
                
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
}