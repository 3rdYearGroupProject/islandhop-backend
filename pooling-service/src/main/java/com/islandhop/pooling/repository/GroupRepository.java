package com.islandhop.pooling.repository;

import com.islandhop.pooling.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Group entities.
 * Follows the same patterns as TripPlanRepository for consistency.
 */
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    
    /**
     * Find groups by visibility.
     */
    List<Group> findByVisibility(String visibility);
    
    /**
     * Find groups where a user is a member.
     */
    List<Group> findByUserIdsContaining(String userId);
    
    /**
     * Find groups created by a specific user.
     */
    List<Group> findByCreatorUserId(String creatorUserId);
    
    /**
     * Find groups by created by field (for backward compatibility).
     */
    List<Group> findByCreatedBy(String createdBy);
    
    /**
     * Find groups by trip ID.
     */
    List<Group> findByTripId(String tripId);
    
    /**
     * Find first group by trip ID.
     */
    Optional<Group> findFirstByTripId(String tripId);
    
    /**
     * Find public groups with preferences containing specific interests.
     */
    @Query("{'visibility': 'public', 'preferences.interests': { $in: ?0 }}")
    List<Group> findPublicGroupsByInterests(List<String> interests);
    
    /**
     * Find public groups by destination.
     */
    @Query("{'visibility': 'public', 'preferences.destination': ?0}")
    List<Group> findPublicGroupsByDestination(String destination);
    
    /**
     * Find group by trip ID and user ID.
     */
    Optional<Group> findByTripIdAndUserIdsContaining(String tripId, String userId);

    /**
     * Find groups by visibility and status, excluding a specific group.
     */
    List<Group> findByVisibilityAndStatusAndIdNot(String visibility, String status, String excludeId);
    
    /**
     * Find groups by visibility and status.
     */
    List<Group> findByVisibilityAndStatus(String visibility, String status);
    
    /**
     * Find public groups with filtering capabilities.
     */
    @Query("{'visibility': 'public', 'status': 'finalized'}")
    List<Group> findPublicFinalizedGroups();
    
    /**
     * Find public groups by base city.
     */
    @Query("{'visibility': 'public', 'preferences.baseCity': ?0}")
    List<Group> findPublicGroupsByBaseCity(String baseCity);
    
    /**
     * Find public groups by budget level.
     */
    @Query("{'visibility': 'public', 'preferences.budgetLevel': ?0}")
    List<Group> findPublicGroupsByBudgetLevel(String budgetLevel);
    
    /**
     * Find public groups by date range.
     */
    @Query("{'visibility': 'public', 'preferences.startDate': ?0, 'preferences.endDate': ?1}")
    List<Group> findPublicGroupsByDateRange(String startDate, String endDate);
    
    /**
     * Find public groups with complex filtering.
     */
    @Query("{'visibility': 'public', 'status': 'finalized', " +
           "$and': [" +
           "{'$or': [{'preferences.baseCity': {$exists: false}}, {'preferences.baseCity': ?0}]}, " +
           "{'$or': [{'preferences.budgetLevel': {$exists: false}}, {'preferences.budgetLevel': ?1}]}, " +
           "{'$or': [{'preferences.startDate': {$exists: false}}, {'preferences.startDate': ?2}]}, " +
           "{'$or': [{'preferences.endDate': {$exists: false}}, {'preferences.endDate': ?3}]}" +
           "]}")
    List<Group> findPublicGroupsWithFilters(String baseCity, String budgetLevel, String startDate, String endDate);
}
