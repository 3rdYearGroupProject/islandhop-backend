package com.islandhop.chat.controller;

import com.islandhop.chat.dto.MessageDTO;
import com.islandhop.chat.model.PersonalMessage;
import com.islandhop.chat.service.PersonalChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling personal chat operations.
 * Provides endpoints for sending and retrieving personal messages.
 */
@RestController
@RequestMapping("/api/v1/chat/personal")
@CrossOrigin(origins = "*")
public class PersonalChatController {

    private static final Logger logger = LoggerFactory.getLogger(PersonalChatController.class);

    @Autowired
    private PersonalChatService personalChatService;

    /**
     * Send a personal message between two users.
     * 
     * @param messageDTO The message data transfer object
     * @return ResponseEntity with the saved message
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageDTO messageDTO) {
        logger.info("Sending personal message from {} to {}", messageDTO.getSenderId(), messageDTO.getReceiverId());

        try {
            PersonalMessage savedMessage = personalChatService.sendMessage(messageDTO);
            logger.debug("Personal message sent successfully with ID: {}", savedMessage.getId());
            
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            logger.error("Error sending personal message: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Get message history between two users with pagination.
     * 
     * @param senderId The sender's ID
     * @param receiverId The receiver's ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return ResponseEntity with paginated messages
     */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessagesBetweenUsers(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Retrieving messages between {} and {} (page: {}, size: {})", 
                   senderId, receiverId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PersonalMessage> messages = personalChatService.getMessagesBetweenUsers(
                    senderId, receiverId, pageable);
            
            logger.debug("Retrieved {} messages between users", messages.getTotalElements());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error retrieving messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve messages: " + e.getMessage());
        }
    }

    /**
     * Get conversation list for a user.
     * Returns a list of users the specified user has conversed with.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with list of conversation partners
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversations(@PathVariable String userId) {
        logger.info("Retrieving conversations for user: {}", userId);

        try {
            List<Map<String, Object>> conversations = personalChatService.getUserConversations(userId);
            logger.debug("Found {} conversations for user", conversations.size());
            
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            logger.error("Error retrieving conversations: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve conversations: " + e.getMessage());
        }
    }

    /**
     * Get recent messages for a user across all conversations.
     * 
     * @param userId The user's ID
     * @param limit Maximum number of messages to return (default: 50)
     * @return ResponseEntity with recent messages
     */
    @GetMapping("/recent/{userId}")
    public ResponseEntity<?> getRecentMessages(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {
        
        logger.info("Retrieving {} recent messages for user: {}", limit, userId);

        try {
            List<PersonalMessage> recentMessages = personalChatService.getRecentMessagesForUser(userId, limit);
            logger.debug("Retrieved {} recent messages for user", recentMessages.size());
            
            return ResponseEntity.ok(recentMessages);
        } catch (Exception e) {
            logger.error("Error retrieving recent messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve recent messages: " + e.getMessage());
        }
    }

    /**
     * Mark messages as read between two users.
     * 
     * @param senderId The sender's ID
     * @param receiverId The receiver's ID
     * @return ResponseEntity with success message
     */
    @PutMapping("/mark-read")
    public ResponseEntity<?> markMessagesAsRead(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        
        logger.info("Marking messages as read between {} and {}", senderId, receiverId);

        try {
            personalChatService.markMessagesAsRead(senderId, receiverId);
            logger.debug("Messages marked as read successfully");
            
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            logger.error("Error marking messages as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to mark messages as read: " + e.getMessage());
        }
    }

    /**
     * Get unread message count for a user.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with unread count
     */
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<?> getUnreadMessageCount(@PathVariable String userId) {
        logger.info("Getting unread message count for user: {}", userId);

        try {
            long unreadCount = personalChatService.getUnreadMessageCount(userId);
            logger.debug("User {} has {} unread messages", userId, unreadCount);
            
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            logger.error("Error getting unread message count: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to get unread count: " + e.getMessage());
        }
    }

    /**
     * Search messages by content between two users.
     * 
     * @param senderId The sender's ID
     * @param receiverId The receiver's ID
     * @param searchTerm The search term
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return ResponseEntity with search results
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMessages(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Searching messages between {} and {} for term: {}", senderId, receiverId, searchTerm);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PersonalMessage> searchResults = personalChatService.searchMessages(
                    senderId, receiverId, searchTerm, pageable);
            
            logger.debug("Found {} messages matching search term", searchResults.getTotalElements());
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            logger.error("Error searching messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to search messages: " + e.getMessage());
        }
    }

    /**
     * Delete a personal message.
     * 
     * @param messageId The message ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        logger.info("Deleting personal message: {}", messageId);

        try {
            personalChatService.deleteMessage(messageId);
            logger.debug("Personal message deleted successfully");
            
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting message: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete message: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for personal chat service.
     * 
     * @return ResponseEntity with service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Personal chat service health check");
        return ResponseEntity.ok("Personal Chat Service is running");
    }
}
