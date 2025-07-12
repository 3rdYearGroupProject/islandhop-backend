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
     * Find groups by trip ID.
     */
    List<Group> findByTripId(String tripId);
    
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
}
