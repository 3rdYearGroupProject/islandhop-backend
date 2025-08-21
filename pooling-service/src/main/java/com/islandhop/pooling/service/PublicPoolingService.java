package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.model.Invitation;
import com.islandhop.pooling.repository.GroupRepository;
import com.islandhop.pooling.exception.GroupNotFoundException;
import com.islandhop.pooling.exception.TripNotFoundException;
import com.islandhop.pooling.exception.UnauthorizedTripAccessException;
import com.islandhop.pooling.client.ItineraryServiceClient;
import com.islandhop.pooling.client.UserServiceClient;
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
    private final ItineraryServiceClient itineraryServiceClient;
    private final UserServiceClient userServiceClient;
    
    @Value("${pooling.compatibility.min-score:0.1}")
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
            
            // Return ALL compatible groups instead of limiting to top 10
            List<PreCheckResponse.CompatibleGroup> allSuggestions = new ArrayList<>(compatibleGroups);
            
            // Create response
            PreCheckResponse response = new PreCheckResponse();
            response.setStatus("success");
            response.setSuggestions(allSuggestions);
            response.setTotalSuggestions(allSuggestions.size());
            response.setHasCompatibleGroups(!allSuggestions.isEmpty());
            
            if (allSuggestions.isEmpty()) {
                response.setMessage("No compatible groups found. You can create a new group.");
                log.info("No compatible groups found for user '{}'", request.getUserId());
            } else {
                response.setMessage(String.format("Found %d compatible group(s)", allSuggestions.size()));
                log.info("Found {} compatible groups for user '{}'", allSuggestions.size(), request.getUserId());
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
     * Save a trip with suggestions for similar existing trips.
     * Finds compatible groups with similar destinations, activities, and terrains.
     */
    public SaveTripWithSuggestionsResponse saveTripWithSuggestions(String groupId, SaveTripRequest request) {
        log.info("Saving trip with suggestions for group {} by user {}", groupId, request.getUserId());
        
        try {
            // First, update the current group with the trip data
            Optional<Group> currentGroupOpt = groupRepository.findById(groupId);
            if (currentGroupOpt.isEmpty()) {
                throw new GroupNotFoundException("Group not found: " + groupId);
            }
            
            Group currentGroup = currentGroupOpt.get();
            
            // Save trip data to the current group
            Map<String, Object> tripPreferences = new HashMap<>();
            if (request.getTripData() != null) {
                tripPreferences.put("tripName", request.getTripData().getName());
                tripPreferences.put("startDate", request.getTripData().getStartDate());
                tripPreferences.put("endDate", request.getTripData().getEndDate());
                
                // Convert destinations to the expected format
                if (request.getTripData().getDestinations() != null) {
                    List<Map<String, String>> destinationMaps = request.getTripData().getDestinations().stream()
                            .map(dest -> Map.of("name", dest.getName()))
                            .collect(Collectors.toList());
                    tripPreferences.put("destinations", destinationMaps);
                }
                
                tripPreferences.put("terrains", request.getTripData().getTerrains());
                tripPreferences.put("activities", request.getTripData().getActivities());
                tripPreferences.put("itinerary", request.getTripData().getItinerary());
            }
            
            currentGroup.setPreferences(tripPreferences);
            currentGroup.setTripName(request.getTripData().getName());
            groupRepository.save(currentGroup);
            
            // Find similar existing public groups
            List<Group> publicGroups = groupRepository.findByVisibilityAndStatus("public", "finalized");
            
            List<SaveTripWithSuggestionsResponse.SimilarTrip> similarTrips = new ArrayList<>();
            
            for (Group group : publicGroups) {
                if (group.getId().equals(groupId)) continue; // Skip current group
                if (group.getUserIds().size() >= group.getMaxMembers()) continue; // Skip full groups
                
                Map<String, Object> groupPreferences = group.getPreferences();
                if (groupPreferences == null) continue;
                
                // Calculate similarity score
                double similarityScore = calculateTripSimilarity(tripPreferences, groupPreferences);
                
                if (similarityScore >= minCompatibilityScore) {
                    SaveTripWithSuggestionsResponse.SimilarTrip similarTrip = mapToSimilarTrip(group, similarityScore);
                    similarTrips.add(similarTrip);
                }
            }
            
            // Sort by similarity score descending
            similarTrips.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
            
            // Return ALL similar trips instead of limiting to top 5
            List<SaveTripWithSuggestionsResponse.SimilarTrip> allSuggestions = new ArrayList<>(similarTrips);
            
            SaveTripWithSuggestionsResponse response = new SaveTripWithSuggestionsResponse();
            response.setTripId(request.getTripId());
            response.setGroupId(groupId);
            response.setSimilarTrips(allSuggestions);
            response.setTotalSuggestions(allSuggestions.size());
            
            if (allSuggestions.isEmpty()) {
                response.setMessage("Trip saved successfully. No similar trips found.");
                response.setHasSimilarTrips(false);
            } else {
                response.setMessage(String.format("Trip saved successfully. Found %d similar trips.", allSuggestions.size()));
                response.setHasSimilarTrips(true);
            }
            
            log.info("Trip saved for group {} with {} similar trip suggestions", groupId, allSuggestions.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error saving trip with suggestions for group {}: {}", groupId, e.getMessage(), e);
            throw new RuntimeException("Failed to save trip with suggestions: " + e.getMessage());
        }
    }
    
    /**
     * Calculate similarity between two trips based on destinations, activities, and terrains.
     */
    private double calculateTripSimilarity(Map<String, Object> trip1, Map<String, Object> trip2) {
        double totalScore = 0.0;
        int factors = 0;
        
        // Compare destinations (40% weight)
        Object destinations1Obj = trip1.get("destinations");
        Object destinations2Obj = trip2.get("destinations");
        
        if (destinations1Obj != null && destinations2Obj != null) {
            Set<String> destNames1 = extractDestinationNames(destinations1Obj);
            Set<String> destNames2 = extractDestinationNames(destinations2Obj);
            
            double destinationSimilarity = calculateSetSimilarity(destNames1, destNames2);
            totalScore += destinationSimilarity * 0.4;
            factors++;
        }
        
        // Compare activities (30% weight)
        List<String> activities1 = (List<String>) trip1.get("activities");
        List<String> activities2 = (List<String>) trip2.get("activities");
        
        if (activities1 != null && activities2 != null) {
            Set<String> activitySet1 = new HashSet<>(activities1);
            Set<String> activitySet2 = new HashSet<>(activities2);
            
            double activitySimilarity = calculateSetSimilarity(activitySet1, activitySet2);
            totalScore += activitySimilarity * 0.3;
            factors++;
        }
        
        // Compare terrains (30% weight)
        List<String> terrains1 = (List<String>) trip1.get("terrains");
        List<String> terrains2 = (List<String>) trip2.get("terrains");
        
        if (terrains1 != null && terrains2 != null) {
            Set<String> terrainSet1 = new HashSet<>(terrains1);
            Set<String> terrainSet2 = new HashSet<>(terrains2);
            
            double terrainSimilarity = calculateSetSimilarity(terrainSet1, terrainSet2);
            totalScore += terrainSimilarity * 0.3;
            factors++;
        }
        
        return factors > 0 ? totalScore : 0.0;
    }
    
    /**
     * Calculate Jaccard similarity between two sets.
     */
    private double calculateSetSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Extract destination names from either List<Map<String, String>> or List<Destination> format.
     */
    private Set<String> extractDestinationNames(Object destinationsObj) {
        Set<String> names = new HashSet<>();
        
        if (destinationsObj instanceof List<?> destinations) {
            log.debug("Processing destinations list with {} items", destinations.size());
            for (Object dest : destinations) {
                if (dest instanceof Map<?, ?> destMap) {
                    // Handle Map format: {"name": "Colombo"}
                    Object name = destMap.get("name");
                    if (name instanceof String) {
                        names.add((String) name);
                        log.debug("Added destination from Map format: {}", name);
                    }
                } else if (dest instanceof SaveTripRequest.TripData.Destination destObj) {
                    // Handle Destination object format
                    if (destObj.getName() != null) {
                        names.add(destObj.getName());
                        log.debug("Added destination from Destination object: {}", destObj.getName());
                    }
                } else {
                    log.warn("Unknown destination format: {} (type: {})", dest, dest.getClass().getSimpleName());
                }
            }
        } else if (destinationsObj != null) {
            log.warn("Destinations object is not a List. Type: {}, Value: {}", 
                    destinationsObj.getClass().getSimpleName(), destinationsObj);
        }
        
        log.debug("Extracted {} destination names: {}", names.size(), names);
        return names;
    }
    
    /**
     * Map Group to SimilarTrip DTO.
     */
    private SaveTripWithSuggestionsResponse.SimilarTrip mapToSimilarTrip(Group group, double similarityScore) {
        SaveTripWithSuggestionsResponse.SimilarTrip similarTrip = new SaveTripWithSuggestionsResponse.SimilarTrip();
        
        similarTrip.setGroupId(group.getId());
        similarTrip.setTripName(group.getTripName());
        similarTrip.setGroupName(group.getGroupName());
        similarTrip.setSimilarityScore(Math.round(similarityScore * 100.0) / 100.0);
        similarTrip.setCurrentMembers(group.getUserIds().size());
        similarTrip.setMaxMembers(group.getMaxMembers());
        similarTrip.setCreatedBy(group.getCreatedBy());
        
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            similarTrip.setStartDate((String) preferences.get("startDate"));
            similarTrip.setEndDate((String) preferences.get("endDate"));
            similarTrip.setDestinations((List<Map<String, String>>) preferences.get("destinations"));
            similarTrip.setActivities((List<String>) preferences.get("activities"));
            similarTrip.setTerrains((List<String>) preferences.get("terrains"));
        }
        
        return similarTrip;
    }
    
    /**
     * Get comprehensive trip details including itinerary and joined group members.
     * This method is publicly accessible for both logged-in and anonymous users.
     *
     * @param tripId The ID of the trip
     * @param userId The ID of the user making the request (optional)
     * @return ComprehensiveTripResponse with trip and member information
     */
    public ComprehensiveTripResponse getComprehensiveTripDetails(String tripId, String userId) {
        log.info("Fetching comprehensive trip details for trip {} requested by user {}", tripId, userId != null ? userId : "anonymous");
        
        try {
            // 1. Find associated group first to determine which userId to use for trip service
            Optional<Group> groupOpt = groupRepository.findFirstByTripId(tripId);
            Group group = groupOpt.orElse(null);
            
            // 2. Determine which userId to use for trip service call
            String effectiveUserId = userId;
            if (group != null && group.isPublic()) {
                // For public groups, use the group owner's userId to fetch trip details
                effectiveUserId = group.getCreatorUserId();
                log.info("Using group owner's userId {} for public group trip {}", effectiveUserId, tripId);
            } else if (userId == null) {
                // For private groups or no group, userId is required
                log.warn("No userId provided for non-public trip: {}", tripId);
                throw new IllegalArgumentException("User ID is required for this trip");
            }
            
            // 3. Get trip data from itinerary service
            Map<String, Object> tripData = itineraryServiceClient.getTripPlan(tripId, effectiveUserId)
                .doOnError(error -> log.warn("Failed to fetch trip data from itinerary service: {}", error.getMessage()))
                .onErrorReturn(new HashMap<>()) // Return empty map if trip service fails
                .block();
            
            if (tripData == null || tripData.isEmpty()) {
                log.warn("No trip data found for tripId: {}", tripId);
                throw new TripNotFoundException("Trip not found with ID: " + tripId);
            }
            
            // 4. Build comprehensive response
            ComprehensiveTripResponse response = ComprehensiveTripResponse.builder()
                .tripDetails(buildTripDetails(tripData))
                .groupInfo(group != null ? buildGroupInfo(group) : null)
                .members(group != null ? buildMemberSummaries(group) : List.of())
                .status("success")
                .message("Comprehensive trip details retrieved successfully")
                .fetchedAt(Instant.now())
                .build();
            
            log.info("Successfully built comprehensive trip response for trip {} with {} members", 
                    tripId, group != null ? group.getUserIds().size() : 0);
            
            return response;
            
        } catch (TripNotFoundException e) {
            // Re-throw these specific exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching comprehensive trip details for trip {}: {}", tripId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch comprehensive trip details: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build trip details from itinerary service response.
     */
    private ComprehensiveTripResponse.TripDetails buildTripDetails(Map<String, Object> tripData) {
        // Extract daily plans
        List<Map<String, Object>> dailyPlansData = (List<Map<String, Object>>) tripData.get("dailyPlans");
        List<ComprehensiveTripResponse.DailyPlanSummary> dailyPlans = dailyPlansData != null ? 
            dailyPlansData.stream()
                .map(this::convertToDailyPlanSummary)
                .collect(Collectors.toList()) : new ArrayList<>();
        
        return ComprehensiveTripResponse.TripDetails.builder()
            .tripId((String) tripData.get("tripId"))
            .tripName((String) tripData.get("tripName"))
            .startDate((String) tripData.get("startDate"))
            .endDate((String) tripData.get("endDate"))
            .baseCity((String) tripData.get("baseCity"))
            .budgetLevel((String) tripData.getOrDefault("budgetLevel", "Medium"))
            .activityPacing((String) tripData.getOrDefault("activityPacing", "Normal"))
            .preferredActivities((List<String>) tripData.getOrDefault("preferredActivities", new ArrayList<>()))
            .preferredTerrains((List<String>) tripData.getOrDefault("preferredTerrains", new ArrayList<>()))
            .multiCityAllowed((Boolean) tripData.getOrDefault("multiCityAllowed", true))
            .dailyPlans(dailyPlans)
            .createdAt(parseInstant((String) tripData.get("createdAt")))
            .lastUpdated(parseInstant((String) tripData.get("lastUpdated")))
            .build();
    }
    
    /**
     * Convert daily plan data to summary format.
     */
    private ComprehensiveTripResponse.DailyPlanSummary convertToDailyPlanSummary(Map<String, Object> dailyPlan) {
        List<Map<String, Object>> attractions = (List<Map<String, Object>>) dailyPlan.getOrDefault("attractions", new ArrayList<>());
        List<Map<String, Object>> hotels = (List<Map<String, Object>>) dailyPlan.getOrDefault("hotels", new ArrayList<>());
        List<Map<String, Object>> restaurants = (List<Map<String, Object>>) dailyPlan.getOrDefault("restaurants", new ArrayList<>());
        
        return ComprehensiveTripResponse.DailyPlanSummary.builder()
            .day((Integer) dailyPlan.get("day"))
            .city((String) dailyPlan.get("city"))
            .userSelected((Boolean) dailyPlan.getOrDefault("userSelected", false))
            .attractionsCount(attractions.size())
            .hotelsCount(hotels.size())
            .restaurantsCount(restaurants.size())
            .attractions(convertToPlaceSummaries(attractions))
            .hotels(convertToPlaceSummaries(hotels))
            .restaurants(convertToPlaceSummaries(restaurants))
            .build();
    }
    
    /**
     * Convert place data to place summaries.
     */
    private List<ComprehensiveTripResponse.PlaceSummary> convertToPlaceSummaries(List<Map<String, Object>> places) {
        return places.stream()
            .map(place -> ComprehensiveTripResponse.PlaceSummary.builder()
                .name((String) place.get("name"))
                .category((String) place.get("category"))
                .rating(parseDouble(place.get("rating")))
                .address((String) place.get("address"))
                .userSelected((Boolean) place.getOrDefault("userSelected", false))
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * Build group information.
     */
    private ComprehensiveTripResponse.GroupInfo buildGroupInfo(Group group) {
        return ComprehensiveTripResponse.GroupInfo.builder()
            .groupId(group.getId())
            .groupName(group.getGroupName())
            .visibility(group.getVisibility())
            .status(group.getStatus())
            .groupLeader(group.getCreatedBy())
            .currentMembers(group.getUserIds().size())
            .maxMembers(group.getMaxMembers())
            .availableSlots(group.getMaxMembers() - group.getUserIds().size())
            .requiresApproval(group.isRequiresApproval())
            .createdAt(group.getCreatedAt())
            .lastUpdated(group.getLastUpdated())
            .build();
    }
    
    /**
     * Build member summaries with basic information.
     */
    private List<ComprehensiveTripResponse.MemberSummary> buildMemberSummaries(Group group) {
        return group.getUserIds().stream()
            .map(userId -> {
                boolean isLeader = userId.equals(group.getCreatedBy());
                
                // Fetch real user details from user service
                UserServiceClient.UserProfile userProfile = userServiceClient.getUserByUid(userId);
                String userName = userProfile != null ? userProfile.getFullName() : userId;
                String userEmail = userProfile != null ? userProfile.getEmail() : userId + "@example.com";
                
                return ComprehensiveTripResponse.MemberSummary.builder()
                    .userId(userId)
                    .name(userName)
                    .email(userEmail)
                    .role(isLeader ? "leader" : "member")
                    .joinedAt(group.getCreatedAt()) // Simplified - would track individual join times
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
     * Utility method to parse Instant from string.
     */
    private Instant parseInstant(String instantString) {
        try {
            return instantString != null ? Instant.parse(instantString) : Instant.now();
        } catch (Exception e) {
            log.warn("Failed to parse instant: {}", instantString);
            return Instant.now();
        }
    }
    
    /**
     * Utility method to parse Double from object.
     */
    private Double parseDouble(Object value) {
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            return null;
        } catch (Exception e) {
            return null;
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
     * Changes the group status to "finalized" and makes it available for other users to find compatible groups.
     * Only the group creator can finalize the group.
     */
    public FinalizeGroupResponse finalizeGroup(String groupId, FinalizeGroupRequest request) {
        String userId = request.getUserId();
        log.info("User {} attempting to finalize group {}", userId, groupId);
        
        try {
            // Find the group
            Optional<Group> groupOpt = groupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                log.warn("Group not found: {}", groupId);
                return new FinalizeGroupResponse(groupId, "Group not found");
            }
            
            Group group = groupOpt.get();
            
            // Validate that the user is authorized to finalize the group
            if (!group.getCreatedBy().equals(userId) && !group.getCreatorUserId().equals(userId)) {
                log.warn("User {} not authorized to finalize group {}. Group created by: {}", 
                        userId, groupId, group.getCreatedBy());
                return new FinalizeGroupResponse(groupId, "Not authorized to finalize this group");
            }
            
            // Check if group is already finalized
            if ("finalized".equals(group.getStatus())) {
                log.info("Group {} is already finalized", groupId);
                return new FinalizeGroupResponse(groupId, "Group is already finalized");
            }
            
            // Validate that the group has the required data for finalization
            if (group.getPreferences() == null || group.getPreferences().isEmpty()) {
                log.warn("Cannot finalize group {} - missing trip preferences/data", groupId);
                return new FinalizeGroupResponse(groupId, 
                    "Cannot finalize group - trip data is required. Please save trip details first.");
            }
            
            // Validate that the group has at least one member
            if (group.getUserIds().isEmpty()) {
                log.warn("Cannot finalize group {} - no members", groupId);
                return new FinalizeGroupResponse(groupId, 
                    "Cannot finalize group - group must have at least one member");
            }
            
            // Update group status to active (will be finalized after payments)
            group.setStatus("active");
            group.setLastUpdated(Instant.now());
            
            // Save the updated group
            Group updatedGroup = groupRepository.save(group);
            
            log.info("Group {} successfully activated by user {} with {} members", 
                    updatedGroup.getId(), userId, updatedGroup.getUserIds().size());
            
            return new FinalizeGroupResponse(groupId, 
                String.format("Group '%s' activated successfully with %d member(s). Your trip is now ready for the next steps.", 
                        group.getGroupName() != null ? group.getGroupName() : "Trip Group", 
                        group.getUserIds().size()));
                        
        } catch (Exception e) {
            log.error("Error finalizing group {} by user {}: {}", groupId, userId, e.getMessage(), e);
            return new FinalizeGroupResponse(groupId, 
                "Failed to finalize group due to system error. Please try again.");
        }
    }
    
    /**
     * Get compatible groups for a specific trip.
     * Similar to saveTripWithSuggestions but doesn't save the trip data.
     */
    public List<CompatibleGroupResponse> getCompatibleGroups(String tripId, String userId) {
        log.info("Getting compatible groups for trip {} and user {}", tripId, userId);
        
        try {
            // Get the trip/group data
            Optional<Group> tripGroupOpt = groupRepository.findById(tripId);
            if (tripGroupOpt.isEmpty()) {
                log.warn("Trip/Group not found: {}", tripId);
                return new ArrayList<>();
            }
            
            Group tripGroup = tripGroupOpt.get();
            Map<String, Object> tripPreferences = tripGroup.getPreferences();
            
            if (tripPreferences == null) {
                log.warn("No trip preferences found for trip: {}", tripId);
                return new ArrayList<>();
            }
            
            // Find compatible public groups
            List<Group> publicGroups = groupRepository.findByVisibilityAndStatus("public", "finalized");
            
            List<CompatibleGroupResponse> compatibleGroups = new ArrayList<>();
            
            for (Group group : publicGroups) {
                if (group.getId().equals(tripId)) continue; // Skip current group
                if (group.getUserIds().size() >= group.getMaxMembers()) continue; // Skip full groups
                
                Map<String, Object> groupPreferences = group.getPreferences();
                if (groupPreferences == null) continue;
                
                // Calculate similarity score
                double similarityScore = calculateTripSimilarity(tripPreferences, groupPreferences);
                
                if (similarityScore >= minCompatibilityScore) {
                    CompatibleGroupResponse compatibleGroup = mapToCompatibleGroupResponse(group, similarityScore);
                    compatibleGroups.add(compatibleGroup);
                }
            }
            
            // Sort by similarity score descending
            compatibleGroups.sort((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
            
            // Return ALL compatible groups instead of limiting to top 10
            List<CompatibleGroupResponse> allSuggestions = new ArrayList<>(compatibleGroups);
            
            log.info("Found {} compatible groups for trip {}", allSuggestions.size(), tripId);
            return allSuggestions;
            
        } catch (Exception e) {
            log.error("Error getting compatible groups for trip {}: {}", tripId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Map Group to CompatibleGroupResponse DTO.
     */
    private CompatibleGroupResponse mapToCompatibleGroupResponse(Group group, double compatibilityScore) {
        CompatibleGroupResponse response = new CompatibleGroupResponse();
        
        response.setGroupId(group.getId());
        response.setTripName(group.getTripName());
        response.setGroupName(group.getGroupName());
        response.setCompatibilityScore(Math.round(compatibilityScore * 100.0) / 100.0);
        response.setCurrentMembers(group.getUserIds().size());
        response.setMaxMembers(group.getMaxMembers());
        response.setCreatedBy(group.getCreatedBy());
        
        Map<String, Object> preferences = group.getPreferences();
        if (preferences != null) {
            response.setStartDate((String) preferences.get("startDate"));
            response.setEndDate((String) preferences.get("endDate"));
            response.setDestinations((List<Map<String, String>>) preferences.get("destinations"));
            response.setActivities((List<String>) preferences.get("activities"));
            response.setTerrains((List<String>) preferences.get("terrains"));
        }
        
        return response;
    }
    
    /**
     * Generates a unique ID for join requests.
     * @return A unique join request ID
     */
    private String generateJoinRequestId() {
        return UUID.randomUUID().toString();
    }
}
