package com.islandhop.chat.repository.mongo;

import com.islandhop.chat.model.GroupMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for GroupMessage entities.
 * Provides CRUD operations and custom queries for group chat messages.
 */
@Repository
public interface GroupMessageRepository extends MongoRepository<GroupMessage, String> {

    /**
     * Find all messages in a specific group, ordered by timestamp.
     * 
     * @param groupId The group's ID
     * @return List of messages in the group
     */
    List<GroupMessage> findByGroupIdOrderByTimestampAsc(String groupId);

    /**
     * Find recent messages in a group (last N messages).
     * 
     * @param groupId The group's ID
     * @param limit Maximum number of messages to return
     * @return List of recent messages
     */
    @Query("{ 'groupId': ?0 }")
    List<GroupMessage> findRecentMessagesByGroupId(String groupId, int limit);

    /**
     * Find messages in a group after a specific timestamp.
     * 
     * @param groupId The group's ID
     * @param timestamp The timestamp to search after
     * @return List of messages after the timestamp
     */
    List<GroupMessage> findByGroupIdAndTimestampAfterOrderByTimestampAsc(String groupId, LocalDateTime timestamp);

    /**
     * Find all messages sent by a specific user in all groups.
     * 
     * @param senderId The sender's user ID
     * @return List of messages sent by the user
     */
    List<GroupMessage> findBySenderIdOrderByTimestampDesc(String senderId);

    /**
     * Count messages in a specific group.
     * 
     * @param groupId The group's ID
     * @return Number of messages in the group
     */
    long countByGroupId(String groupId);
}
