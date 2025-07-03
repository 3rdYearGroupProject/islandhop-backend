package com.islandhop.pooling.service;

import com.islandhop.pooling.algorithm.TimelinePoolingAlgorithm;
import com.islandhop.pooling.client.*;
import com.islandhop.pooling.dto.PoolingRequest;
import com.islandhop.pooling.dto.PoolSuggestion;
import com.islandhop.pooling.model.*;
import com.islandhop.pooling.repository.TripPoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoolingService {

    private static final Logger logger = LoggerFactory.getLogger(PoolingService.class);

    private final TripPoolRepository poolRepository;
    private final DirectIntegrationService integrationService;
    private final TimelinePoolingAlgorithm timelineAlgorithm;

    @Value("${pooling.pool.max-size:6}")
    private int maxPoolSize;

    @Value("${pooling.pool.min-size:2}")
    private int minPoolSize;

    public PoolingService(TripPoolRepository poolRepository,
                         DirectIntegrationService integrationService,
                         TimelinePoolingAlgorithm timelineAlgorithm) {
        this.poolRepository = poolRepository;
        this.integrationService = integrationService;
        this.timelineAlgorithm = timelineAlgorithm;
    }

    /**
     * Find potential pools for a user based on their trip
     */
    public List<PoolSuggestion> findPotentialPools(PoolingRequest request) {
        logger.info("Finding potential pools for user: {} with trip: {}", request.getUserId(), request.getTripId());

        // Get user's trip
        TripDto userTrip = integrationService.getTripByIdAndUserId(request.getTripId(), request.getUserId());
        if (userTrip == null) {
            logger.warn("Trip not found: {} for user: {}", request.getTripId(), request.getUserId());
            return Collections.emptyList();
        }

        // Get user's profile
        TouristProfileDto userProfile = integrationService.getTouristProfileByEmail(request.getUserId());
        if (userProfile == null) {
            logger.warn("User profile not found: {}", request.getUserId());
            return Collections.emptyList();
        }

        // Find candidate trips in overlapping date range
        LocalDate searchStart = userTrip.getStartDate().minusDays(request.getDateFlexibilityDays());
        LocalDate searchEnd = userTrip.getEndDate().plusDays(request.getDateFlexibilityDays());
        
        List<TripDto> candidateTrips = integrationService.getTripsInDateRange(searchStart, searchEnd);
        logger.info("Found {} candidate trips in date range", candidateTrips.size());

        // Get profiles for all candidate users
        Map<String, TouristProfileDto> candidateProfiles = new HashMap<>();
        for (TripDto trip : candidateTrips) {
            TouristProfileDto profile = integrationService.getTouristProfileByEmail(trip.getUserId());
            if (profile != null) {
                candidateProfiles.put(trip.getUserId(), profile);
            }
        }

        // Find timeline-based matches
        List<TimelinePoolingAlgorithm.TripPoolMatch> matches = timelineAlgorithm.findTimelineMatches(
                userTrip, candidateTrips, userProfile, candidateProfiles);

        // Convert matches to pool suggestions
        List<PoolSuggestion> suggestions = new ArrayList<>();
        
        for (TimelinePoolingAlgorithm.TripPoolMatch match : matches) {
            // Check if these users are already in a pool together
            if (!areUsersAlreadyPooled(request.getUserId(), match.getCandidateTrip().getUserId())) {
                PoolSuggestion suggestion = createPoolSuggestion(userTrip, match, userProfile);
                suggestions.add(suggestion);
            }
        }

        // Also check existing pools that user could join
        List<PoolSuggestion> existingPoolSuggestions = findJoinableExistingPools(userTrip, userProfile);
        suggestions.addAll(existingPoolSuggestions);

        // Sort by compatibility score
        suggestions.sort((s1, s2) -> Double.compare(s2.getCompatibilityScore(), s1.getCompatibilityScore()));

        logger.info("Found {} pool suggestions for user: {}", suggestions.size(), request.getUserId());
        return suggestions;
    }

    /**
     * Create a new trip pool
     */
    public TripPool createTripPool(String creatorUserId, String tripId, String poolName, String description) {
        logger.info("Creating new trip pool for user: {} with trip: {}", creatorUserId, tripId);

        // Get creator's trip and profile
        TripDto creatorTrip = integrationService.getTripByIdAndUserId(tripId, creatorUserId);
        TouristProfileDto creatorProfile = integrationService.getTouristProfileByEmail(creatorUserId);

        if (creatorTrip == null || creatorProfile == null) {
            throw new IllegalArgumentException("Invalid trip or user profile");
        }

        // Create pool member for creator
        PoolMember creator = new PoolMember();
        creator.setUserId(creatorUserId);
        creator.setEmail(creatorProfile.getEmail());
        creator.setFirstName(creatorProfile.getFirstName());
        creator.setLastName(creatorProfile.getLastName());
        creator.setNationality(creatorProfile.getNationality());
        creator.setLanguages(creatorProfile.getLanguages());
        creator.setTripId(tripId);
        creator.setTripName(creatorTrip.getTripName());
        creator.setTripCategories(creatorTrip.getCategories());
        creator.setActivityPacing(creatorTrip.getPacing());
        creator.setRole(PoolMember.MemberRole.CREATOR);
        creator.setStatus(PoolMember.MemberStatus.ACTIVE);
        creator.setJoinedAt(LocalDateTime.now());
        creator.setCompatibilityScore(1.0); // Perfect compatibility with self

        // Create the trip pool
        TripPool pool = new TripPool();
        pool.setPoolName(poolName != null ? poolName : "Trip to " + creatorTrip.getBaseCity());
        pool.setDescription(description);
        pool.setPoolType(TripPool.PoolType.TIMELINE_BASED);
        pool.setStatus(TripPool.PoolStatus.FORMING);
        pool.setStartDate(creatorTrip.getStartDate());
        pool.setEndDate(creatorTrip.getEndDate());
        pool.setBaseCity(creatorTrip.getBaseCity());
        pool.setMembers(Collections.singletonList(creator));
        pool.setCreatedByUserId(creatorUserId);
        pool.setMaxMembers(maxPoolSize);
        pool.setCurrentMembers(1);
        pool.setCommonInterests(creatorTrip.getCategories());
        pool.setPublic(true);
        pool.setAllowJoinRequests(true);
        pool.setJoinCode(generateJoinCode());
        pool.setCreatedAt(LocalDateTime.now());
        pool.setUpdatedAt(LocalDateTime.now());

        // Extract common cities from planned places
        if (creatorTrip.getPlaces() != null) {
            Set<String> cities = creatorTrip.getPlaces().stream()
                    .map(PlannedPlaceDto::getCity)
                    .collect(Collectors.toSet());
            pool.setCommonCities(new ArrayList<>(cities));
        }

        TripPool savedPool = poolRepository.save(pool);
        logger.info("Created trip pool: {} for user: {}", savedPool.getPoolId(), creatorUserId);
        
        return savedPool;
    }

    /**
     * Join an existing trip pool
     */
    public TripPool joinTripPool(String poolId, String userId, String tripId) {
        logger.info("User {} attempting to join pool: {}", userId, poolId);

        TripPool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

        if (pool.getCurrentMembers() >= pool.getMaxMembers()) {
            throw new IllegalStateException("Pool is full");
        }

        if (pool.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId))) {
            throw new IllegalStateException("User already in pool");
        }

        // Get user's trip and profile
        TripDto userTrip = integrationService.getTripByIdAndUserId(tripId, userId);
        TouristProfileDto userProfile = integrationService.getTouristProfileByEmail(userId);

        if (userTrip == null || userProfile == null) {
            throw new IllegalArgumentException("Invalid trip or user profile");
        }

        // Calculate compatibility with existing members
        double avgCompatibility = calculateAverageCompatibilityWithPool(userTrip, userProfile, pool);

        // Create new pool member
        PoolMember newMember = new PoolMember();
        newMember.setUserId(userId);
        newMember.setEmail(userProfile.getEmail());
        newMember.setFirstName(userProfile.getFirstName());
        newMember.setLastName(userProfile.getLastName());
        newMember.setNationality(userProfile.getNationality());
        newMember.setLanguages(userProfile.getLanguages());
        newMember.setTripId(tripId);
        newMember.setTripName(userTrip.getTripName());
        newMember.setTripCategories(userTrip.getCategories());
        newMember.setActivityPacing(userTrip.getPacing());
        newMember.setRole(PoolMember.MemberRole.MEMBER);
        newMember.setStatus(PoolMember.MemberStatus.ACTIVE);
        newMember.setJoinedAt(LocalDateTime.now());
        newMember.setCompatibilityScore(avgCompatibility);

        // Update pool
        pool.getMembers().add(newMember);
        pool.setCurrentMembers(pool.getCurrentMembers() + 1);
        pool.setUpdatedAt(LocalDateTime.now());

        // Update common interests and cities
        updatePoolCommonAttributes(pool, userTrip);

        // Recalculate average compatibility
        pool.setAverageCompatibilityScore(calculatePoolAverageCompatibility(pool));

        TripPool updatedPool = poolRepository.save(pool);
        logger.info("User {} successfully joined pool: {}", userId, poolId);
        
        return updatedPool;
    }

    /**
     * Get all pools for a user (created by or member of)
     */
    public List<TripPool> getUserPools(String userId) {
        logger.info("Fetching pools for user: {}", userId);

        List<TripPool> createdPools = poolRepository.findByCreatedByUserId(userId);
        List<TripPool> memberPools = poolRepository.findByMemberUserId(userId);

        Set<String> poolIds = new HashSet<>();
        List<TripPool> allPools = new ArrayList<>();

        for (TripPool pool : createdPools) {
            if (poolIds.add(pool.getPoolId())) {
                allPools.add(pool);
            }
        }

        for (TripPool pool : memberPools) {
            if (poolIds.add(pool.getPoolId())) {
                allPools.add(pool);
            }
        }

        logger.info("Found {} pools for user: {}", allPools.size(), userId);
        return allPools;
    }

    /**
     * Leave a trip pool
     */
    public void leaveTripPool(String poolId, String userId) {
        logger.info("User {} leaving pool: {}", userId, poolId);

        TripPool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));

        PoolMember memberToRemove = pool.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User not in pool"));

        if (memberToRemove.getRole() == PoolMember.MemberRole.CREATOR && pool.getCurrentMembers() > 1) {
            // Transfer ownership to another member
            PoolMember newCreator = pool.getMembers().stream()
                    .filter(m -> !m.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
            
            if (newCreator != null) {
                newCreator.setRole(PoolMember.MemberRole.CREATOR);
                pool.setCreatedByUserId(newCreator.getUserId());
            }
        }

        memberToRemove.setStatus(PoolMember.MemberStatus.LEFT);
        pool.setCurrentMembers(pool.getCurrentMembers() - 1);
        pool.setUpdatedAt(LocalDateTime.now());

        if (pool.getCurrentMembers() < minPoolSize) {
            pool.setStatus(TripPool.PoolStatus.CANCELLED);
        }

        poolRepository.save(pool);
        logger.info("User {} left pool: {}", userId, poolId);
    }

    // Helper methods

    private boolean areUsersAlreadyPooled(String userId1, String userId2) {
        List<TripPool> user1Pools = poolRepository.findByMemberUserId(userId1);
        List<TripPool> user2Pools = poolRepository.findByMemberUserId(userId2);

        Set<String> user1PoolIds = user1Pools.stream()
                .filter(p -> p.getStatus() == TripPool.PoolStatus.ACTIVE || p.getStatus() == TripPool.PoolStatus.FORMING)
                .map(TripPool::getPoolId)
                .collect(Collectors.toSet());

        return user2Pools.stream()
                .filter(p -> p.getStatus() == TripPool.PoolStatus.ACTIVE || p.getStatus() == TripPool.PoolStatus.FORMING)
                .anyMatch(p -> user1PoolIds.contains(p.getPoolId()));
    }

    private PoolSuggestion createPoolSuggestion(TripDto userTrip, 
                                              TimelinePoolingAlgorithm.TripPoolMatch match,
                                              TouristProfileDto userProfile) {
        PoolSuggestion suggestion = new PoolSuggestion();
        
        // Create a hypothetical pool for this suggestion
        TripPool suggestedPool = new TripPool();
        suggestedPool.setPoolName("Trip with " + match.getCandidateProfile().getFirstName());
        suggestedPool.setPoolType(TripPool.PoolType.TIMELINE_BASED);
        suggestedPool.setStatus(TripPool.PoolStatus.FORMING);
        suggestedPool.setStartDate(match.getTimelineOverlap().getOverlapStart());
        suggestedPool.setEndDate(match.getTimelineOverlap().getOverlapEnd());
        suggestedPool.setBaseCity(userTrip.getBaseCity());
        
        suggestion.setSuggestedPool(suggestedPool);
        suggestion.setCompatibilityScore(match.getCompatibilityScore().getOverallScore());
        suggestion.setOverlapDays(match.getTimelineOverlap().getOverlapDays());
        suggestion.setMatchReason("Timeline-based compatibility");
        suggestion.setCommonInterests(match.getCompatibilityScore().getCompatibilityReasons());
        suggestion.setCurrentMembers(1); // Just the candidate
        suggestion.setPoolCreatedBy(match.getCandidateProfile().getFirstName());
        
        return suggestion;
    }

    private List<PoolSuggestion> findJoinableExistingPools(TripDto userTrip, TouristProfileDto userProfile) {
        // Find existing pools with overlapping dates
        List<TripPool> existingPools = poolRepository.findPoolsWithDateOverlap(
                userTrip.getStartDate(), userTrip.getEndDate());

        List<PoolSuggestion> suggestions = new ArrayList<>();

        for (TripPool pool : existingPools) {
            if (pool.getCurrentMembers() < pool.getMaxMembers() && pool.isAllowJoinRequests()) {
                // Calculate compatibility with this pool
                double compatibility = calculateCompatibilityWithExistingPool(userTrip, userProfile, pool);
                
                if (compatibility >= 0.6) { // Minimum threshold
                    PoolSuggestion suggestion = new PoolSuggestion();
                    suggestion.setSuggestedPool(pool);
                    suggestion.setCompatibilityScore(compatibility);
                    suggestion.setMatchReason("Existing pool match");
                    suggestion.setCurrentMembers(pool.getCurrentMembers());
                    suggestion.setPoolCreatedBy(pool.getCreatedByUserId());
                    
                    suggestions.add(suggestion);
                }
            }
        }

        return suggestions;
    }

    private double calculateAverageCompatibilityWithPool(TripDto userTrip, TouristProfileDto userProfile, TripPool pool) {
        // Implementation to calculate compatibility with existing pool members
        // For now, return a default value
        return 0.75;
    }

    private double calculateCompatibilityWithExistingPool(TripDto userTrip, TouristProfileDto userProfile, TripPool pool) {
        // Implementation to calculate compatibility with existing pool
        // For now, return a default value  
        return 0.7;
    }

    private void updatePoolCommonAttributes(TripPool pool, TripDto newMemberTrip) {
        // Update common interests
        Set<String> currentInterests = new HashSet<>(pool.getCommonInterests());
        Set<String> newInterests = new HashSet<>(newMemberTrip.getCategories());
        currentInterests.retainAll(newInterests); // Keep only common interests
        pool.setCommonInterests(new ArrayList<>(currentInterests));

        // Update common cities
        if (newMemberTrip.getPlaces() != null) {
            Set<String> newCities = newMemberTrip.getPlaces().stream()
                    .map(PlannedPlaceDto::getCity)
                    .collect(Collectors.toSet());
            
            Set<String> currentCities = new HashSet<>(pool.getCommonCities());
            newCities.retainAll(currentCities); // Keep only common cities
            pool.setCommonCities(new ArrayList<>(newCities));
        }
    }

    private double calculatePoolAverageCompatibility(TripPool pool) {
        // Calculate average compatibility between all members
        // For now, return average of individual compatibility scores
        return pool.getMembers().stream()
                .mapToDouble(PoolMember::getCompatibilityScore)
                .average()
                .orElse(0.0);
    }

    private String generateJoinCode() {
        return "POOL" + System.currentTimeMillis() % 100000;
    }
}
