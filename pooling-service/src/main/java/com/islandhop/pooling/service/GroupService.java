package com.islandhop.pooling.service;

import com.islandhop.pooling.client.ItineraryServiceClient;
import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.event.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.GroupAction;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing groups and their operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final ItineraryServiceClient itineraryServiceClient;
    private final EventPublisher eventPublisher;
    
    /**
     * Create a new group.
     */
    @Transactional
    public CreateGroupResponse createGroup(CreateGroupRequest request, String userId) {
        log.info("Creating group '{}' for user {}", request.getGroupName(), userId);
        
        try {
            // Generate group ID
            String groupId = UUID.randomUUID().toString();
            
            // Handle trip creation or validation
            String tripId = request.getTripId();
            if (tripId == null || tripId.trim().isEmpty()) {
                // Create new trip
                Map<String, Object> tripData = createDefaultTripData(request.getGroupName(), userId);
                Map<String, Object> tripResponse = itineraryServiceClient.createTripPlan(userId, tripData)
                    .block();
                tripId = (String) tripResponse.get("tripId");
                log.info("Created new trip {} for group {}", tripId, groupId);
            } else {
                // Validate existing trip
                itineraryServiceClient.getTripPlan(tripId, userId).block();
                log.info("Validated existing trip {} for group {}", tripId, groupId);
            }
            
            // Create group
            Group group = new Group();
            group.setGroupId(groupId);
            group.setGroupName(request.getGroupName());
            group.setTripId(tripId);
            group.setVisibility(request.getVisibility());
            group.setPreferences(request.getPreferences());
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Add creator as first member
            group.addUser(userId);
            
            // Add creation action
            group.getActions().add(GroupAction.create(userId, GroupAction.ActionType.CREATE_GROUP, 
                "Created group '" + request.getGroupName() + "'"));
            
            // Save group
            groupRepository.save(group);
            
            // Publish event for public groups
            if (group.isPublic()) {
                eventPublisher.publishPublicGroupCreatedEvent(
                    PublicGroupCreatedEvent.create(groupId, request.getGroupName(), tripId, userId, request.getPreferences()));
            }
            
            log.info("Successfully created group {} for user {}", groupId, userId);
            return new CreateGroupResponse("success", groupId, request.getGroupName(), tripId, "Group created successfully");
            
        } catch (Exception e) {
            log.error("Failed to create group for user {}: {}", userId, e.getMessage());
            throw new GroupCreationException("Failed to create group: " + e.getMessage());
        }
    }
    
    /**
     * Invite a user to a private group.
     */
    @Transactional
    public InviteUserResponse inviteUser(String groupId, InviteUserRequest request, String userId) {
        log.info("Inviting user {} to group {} by user {}", request.getInvitedUserId(), groupId, userId);
        
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
        
        if (!group.isCreator(userId)) {
            throw new UnauthorizedGroupAccessException("Only the group creator can invite users");
        }
        
        if (!group.isPrivate()) {
            throw new InvalidGroupOperationException("Can only invite users to private groups");
        }
        
        if (group.isMember(request.getInvitedUserId())) {
            throw new InvalidGroupOperationException("User is already a member of this group");
        }
        
        // Add user to group
        group.addUser(request.getInvitedUserId());
        group.getActions().add(GroupAction.create(userId, GroupAction.ActionType.INVITE_USER, 
            "Invited user " + request.getInvitedUserId()));
        group.setLastUpdated(Instant.now());
        
        groupRepository.save(group);
        
        // Publish event
        eventPublisher.publishUserInvitedEvent(
            UserInvitedEvent.create(groupId, group.getGroupName(), request.getInvitedUserId(), userId));
        
        log.info("Successfully invited user {} to group {}", request.getInvitedUserId(), groupId);
        return new InviteUserResponse("success", groupId, "User invited successfully");
    }
    
    /**
     * Add a place to group itinerary.
     */
    @Transactional
    public GroupItineraryResponse addPlaceToGroupItinerary(String groupId, int day, String type, 
                                                          SuggestionResponse place, String userId) {
        log.info("Adding place to group {} itinerary day {} type {} by user {}", groupId, day, type, userId);
        
        Group group = validateGroupAccess(groupId, userId);
        
        // Validate day and type
        if (day < 1 || day > 30) {
            throw new InvalidDayException("Day must be between 1 and 30");
        }
        
        if (!Arrays.asList("attractions", "hotels", "restaurants").contains(type)) {
            throw new InvalidTypeException("Type must be one of: attractions, hotels, restaurants");
        }
        
        try {
            // Add place via itinerary service
            itineraryServiceClient.addPlaceToTrip(group.getTripId(), day, type, place, userId).block();
            
            // Log action
            group.getActions().add(GroupAction.create(userId, GroupAction.ActionType.ADD_PLACE, 
                "Added " + place.getName() + " to Day " + day + " " + type));
            group.setLastUpdated(Instant.now());
            
            groupRepository.save(group);
            
            // Publish event
            eventPublisher.publishGroupItineraryUpdatedEvent(
                GroupItineraryUpdatedEvent.create(groupId, group.getTripId(), userId, "ADD_PLACE", day, type, place.getName()));
            
            log.info("Successfully added place to group {} itinerary", groupId);
            return new GroupItineraryResponse("success", groupId, group.getTripId(), "Place added to group itinerary");
            
        } catch (Exception e) {
            log.error("Failed to add place to group {} itinerary: {}", groupId, e.getMessage());
            throw new GroupItineraryException("Failed to add place to group itinerary: " + e.getMessage());
        }
    }
    
    /**
     * Update city in group itinerary.
     */
    @Transactional
    public GroupItineraryResponse updateCityInGroupItinerary(String groupId, int day, 
                                                           UpdateCityRequest request, String userId) {
        log.info("Updating city for group {} itinerary day {} to {} by user {}", groupId, day, request.getCity(), userId);
        
        Group group = validateGroupAccess(groupId, userId);
        
        // Validate day
        if (day < 1 || day > 30) {
            throw new InvalidDayException("Day must be between 1 and 30");
        }
        
        try {
            // Update city via itinerary service
            itineraryServiceClient.updateCityForDay(group.getTripId(), day, request.getCity(), userId).block();
            
            // Log action
            group.getActions().add(GroupAction.create(userId, GroupAction.ActionType.UPDATE_CITY, 
                "Updated city for Day " + day + " to " + request.getCity()));
            group.setLastUpdated(Instant.now());
            
            groupRepository.save(group);
            
            // Publish event
            eventPublisher.publishGroupItineraryUpdatedEvent(
                GroupItineraryUpdatedEvent.create(groupId, group.getTripId(), userId, "UPDATE_CITY", day, null, request.getCity()));
            
            log.info("Successfully updated city for group {} itinerary", groupId);
            return new GroupItineraryResponse("success", groupId, group.getTripId(), "City updated successfully");
            
        } catch (Exception e) {
            log.error("Failed to update city for group {} itinerary: {}", groupId, e.getMessage());
            throw new GroupItineraryException("Failed to update city for group itinerary: " + e.getMessage());
        }
    }
    
    /**
     * Get group details.
     */
    public GroupDetailsResponse getGroupDetails(String groupId, String userId) {
        log.info("Retrieving details for group {} by user {}", groupId, userId);
        
        Group group = validateGroupAccess(groupId, userId);
        
        GroupDetailsResponse response = new GroupDetailsResponse();
        response.setStatus("success");
        response.setGroupId(group.getGroupId());
        response.setGroupName(group.getGroupName());
        response.setTripId(group.getTripId());
        response.setUserIds(group.getUserIds());
        response.setVisibility(group.getVisibility());
        response.setPreferences(group.getPreferences());
        response.setActions(group.getActions());
        response.setMessage("Group details retrieved successfully");
        
        // Include join requests only for creators
        if (group.isCreator(userId)) {
            response.setJoinRequests(group.getJoinRequests());
        }
        
        log.info("Successfully retrieved details for group {}", groupId);
        return response;
    }
    
    /**
     * List public groups.
     */
    public List<PublicGroupResponse> listPublicGroups(String userId, Map<String, String> filters) {
        log.info("Listing public groups for user {} with filters {}", userId, filters);
        
        List<Group> publicGroups = groupRepository.findPublicGroups();
        
        // Get trip summaries for all public groups
        List<PublicGroupResponse> responses = publicGroups.stream()
            .map(group -> {
                try {
                    Map<String, Object> tripSummary = itineraryServiceClient.getTripSummary(group.getTripId()).block();
                    
                    PublicGroupResponse response = new PublicGroupResponse();
                    response.setGroupId(group.getGroupId());
                    response.setGroupName(group.getGroupName());
                    response.setTripId(group.getTripId());
                    response.setDestination((String) tripSummary.get("destination"));
                    response.setStartDate((String) tripSummary.get("startDate"));
                    response.setEndDate((String) tripSummary.get("endDate"));
                    response.setPreferences(group.getPreferences());
                    response.setMessage("Public groups retrieved successfully");
                    
                    return response;
                } catch (Exception e) {
                    log.warn("Failed to get trip summary for group {}: {}", group.getGroupId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Apply filters if provided
        if (filters != null && !filters.isEmpty()) {
            responses = applyFilters(responses, filters);
        }
        
        log.info("Retrieved {} public groups", responses.size());
        return responses;
    }
    
    /**
     * Request to join a public group.
     */
    @Transactional
    public JoinGroupResponse requestToJoinGroup(String groupId, JoinGroupRequest request, String userId) {
        log.info("User {} requesting to join group {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
        
        if (!group.isPublic()) {
            throw new InvalidGroupOperationException("Can only request to join public groups");
        }
        
        if (group.isMember(userId)) {
            throw new InvalidGroupOperationException("User is already a member of this group");
        }
        
        // Check if user already has a pending request
        boolean hasPendingRequest = group.getJoinRequests().stream()
            .anyMatch(jr -> jr.getUserId().equals(userId) && jr.isPending());
        
        if (hasPendingRequest) {
            throw new InvalidGroupOperationException("User already has a pending join request");
        }
        
        // Create join request
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setUserId(userId);
        joinRequest.setUserProfile(request.getUserProfile());
        joinRequest.setStatus("pending");
        joinRequest.setRequestedAt(Instant.now().toString());
        
        group.getJoinRequests().add(joinRequest);
        group.setLastUpdated(Instant.now());
        
        groupRepository.save(group);
        
        // Publish event
        eventPublisher.publishJoinRequestEvent(
            JoinRequestEvent.create(groupId, group.getGroupName(), userId, group.getCreatorUserId(), request.getUserProfile()));
        
        log.info("Successfully submitted join request for group {} by user {}", groupId, userId);
        return new JoinGroupResponse("success", groupId, "Join request sent");
    }
    
    /**
     * Approve or reject join request.
     */
    @Transactional
    public JoinRequestDecisionResponse processJoinRequest(String groupId, String joinerUserId, 
                                                        JoinRequestDecisionRequest request, String creatorUserId) {
        log.info("Processing join request for group {} joiner {} by creator {} with decision {}", 
                groupId, joinerUserId, creatorUserId, request.getStatus());
        
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
        
        if (!group.isCreator(creatorUserId)) {
            throw new UnauthorizedGroupAccessException("Only the group creator can approve/reject join requests");
        }
        
        // Find join request
        JoinRequest joinRequest = group.getJoinRequests().stream()
            .filter(jr -> jr.getUserId().equals(joinerUserId) && jr.isPending())
            .findFirst()
            .orElseThrow(() -> new JoinRequestNotFoundException("Join request not found"));
        
        // Update join request status
        if ("approved".equals(request.getStatus())) {
            joinRequest.approve();
            group.addUser(joinerUserId);
            group.getActions().add(GroupAction.create(creatorUserId, GroupAction.ActionType.APPROVE_JOIN_REQUEST, 
                "Approved join request from user " + joinerUserId));
            
            // Publish approved event
            eventPublisher.publishJoinApprovedEvent(
                JoinApprovedEvent.create(groupId, group.getGroupName(), joinerUserId, creatorUserId));
            
        } else if ("rejected".equals(request.getStatus())) {
            joinRequest.reject();
            group.getActions().add(GroupAction.create(creatorUserId, GroupAction.ActionType.REJECT_JOIN_REQUEST, 
                "Rejected join request from user " + joinerUserId));
            
            // Publish rejected event
            eventPublisher.publishJoinRejectedEvent(
                JoinRejectedEvent.create(groupId, group.getGroupName(), joinerUserId, creatorUserId));
        }
        
        group.setLastUpdated(Instant.now());
        groupRepository.save(group);
        
        String message = "approved".equals(request.getStatus()) ? "Join request approved" : "Join request rejected";
        log.info("Successfully processed join request for group {} with decision {}", groupId, request.getStatus());
        return new JoinRequestDecisionResponse("success", groupId, message);
    }
    
    /**
     * Get scored trip suggestions.
     */
    public List<TripSuggestionResponse> getScoredTripSuggestions(TripSuggestionsRequest request, String userId) {
        log.info("Generating trip suggestions for user {} with travel dates {}-{}", 
                userId, request.getStartDate(), request.getEndDate());
        
        List<Group> publicGroups = groupRepository.findPublicGroups();
        
        List<TripSuggestionResponse> suggestions = new ArrayList<>();
        
        for (Group group : publicGroups) {
            try {
                // Get trip details
                Map<String, Object> tripSummary = itineraryServiceClient.getTripSummary(group.getTripId()).block();
                
                // Calculate compatibility score
                int score = calculateCompatibilityScore(request, group, tripSummary);
                
                if (score > 0) {
                    TripSuggestionResponse suggestion = new TripSuggestionResponse();
                    suggestion.setGroupId(group.getGroupId());
                    suggestion.setGroupName(group.getGroupName());
                    suggestion.setTripId(group.getTripId());
                    suggestion.setDestination((String) tripSummary.get("destination"));
                    suggestion.setStartDate((String) tripSummary.get("startDate"));
                    suggestion.setEndDate((String) tripSummary.get("endDate"));
                    suggestion.setPreferences(group.getPreferences());
                    suggestion.setScore(score);
                    suggestion.setMessage("Suggested trips retrieved successfully");
                    
                    suggestions.add(suggestion);
                }
            } catch (Exception e) {
                log.warn("Failed to process group {} for suggestions: {}", group.getGroupId(), e.getMessage());
            }
        }
        
        // Sort by score (descending) and return top 10
        suggestions.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        List<TripSuggestionResponse> topSuggestions = suggestions.stream()
            .limit(10)
            .collect(Collectors.toList());
        
        // Publish event
        eventPublisher.publishTripSuggestionsGeneratedEvent(
            TripSuggestionsGeneratedEvent.create(userId, topSuggestions.size()));
        
        log.info("Generated {} trip suggestions for user {}", topSuggestions.size(), userId);
        return topSuggestions;
    }
    
    // Helper methods
    
    private Group validateGroupAccess(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
        
        if (!group.isMember(userId)) {
            throw new UnauthorizedGroupAccessException("User is not a member of this group");
        }
        
        return group;
    }
    
    private Map<String, Object> createDefaultTripData(String groupName, String userId) {
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("tripName", groupName);
        tripData.put("startDate", LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE));
        tripData.put("endDate", LocalDate.now().plusDays(37).format(DateTimeFormatter.ISO_LOCAL_DATE));
        tripData.put("baseCity", "Colombo");
        tripData.put("multiCityAllowed", true);
        tripData.put("activityPacing", "Normal");
        tripData.put("budgetLevel", "Medium");
        tripData.put("preferredTerrains", List.of());
        tripData.put("preferredActivities", List.of());
        return tripData;
    }
    
    private List<PublicGroupResponse> applyFilters(List<PublicGroupResponse> responses, Map<String, String> filters) {
        return responses.stream()
            .filter(response -> {
                if (filters.containsKey("destination")) {
                    String destination = filters.get("destination");
                    return response.getDestination() != null && 
                           response.getDestination().toLowerCase().contains(destination.toLowerCase());
                }
                return true;
            })
            .filter(response -> {
                if (filters.containsKey("startDate")) {
                    String filterStartDate = filters.get("startDate");
                    return response.getStartDate() != null && 
                           response.getStartDate().compareTo(filterStartDate) >= 0;
                }
                return true;
            })
            .filter(response -> {
                if (filters.containsKey("endDate")) {
                    String filterEndDate = filters.get("endDate");
                    return response.getEndDate() != null && 
                           response.getEndDate().compareTo(filterEndDate) <= 0;
                }
                return true;
            })
            .collect(Collectors.toList());
    }
    
    private int calculateCompatibilityScore(TripSuggestionsRequest request, Group group, Map<String, Object> tripSummary) {
        int score = 0;
        
        // Date overlap score (40%)
        score += calculateDateOverlapScore(request, tripSummary) * 40 / 100;
        
        // Preference match score (60%)
        score += calculatePreferenceMatchScore(request, group, tripSummary) * 60 / 100;
        
        return score;
    }
    
    private int calculateDateOverlapScore(TripSuggestionsRequest request, Map<String, Object> tripSummary) {
        try {
            LocalDate userStartDate = LocalDate.parse(request.getStartDate());
            LocalDate userEndDate = LocalDate.parse(request.getEndDate());
            LocalDate tripStartDate = LocalDate.parse((String) tripSummary.get("startDate"));
            LocalDate tripEndDate = LocalDate.parse((String) tripSummary.get("endDate"));
            
            LocalDate overlapStart = userStartDate.isAfter(tripStartDate) ? userStartDate : tripStartDate;
            LocalDate overlapEnd = userEndDate.isBefore(tripEndDate) ? userEndDate : tripEndDate;
            
            if (overlapStart.isAfter(overlapEnd)) {
                return 0; // No overlap
            }
            
            long overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            long userTripDays = ChronoUnit.DAYS.between(userStartDate, userEndDate) + 1;
            
            return (int) ((overlapDays * 100) / userTripDays);
        } catch (Exception e) {
            log.warn("Error calculating date overlap score: {}", e.getMessage());
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    private int calculatePreferenceMatchScore(TripSuggestionsRequest request, Group group, Map<String, Object> tripSummary) {
        int score = 0;
        
        // Interests match (30%)
        if (request.getInterests() != null && group.getPreferences() != null) {
            List<String> groupInterests = (List<String>) group.getPreferences().get("interests");
            if (groupInterests != null) {
                long matchingInterests = request.getInterests().stream()
                    .filter(groupInterests::contains)
                    .count();
                score += (matchingInterests * 30) / request.getInterests().size();
            }
        }
        
        // Language match (20%)
        if (request.getLanguage() != null && group.getPreferences() != null) {
            List<String> groupLanguages = (List<String>) group.getPreferences().get("language");
            if (groupLanguages != null) {
                long matchingLanguages = request.getLanguage().stream()
                    .filter(groupLanguages::contains)
                    .count();
                score += (matchingLanguages * 20) / request.getLanguage().size();
            }
        }
        
        // Budget level match (10%)
        if (request.getBudgetLevel() != null && group.getPreferences() != null) {
            String groupBudgetLevel = (String) group.getPreferences().get("budgetLevel");
            if (request.getBudgetLevel().equals(groupBudgetLevel)) {
                score += 10;
            }
        }
        
        return score;
    }
}
