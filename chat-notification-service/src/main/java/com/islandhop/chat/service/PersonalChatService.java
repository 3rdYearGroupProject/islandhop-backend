package com.islandhop.chat.service;

import com.islandhop.chat.dto.MessageDTO;
import com.islandhop.chat.model.PersonalMessage;
import com.islandhop.chat.repository.mongo.PersonalMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for handling personal chat message operations.
 * Manages message persistence, real-time delivery, and Redis pub/sub.
 */
@Service
public class PersonalChatService {

    private static final Logger logger = LoggerFactory.getLogger(PersonalChatService.class);

    @Autowired
    private PersonalMessageRepository messageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${chat.redis.personal-channel-prefix:chat:user:}")
    private String personalChannelPrefix;

    /**
     * Send a personal message between two users.
     * Saves message to MongoDB and broadcasts via Redis and WebSocket.
     * 
     * @param messageDTO The message data transfer object
     * @return The saved PersonalMessage entity
     */
    public PersonalMessage sendMessage(MessageDTO messageDTO) {
        logger.info("Sending personal message from {} to {}", messageDTO.getSenderId(), messageDTO.getReceiverId());

        // Create and save message
        PersonalMessage message = new PersonalMessage(
                messageDTO.getSenderId(),
                messageDTO.getReceiverId(),
                messageDTO.getContent()
        );
        message.setMessageType(messageDTO.getMessageType());

        PersonalMessage savedMessage = messageRepository.save(message);
        logger.debug("Personal message saved with ID: {}", savedMessage.getId());

        // Broadcast via Redis pub/sub
        broadcastMessageViaRedis(savedMessage);

        // Send via WebSocket
        sendMessageViaWebSocket(savedMessage);

        return savedMessage;
    }

    /**
     * Get all messages between two users with pagination.
     * 
     * @param userId1 First user's ID
     * @param userId2 Second user's ID
     * @param pageable Pagination information
     * @return Page of messages between the users
     */
    public Page<PersonalMessage> getMessagesBetweenUsers(String userId1, String userId2, Pageable pageable) {
        logger.info("Retrieving messages between users {} and {} with pagination", userId1, userId2);
        
        // For now, convert to list and return as page - you may need to implement pagination in repository
        List<PersonalMessage> messages = messageRepository.findMessagesBetweenUsers(userId1, userId2);
        logger.debug("Found {} messages between users", messages.size());
        
        return new org.springframework.data.domain.PageImpl<>(messages, pageable, messages.size());
    }

    /**
     * Get all messages between two users.
     * 
     * @param userId1 First user's ID
     * @param userId2 Second user's ID
     * @return List of messages between the users
     */
    public List<PersonalMessage> getMessagesBetweenUsers(String userId1, String userId2) {
        logger.info("Retrieving messages between users {} and {}", userId1, userId2);
        
        List<PersonalMessage> messages = messageRepository.findMessagesBetweenUsers(userId1, userId2);
        logger.debug("Found {} messages between users", messages.size());
        
        return messages;
    }

    /**
     * Get conversation list for a user.
     * Returns the latest message from each conversation.
     * 
     * @param userId The user's ID
     * @return List of conversations with latest messages
     */
    public List<Map<String, Object>> getUserConversations(String userId) {
        return getConversations(userId);
    }

    /**
     * Get conversation list for a user.
     * Returns the latest message from each conversation.
     * 
     * @param userId The user's ID
     * @return List of conversations with latest messages
     */
    public List<Map<String, Object>> getConversations(String userId) {
        logger.info("Retrieving conversations for user: {}", userId);

        List<PersonalMessage> allMessages = messageRepository.findConversationsByUserId(userId);
        
        // Group messages by conversation partner and get latest message
        Map<String, PersonalMessage> latestMessages = allMessages.stream()
                .collect(Collectors.toMap(
                        message -> message.getSenderId().equals(userId) ? message.getReceiverId() : message.getSenderId(),
                        message -> message,
                        (existing, replacement) -> 
                                existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
                ));

        List<Map<String, Object>> conversations = latestMessages.entrySet().stream()
                .map(entry -> {
                    String partnerId = entry.getKey();
                    PersonalMessage latestMessage = entry.getValue();
                    long unreadCount = messageRepository.countByReceiverIdAndIsReadFalse(userId);
                    
                    return Map.of(
                            "partnerId", partnerId,
                            "latestMessage", latestMessage,
                            "unreadCount", unreadCount
                    );
                })
                .collect(Collectors.toList());

        logger.debug("Found {} conversations for user", conversations.size());
        return conversations;
    }

    /**
     * Mark messages as read between two users.
     * 
     * @param receiverId The receiver's user ID
     * @param senderId The sender's user ID
     */
    public void markMessagesAsRead(String receiverId, String senderId) {
        logger.info("Marking messages as read from {} to {}", senderId, receiverId);

        List<PersonalMessage> unreadMessages = messageRepository.findMessagesBetweenUsers(senderId, receiverId)
                .stream()
                .filter(msg -> msg.getReceiverId().equals(receiverId) && !msg.isRead())
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);

        logger.debug("Marked {} messages as read", unreadMessages.size());
    }

    /**
     * Get recent messages for a user with limit.
     * 
     * @param userId The user's ID
     * @param limit Maximum number of messages to return
     * @return List of recent messages
     */
    public List<PersonalMessage> getRecentMessagesForUser(String userId, int limit) {
        logger.info("Retrieving {} recent messages for user: {}", limit, userId);
        
        // Get messages where user is sender or receiver, ordered by timestamp desc
        List<PersonalMessage> messages = messageRepository.findConversationsByUserId(userId);
        List<PersonalMessage> recentMessages = messages.stream()
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
        
        logger.debug("Found {} recent messages for user", recentMessages.size());
        return recentMessages;
    }

    /**
     * Get unread message count for a user.
     * 
     * @param userId The user's ID
     * @return Number of unread messages
     */
    public long getUnreadMessageCount(String userId) {
        logger.info("Getting unread message count for user: {}", userId);
        
        long count = messageRepository.countByReceiverIdAndIsReadFalse(userId);
        logger.debug("User {} has {} unread messages", userId, count);
        
        return count;
    }

    /**
     * Search messages by content with pagination.
     * 
     * @param userId The user's ID
     * @param partnerId The conversation partner's ID
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching messages
     */
    public Page<PersonalMessage> searchMessages(String userId, String partnerId, String searchTerm, Pageable pageable) {
        logger.info("Searching messages for user {} with partner {} using term: {}", userId, partnerId, searchTerm);
        
        // Simple implementation - get all messages and filter by search term
        List<PersonalMessage> allMessages = messageRepository.findMessagesBetweenUsers(userId, partnerId);
        List<PersonalMessage> matchingMessages = allMessages.stream()
                .filter(msg -> msg.getContent().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
        
        logger.debug("Found {} matching messages", matchingMessages.size());
        
        // Return as empty page for now - implement proper pagination in repository later
        return new org.springframework.data.domain.PageImpl<>(matchingMessages, pageable, matchingMessages.size());
    }

    /**
     * Delete a message by ID.
     * 
     * @param messageId The message ID to delete
     */
    public void deleteMessage(String messageId) {
        logger.info("Deleting message with ID: {}", messageId);
        
        messageRepository.deleteById(messageId);
        logger.debug("Message deleted successfully");
    }

    /**
     * Broadcast message via Redis pub/sub.
     * 
     * @param message The message to broadcast
     */
    private void broadcastMessageViaRedis(PersonalMessage message) {
        try {
            String receiverChannel = personalChannelPrefix + message.getReceiverId();
            redisTemplate.convertAndSend(receiverChannel, message);
            logger.debug("Message broadcasted via Redis to channel: {}", receiverChannel);
        } catch (Exception e) {
            logger.error("Failed to broadcast message via Redis: {}", e.getMessage());
        }
    }

    /**
     * Send message via WebSocket.
     * 
     * @param message The message to send
     */
    private void sendMessageViaWebSocket(PersonalMessage message) {
        try {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId(),
                    "/queue/messages",
                    message
            );
            logger.debug("Message sent via WebSocket to user: {}", message.getReceiverId());
        } catch (Exception e) {
            logger.error("Failed to send message via WebSocket: {}", e.getMessage());
        }
    }
}
