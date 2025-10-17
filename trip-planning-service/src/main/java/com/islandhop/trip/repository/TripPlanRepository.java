package com.islandhop.trip.repository;

import com.islandhop.trip.model.TripPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository interface for TripPlan entities.
 * Provides CRUD operations and custom query methods for trip plans.
 */
@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {

    /**
     * Find all trip plans for a specific user.
     *
     * @param userId The user ID to search for
     * @return List of trip plans belonging to the user
     */
    List<TripPlan> findByUserId(String userId);

    /**
     * Find all non-group trip plans for a specific user.
     * Excludes trips where type is "group".
     * Includes trips where type is null, does not exist, or has any value other than "group".
     * 
     * MongoDB Query Explanation:
     * - userId: ?0 matches the userId parameter
     * - $or: [ {type: {$exists: false}}, {type: null}, {type: {$ne: 'group'}} ]
     *   This matches documents where:
     *   1. The 'type' field doesn't exist at all
     *   2. The 'type' field is null
     *   3. The 'type' field exists but is not equal to 'group'
     *
     * @param userId The user ID to search for
     * @return List of non-group trip plans belonging to the user
     */
    @Query("{ 'userId': ?0, $or: [ {'type': {$exists: false}}, {'type': null}, {'type': {$ne: 'group'}} ] }")
    List<TripPlan> findNonGroupTripsByUserId(String userId);

    /**
     * Check if a trip plan exists for a specific user and trip name.
     *
     * @param userId   The user ID
     * @param tripName The trip name
     * @return true if a trip plan exists, false otherwise
     */
    boolean existsByUserIdAndTripName(String userId, String tripName);
}
