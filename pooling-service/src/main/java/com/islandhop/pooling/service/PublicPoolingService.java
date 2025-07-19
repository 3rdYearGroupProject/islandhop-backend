package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for public pooling operations including pre-check and suggestions.
 * Handles compatibility scoring and group filtering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublicPoolingService {
    
    private final GroupRepository groupRepository;
    private final TripCompatibilityService tripCompatibilityService;
    
    @Value("${pooling.compatibility.min-score:0.6}")
    private double minCompatibilityScore;
    
    /**
     * Pre-checks for compatible public groups before creating a new one.
     * Performance optimized for repeated requests.
     */
    public PreCheckResponse preCheckGroups(PreCheckRequest request) {
        log.info("Pre-checking compatible groups for user '{}' in '{}' from {} to {}", 
                request.getUserId(), request.getBaseCity(), request.getStartDate(), request.getEndDate());
        
        try {
            // Get all public finalized groups
            List<Group> publicGroups = groupRepository.findByVisibilityAndStatus("public", "finalized");
            log.debug("Found {} public finalized groups to check compatibility", publicGroups.size());
            
            // Filter groups that have space for more members
            List<Group> availableGroups = publicGroups.stream()
                .filter(group -> group.getUserIds().size() < group.getMaxMembers())
                .collect(Collectors.toList());
            
            log.debug("Found {} groups with available space", availableGroups.size());
            
            // Create user preferences for compatibility scoring
            Map<String, Object> userPreferences = createUserPreferences(request);
            
            // Find compatible groups using compatibility service
            List<PreCheckResponse.CompatibleGroup> compatibleGroups = new ArrayList<>();
            
            for (Group group : availableGroups) {
                double score = tripCompatibilityService.calculatePreCheckCompatibilityScore(userPreferences, group);
                
                log.debug("Compatibility score for group '{}': {}", group.getId(), score);
                
                if (score >= minCompatibilityScore) {
                    PreCheckResponse.CompatibleGroup compatibleGroup = mapToCompatibleGroup(group, score, request);
                    compatibleGroups.add(compatibleGroup);
                }
            }
            
            // Sort by compatibility score descending
            compatibleGroups.sort((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
            
            // Limit to top 10 suggestions
            List<PreCheckResponse.CompatibleGroup> topSuggestions = compatibleGroups.stream()
                .limit(10)
                .collect(Collectors.toList());
            
            // Create response
            PreCheckResponse response = new PreCheckResponse();
            response.setStatus("success");
            response.setSuggestions(topSuggestions);
            response.setTotalSuggestions(topSuggestions.size());
            response.setHasCompatibleGroups(!topSuggestions.isEmpty());
            
            if (topSuggestions.isEmpty()) {
                response.setMessage("No compatible groups found. You can create a new group.");
                log.info("No compatible groups found for user '{}'", request.getUserId());
            } else {
                response.setMessage(String.format("Found %d compatible group(s)", topSuggestions.size()));
                log.info("Found {} compatible groups for user '{}'", topSuggestions.size(), request.getUserId());
            }
            
            // TODO: Publish PreCheckSuggestionsGeneratedEvent via Kafka
            // kafkaTemplate.send("precheck-suggestions", event);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error during pre-check for user '{}': {}", request.getUserId(), e.getMessage(), e);
            
            PreCheckResponse errorResponse = new PreCheckResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Failed to check for compatible groups");
            errorResponse.setSuggestions(new ArrayList<>());
            errorResponse.setTotalSuggestions(0);
            errorResponse.setHasCompatibleGroups(false);
            
            return errorResponse;
        }
    }
    
    /**
     * Creates user preferences map from pre-check request.
     */
    private Map<String, Object> createUserPreferences(PreCheckRequest request) {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("baseCity", request.getBaseCity());
        preferences.put("startDate", request.getStartDate());
        preferences.put("endDate", request.getEndDate());
        preferences.put("budgetLevel", request.getBudgetLevel());
        preferences.put("preferredActivities", request.getPreferredActivities() != null ? request.getPreferredActivities() : new ArrayList<>());
        preferences.put("preferredTerrains", request.getPreferredTerrains() != null ? request.getPreferredTerrains() : new ArrayList<>());
        preferences.put("activityPacing", request.getActivityPacing());
        preferences.put("multiCityAllowed", request.getMultiCityAllowed());
        
        return preferences;
    }
    
    /**
     * Maps a Group entity to CompatibleGroup DTO.
     */
    private PreCheckResponse.CompatibleGroup mapToCompatibleGroup(Group group, double score, PreCheckRequest request) {
        PreCheckResponse.CompatibleGroup compatibleGroup = new PreCheckResponse.CompatibleGroup();
        
        compatibleGroup.setGroupId(group.getId());
        compatibleGroup.setTripName(group.getTripName() != null ? group.getTripName() : "Trip");
        compatibleGroup.setGroupName(group.getGroupName());
        compatibleGroup.setCompatibilityScore(Math.round(score * 100.0) / 100.0); // Round to 2 decimal places
        compatibleGroup.setCurrentMembers(group.getUserIds().size());
        compatibleGroup.setMaxMembers(group.getMaxMembers());
        compatibleGroup.setCreatedBy(group.getCreatedBy());
        
        // Extract preferences
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            compatibleGroup.setBaseCity((String) preferences.get("baseCity"));
            compatibleGroup.setStartDate((String) preferences.get("startDate"));
            compatibleGroup.setEndDate((String) preferences.get("endDate"));
            compatibleGroup.setBudgetLevel((String) preferences.get("budgetLevel"));
            
            // Find common activities and terrains
            List<String> groupActivities = (List<String>) preferences.get("preferredActivities");
            List<String> groupTerrains = (List<String>) preferences.get("preferredTerrains");
            
            if (groupActivities != null && request.getPreferredActivities() != null) {
                List<String> commonActivities = groupActivities.stream()
                    .filter(activity -> request.getPreferredActivities().contains(activity))
                    .collect(Collectors.toList());
                compatibleGroup.setCommonActivities(commonActivities);
            }
            
            if (groupTerrains != null && request.getPreferredTerrains() != null) {
                List<String> commonTerrains = groupTerrains.stream()
                    .filter(terrain -> request.getPreferredTerrains().contains(terrain))
                    .collect(Collectors.toList());
                compatibleGroup.setCommonTerrains(commonTerrains);
            }
        }
        
        return compatibleGroup;
    }
    
    /**
     * Create a new public pooling group.
     */
    public CreatePublicPoolingGroupResponse createPublicPoolingGroup(CreatePublicPoolingGroupRequest request) {
        log.info("Creating public pooling group for user {}", request.getUserId());
        
        try {
            // Validate request
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
                throw new IllegalArgumentException("Group name cannot be null or empty");
            }
            
            // Create a new group
            Group group = new Group();
            group.setCreatedBy(request.getUserId());
            group.setCreatorUserId(request.getUserId());
            group.setCreatorEmail(request.getUserEmail()); // Store creator email for name lookup
            group.setGroupName(request.getGroupName());
            group.setTripName(request.getTripName() != null ? request.getTripName() : request.getGroupName());
            group.setVisibility("public");
            group.setStatus("active");
            group.setMaxMembers(request.getMaxMembers() > 0 ? request.getMaxMembers() : 12);
            group.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : true);
            group.getUserIds().add(request.getUserId());
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Build preferences from request properties
            Map<String, Object> preferences = buildPreferencesFromRequest(request);
            group.setPreferences(preferences);
            
            Group savedGroup = groupRepository.save(group);
            log.info("Successfully created public pooling group '{}' with ID: {}", savedGroup.getGroupName(), savedGroup.getId());
            
            return new CreatePublicPoolingGroupResponse(savedGroup.getId(), "Group created successfully");
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating public pooling group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating public pooling group for user '{}': {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create group: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save a trip with suggestions.
     */
    public SaveTripWithSuggestionsResponse saveTripWithSuggestions(String userId, SaveTripRequest request) {
        log.info("Saving trip with suggestions for user {}", userId);
        
        try {
            // Validate request
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (request.getTripId() == null || request.getTripId().trim().isEmpty()) {
                throw new IllegalArgumentException("Trip ID cannot be null or empty");
            }
            
            // Find groups associated with this trip and user
            List<Group> userGroups = groupRepository.findByUserIdsContaining(userId);
            
            // Filter by trip ID if provided and not empty
            if (request.getTripId() != null && !request.getTripId().trim().isEmpty()) {
                userGroups = userGroups.stream()
                    .filter(group -> request.getTripId().equals(group.getTripId()))
                    .collect(Collectors.toList());
            }
            
            if (userGroups.isEmpty()) {
                log.warn("No groups found for user '{}' and trip '{}'", userId, request.getTripId());
                return new SaveTripWithSuggestionsResponse(request.getTripId(), "No associated groups found for this trip");
            }
            
            // Update the trip name in associated groups if provided
            if (request.getTripName() != null && !request.getTripName().trim().isEmpty()) {
                for (Group group : userGroups) {
                    group.setTripName(request.getTripName());
                    group.setLastUpdated(Instant.now());
                }
                groupRepository.saveAll(userGroups);
                log.info("Updated trip name to '{}' for {} group(s)", request.getTripName(), userGroups.size());
            }
            
            // TODO: In a more advanced implementation, we could:
            // 1. Validate suggestions with the trip service
            // 2. Store group-specific trip customizations
            // 3. Notify other group members of trip updates via Kafka
            // 4. Track group activity and preferences
            
            log.info("Successfully saved trip '{}' with suggestions for user '{}'", request.getTripId(), userId);
            return new SaveTripWithSuggestionsResponse(request.getTripId(), "Trip saved with suggestions successfully");
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for saving trip with suggestions: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error saving trip with suggestions for user '{}' and trip '{}': {}", userId, request.getTripId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save trip with suggestions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Join an existing group.
     */
    public JoinExistingGroupResponse joinExistingGroup(String userId, JoinExistingGroupRequest request) {
        log.info("User {} requesting to join group {}", userId, request.getTargetGroupId());
        
        try {
            // Validate request
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (request.getTargetGroupId() == null || request.getTargetGroupId().trim().isEmpty()) {
                throw new IllegalArgumentException("Target group ID cannot be null or empty");
            }
            
            // Find the target group
            Group targetGroup = groupRepository.findById(request.getTargetGroupId()).orElse(null);
            if (targetGroup == null) {
                log.warn("Group not found: {}", request.getTargetGroupId());
                throw new IllegalArgumentException("Group not found");
            }
            
            // Validate group is public and active
            if (!targetGroup.isPublic()) {
                log.warn("User '{}' attempted to join private group '{}'", userId, request.getTargetGroupId());
                throw new IllegalArgumentException("Cannot join private group");
            }
            
            if (!targetGroup.isActive() && !targetGroup.isFinalized()) {
                log.warn("User '{}' attempted to join inactive group '{}'", userId, request.getTargetGroupId());
                throw new IllegalArgumentException("Group is not accepting new members");
            }
            
            // Check if user is already a member
            if (targetGroup.isMember(userId)) {
                log.info("User '{}' is already a member of group '{}'", userId, request.getTargetGroupId());
                return new JoinExistingGroupResponse(request.getTargetGroupId(), "You are already a member of this group");
            }
            
            // Check if group is full
            if (targetGroup.isFull()) {
                log.warn("User '{}' attempted to join full group '{}'", userId, request.getTargetGroupId());
                throw new IllegalArgumentException("Group is full");
            }
            
            // Check if user has a pending join request
            if (targetGroup.hasPendingJoinRequest(userId)) {
                log.info("User '{}' already has a pending join request for group '{}'", userId, request.getTargetGroupId());
                return new JoinExistingGroupResponse(request.getTargetGroupId(), "You already have a pending join request for this group");
            }
            
            // Handle join based on group approval requirements
            if (targetGroup.isRequiresApproval()) {
                // Create a join request
                JoinRequest joinRequest = new JoinRequest();
                joinRequest.setId(generateJoinRequestId());
                joinRequest.setUserId(userId);
                joinRequest.setStatus("pending");
                joinRequest.setRequestedAt(Instant.now());
                
                targetGroup.addJoinRequest(joinRequest);
                targetGroup.setLastUpdated(Instant.now());
                groupRepository.save(targetGroup);
                
                log.info("Created join request for user '{}' to group '{}'", userId, request.getTargetGroupId());
                
                // TODO: Send notification to group members via Kafka
                // publishJoinRequestEvent(targetGroup, joinRequest);
                
                return new JoinExistingGroupResponse(request.getTargetGroupId(), "Join request submitted successfully. Waiting for approval.");
                
            } else {
                // Direct join - no approval required
                targetGroup.addUser(userId);
                targetGroup.setLastUpdated(Instant.now());
                groupRepository.save(targetGroup);
                
                log.info("User '{}' joined group '{}' directly", userId, request.getTargetGroupId());
                
                // TODO: Send notification to group members via Kafka
                // publishUserJoinedEvent(targetGroup, userId);
                
                return new JoinExistingGroupResponse(request.getTargetGroupId(), "Successfully joined the group");
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for joining group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing join request for user '{}' and group '{}': {}", userId, request.getTargetGroupId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process join request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Finalize a group.
     */
    public FinalizeGroupResponse finalizeGroup(String userId, FinalizeGroupRequest request) {
        log.info("User {} finalizing group {}", userId, request.getGroupId());
        
        try {
            // Validate request
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (request.getGroupId() == null || request.getGroupId().trim().isEmpty()) {
                throw new IllegalArgumentException("Group ID cannot be null or empty");
            }
            
            // Find the group
            Group group = groupRepository.findById(request.getGroupId()).orElse(null);
            if (group == null) {
                log.warn("Group not found: {}", request.getGroupId());
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if user has permission to finalize (must be creator or admin)
            if (!group.isCreator(userId)) {
                log.warn("User '{}' attempted to finalize group '{}' without permission", userId, request.getGroupId());
                throw new IllegalArgumentException("Only the group creator can finalize the group");
            }
            
            // Check if group is already finalized
            if (group.isFinalized()) {
                log.info("Group '{}' is already finalized", request.getGroupId());
                return new FinalizeGroupResponse(request.getGroupId(), "Group is already finalized");
            }
            
            // Validate group has at least one member (should always be true, but safety check)
            if (group.getUserIds().isEmpty()) {
                log.warn("Cannot finalize empty group '{}'", request.getGroupId());
                throw new IllegalArgumentException("Cannot finalize group with no members");
            }
            
            // Finalize the group
            group.finalize(); // This sets status to "finalized" and updates lastUpdated
            groupRepository.save(group);
            
            log.info("Successfully finalized group '{}' with {} members", request.getGroupId(), group.getUserIds().size());
            
            // TODO: Additional finalization tasks:
            // 1. Reject any pending join requests
            // 2. Send notifications to all group members via Kafka
            // 3. Create trip collaboration session if applicable
            // 4. Update group analytics
            
            // Reject pending join requests since group is now finalized
            if (!group.getJoinRequests().isEmpty()) {
                int pendingCount = 0;
                for (JoinRequest joinRequest : group.getJoinRequests()) {
                    if (joinRequest.isPending()) {
                        joinRequest.setStatus("rejected");
                        joinRequest.setRejectionReason("Group has been finalized");
                        joinRequest.setRespondedAt(Instant.now());
                        joinRequest.setReviewedByUserId(userId);
                        pendingCount++;
                    }
                }
                
                if (pendingCount > 0) {
                    groupRepository.save(group);
                    log.info("Rejected {} pending join requests for finalized group '{}'", pendingCount, request.getGroupId());
                }
            }
            
            // TODO: Send finalization event via Kafka
            // publishGroupFinalizedEvent(group, userId);
            
            return new FinalizeGroupResponse(request.getGroupId(), "Group finalized successfully");
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for finalizing group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error finalizing group '{}' for user '{}': {}", request.getGroupId(), userId, e.getMessage(), e);
            throw new RuntimeException("Failed to finalize group: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get compatible groups for a user and trip.
     */
    public List<CompatibleGroupResponse> getCompatibleGroups(String userId, String tripId) {
        log.info("Getting compatible groups for user {} and trip {}", userId, tripId);
        
        try {
            // Validate input parameters
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("User ID is null or empty");
                return new ArrayList<>();
            }
            if (tripId == null || tripId.trim().isEmpty()) {
                log.warn("Trip ID is null or empty");
                return new ArrayList<>();
            }
            
            // Get all public active and finalized groups that are not full
            List<Group> availableGroups = groupRepository.findByVisibilityAndStatus("public", "active")
                .stream()
                .filter(group -> !group.isFull())
                .filter(group -> !group.isMember(userId)) // Exclude groups user is already in
                .collect(Collectors.toList());
            
            // Also include finalized public groups that still have space (for browsing/reference)
            List<Group> finalizedGroups = groupRepository.findByVisibilityAndStatus("public", "finalized")
                .stream()
                .filter(group -> !group.isFull())
                .filter(group -> !group.isMember(userId))
                .collect(Collectors.toList());
            
            availableGroups.addAll(finalizedGroups);
            
            log.debug("Found {} available groups to check compatibility", availableGroups.size());
            
            if (availableGroups.isEmpty()) {
                log.info("No available groups found for compatibility check");
                return new ArrayList<>();
            }
            
            // TODO: In a more advanced implementation, we would:
            // 1. Get trip details from trip service to build user preferences
            // 2. Use TripCompatibilityService to calculate detailed compatibility scores
            // 3. Consider factors like dates, budget, activities, locations, etc.
            
            // For now, create simple compatibility scores based on group preferences
            List<CompatibleGroupResponse> compatibleGroups = new ArrayList<>();
            
            for (Group group : availableGroups) {
                // Calculate basic compatibility score
                double score = calculateBasicCompatibilityScore(group, tripId);
                
                if (score >= minCompatibilityScore) {
                    compatibleGroups.add(new CompatibleGroupResponse(group.getId(), score));
                    log.debug("Group '{}' has compatibility score: {}", group.getId(), score);
                }
            }
            
            // Sort by compatibility score descending
            compatibleGroups.sort((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
            
            // Limit to top 10 results
            List<CompatibleGroupResponse> topCompatibleGroups = compatibleGroups.stream()
                .limit(10)
                .collect(Collectors.toList());
            
            log.info("Found {} compatible groups for user '{}' and trip '{}'", topCompatibleGroups.size(), userId, tripId);
            return topCompatibleGroups;
            
        } catch (Exception e) {
            log.error("Error getting compatible groups for user '{}' and trip '{}': {}", userId, tripId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate basic compatibility score for a group.
     * This is a simplified version - in production, this would use detailed trip data.
     */
    private double calculateBasicCompatibilityScore(Group group, String tripId) {
        double score = 0.5; // Base score
        
        try {
            // Factor in group size (smaller groups might be more compatible)
            if (group.getUserIds().size() <= 4) {
                score += 0.2;
            } else if (group.getUserIds().size() <= 8) {
                score += 0.1;
            }
            
            // Factor in group status (active groups get slight preference)
            if (group.isActive()) {
                score += 0.1;
            }
            
            // Factor in group preferences if available
            Map<String, Object> preferences = group.getPreferences();
            if (preferences != null && !preferences.isEmpty()) {
                score += 0.2; // Groups with defined preferences are more likely to be compatible
            }
            
        } catch (Exception e) {
            log.warn("Error calculating compatibility score for group '{}': {}", group.getId(), e.getMessage());
        }
        
        return Math.min(1.0, score); // Cap at 1.0
    }
    
    /**
     * Generate a unique join request ID.
     */
    private String generateJoinRequestId() {
        return "join_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Build preferences map from CreatePublicPoolingGroupRequest properties.
     */
    private Map<String, Object> buildPreferencesFromRequest(CreatePublicPoolingGroupRequest request) {
        Map<String, Object> preferences = new HashMap<>();
        
        preferences.put("baseCity", request.getBaseCity());
        preferences.put("startDate", request.getStartDate());
        preferences.put("endDate", request.getEndDate());
        preferences.put("arrivalTime", request.getArrivalTime());
        preferences.put("multiCityAllowed", request.getMultiCityAllowed());
        preferences.put("activityPacing", request.getActivityPacing());
        preferences.put("budgetLevel", request.getBudgetLevel());
        preferences.put("preferredTerrains", request.getPreferredTerrains() != null ? request.getPreferredTerrains() : new ArrayList<>());
        preferences.put("preferredActivities", request.getPreferredActivities() != null ? request.getPreferredActivities() : new ArrayList<>());
        
        // Add any additional preferences if provided
        if (request.getAdditionalPreferences() != null) {
            preferences.putAll(request.getAdditionalPreferences());
        }
        
        return preferences;
    }
}
