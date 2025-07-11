package com.islandhop.chat.repository.mongo;

import com.islandhop.chat.model.PersonalMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for PersonalMessage entities.
 * Provides CRUD operations and custom queries for personal chat messages.
 */
@Repository
public interface PersonalMessageRepository extends MongoRepository<PersonalMessage, String> {

    /**
     * Find all messages between two users, ordered by timestamp.
     * 
     * @param senderId The sender's user ID
     * @param receiverId The receiver's user ID
     * @return List of messages between the two users
     */
    @Query("{ $or: [ { $and: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }, " +
           "{ $and: [ { 'senderId': ?1 }, { 'receiverId': ?0 } ] } ] }")
    List<PersonalMessage> findMessagesBetweenUsers(String senderId, String receiverId);

    /**
     * Find all conversations for a specific user.
     * Returns the latest message from each conversation.
     * 
     * @param userId The user's ID
     * @return List of latest messages from each conversation
     */
    @Query("{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }")
    List<PersonalMessage> findConversationsByUserId(String userId);

    /**
     * Find unread messages for a specific receiver.
     * 
     * @param receiverId The receiver's user ID
     * @return List of unread messages
     */
    List<PersonalMessage> findByReceiverIdAndIsReadFalse(String receiverId);

    /**
     * Count unread messages for a specific receiver.
     * 
     * @param receiverId The receiver's user ID
     * @return Number of unread messages
     */
    long countByReceiverIdAndIsReadFalse(String receiverId);
}
