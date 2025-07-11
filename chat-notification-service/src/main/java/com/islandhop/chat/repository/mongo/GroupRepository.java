package com.islandhop.chat.repository.mongo;

import com.islandhop.chat.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for Group entities.
 * Provides CRUD operations and custom queries for chat groups.
 */
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    /**
     * Find all groups where the user is a member.
     * 
     * @param userId The user's ID
     * @return List of groups the user belongs to
     */
    @Query("{ 'memberIds': { $in: [?0] } }")
    List<Group> findGroupsByMemberId(String userId);

    /**
     * Find all groups administered by a specific user.
     * 
     * @param adminId The admin's user ID
     * @return List of groups administered by the user
     */
    List<Group> findByAdminId(String adminId);

    /**
     * Find groups by name pattern (case insensitive).
     * 
     * @param groupName The group name pattern
     * @return List of groups matching the name pattern
     */
    @Query("{ 'groupName': { $regex: ?0, $options: 'i' } }")
    List<Group> findByGroupNameContainingIgnoreCase(String groupName);

    /**
     * Check if a user is a member of a specific group.
     * 
     * @param groupId The group's ID
     * @param userId The user's ID
     * @return true if user is a member, false otherwise
     */
    @Query("{ '_id': ?0, 'memberIds': { $in: [?1] } }")
    boolean isUserMemberOfGroup(String groupId, String userId);
}
