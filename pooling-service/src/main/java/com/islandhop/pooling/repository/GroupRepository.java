package com.islandhop.pooling.repository;

import com.islandhop.pooling.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Group entities.
 */
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    
    /**
     * Find groups by visibility.
     */
    List<Group> findByVisibility(String visibility);
    
    /**
     * Find public groups.
     */
    default List<Group> findPublicGroups() {
        return findByVisibility("public");
    }
    
    /**
     * Find groups where a user is a member.
     */
    List<Group> findByUserIdsContaining(String userId);
    
    /**
     * Find groups by trip ID.
     */
    List<Group> findByTripId(String tripId);
    
    /**
     * Find public groups with specific preferences.
     */
    @Query("{'visibility': 'public', 'preferences.interests': {$in: ?0}}")
    List<Group> findPublicGroupsByInterests(List<String> interests);
    
    /**
     * Find public groups by destination.
     */
    @Query("{'visibility': 'public', 'preferences.destination': ?0}")
    List<Group> findPublicGroupsByDestination(String destination);
    
    /**
     * Check if a group exists and is public.
     */
    @Query("{'groupId': ?0, 'visibility': 'public'}")
    Optional<Group> findPublicGroupById(String groupId);
    
    /**
     * Check if a group exists and user is a member.
     */
    @Query("{'groupId': ?0, 'userIds': {$in: [?1]}}")
    Optional<Group> findGroupByIdAndUserId(String groupId, String userId);
    
    /**
     * Find groups by creator.
     */
    @Query("{'userIds.0': ?0}")
    List<Group> findGroupsByCreator(String creatorUserId);
}
