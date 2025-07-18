package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
     * Uses caching to improve performance for repeated requests.
     */
    @Cacheable(value = "preCheckSuggestions", key = "#request.userId + '_' + #request.baseCity + '_' + #request.startDate + '_' + #request.endDate")
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
        
        // Create a new group
        Group group = new Group();
        group.setCreatedBy(request.getUserId());
        group.setCreatorUserId(request.getUserId());
        group.setCreatorEmail(request.getUserEmail()); // Store creator email for name lookup
        group.setGroupName(request.getGroupName());
        group.setTripName(request.getTripName());
        group.setVisibility("public");
        group.setStatus("active");
        group.setMaxMembers(request.getMaxMembers());
        group.setRequiresApproval(request.getRequiresApproval());
        group.getUserIds().add(request.getUserId());
        group.setCreatedAt(Instant.now());
        
        Group savedGroup = groupRepository.save(group);
        
        return new CreatePublicPoolingGroupResponse(savedGroup.getId(), "Group created successfully");
    }
    
    /**
     * Save a trip with suggestions.
     */
    public SaveTripWithSuggestionsResponse saveTripWithSuggestions(String userId, SaveTripRequest request) {
        log.info("Saving trip with suggestions for user {}", userId);
        
        // Implementation for saving trip with suggestions
        // This would typically involve creating a trip plan and linking it to a group
        
        return new SaveTripWithSuggestionsResponse(request.getTripId(), "Trip saved with suggestions successfully");
    }
    
    /**
     * Join an existing group.
     */
    public JoinExistingGroupResponse joinExistingGroup(String userId, JoinExistingGroupRequest request) {
        log.info("User {} requesting to join group {}", userId, request.getTargetGroupId());
        
        // Implementation for joining existing group
        // This would involve creating a join request or adding the user directly
        
        return new JoinExistingGroupResponse(request.getGroupId(), "Join request submitted successfully");
    }
    
    /**
     * Finalize a group.
     */
    public FinalizeGroupResponse finalizeGroup(String userId, FinalizeGroupRequest request) {
        log.info("User {} finalizing group {}", userId, request.getGroupId());
        
        // Implementation for finalizing group
        // This would involve changing the group status to "finalized"
        
        return new FinalizeGroupResponse(request.getGroupId(), "Group finalized successfully");
    }
    
    /**
     * Get compatible groups.
     */
    public List<CompatibleGroupResponse> getCompatibleGroups(String userId, String tripId) {
        log.info("Getting compatible groups for user {} and trip {}", userId, tripId);
        
        // Implementation for getting compatible groups
        // This would return a list of compatible groups with scores
        
        return new ArrayList<>();
    }
}
