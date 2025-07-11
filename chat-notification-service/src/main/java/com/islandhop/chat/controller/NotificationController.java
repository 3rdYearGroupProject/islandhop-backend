package com.islandhop.chat.controller;

import com.islandhop.chat.dto.NotificationDTO;
import com.islandhop.chat.model.Notification;
import com.islandhop.chat.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for handling notification operations.
 * Provides endpoints for sending, retrieving, and managing notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Send a new notification to a user.
     * 
     * @param notificationDTO The notification data transfer object
     * @return ResponseEntity with the saved notification
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        logger.info("Sending notification to user {}: {}", notificationDTO.getUserId(), notificationDTO.getTitle());

        try {
            Notification savedNotification = notificationService.sendNotification(notificationDTO);
            logger.debug("Notification sent successfully with ID: {}", savedNotification.getId());
            
            return ResponseEntity.ok(savedNotification);
        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Get all notifications for a user.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with list of notifications
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable String userId) {
        logger.info("Retrieving all notifications for user: {}", userId);

        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            logger.debug("Retrieved {} notifications for user", notifications.size());
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error retrieving notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve notifications: " + e.getMessage());
        }
    }

    /**
     * Get unread notifications for a user.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with list of unread notifications
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String userId) {
        logger.info("Retrieving unread notifications for user: {}", userId);

        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            logger.debug("Retrieved {} unread notifications for user", notifications.size());
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error retrieving unread notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve unread notifications: " + e.getMessage());
        }
    }

    /**
     * Mark a notification as read.
     * 
     * @param notificationId The notification's ID
     * @return ResponseEntity with the updated notification
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        logger.info("Marking notification {} as read", notificationId);

        try {
            Notification updatedNotification = notificationService.markNotificationAsRead(notificationId);
            logger.debug("Notification marked as read successfully");
            
            return ResponseEntity.ok(updatedNotification);
        } catch (IllegalArgumentException e) {
            logger.error("Notification not found: {}", notificationId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to mark notification as read: " + e.getMessage());
        }
    }

    /**
     * Mark all notifications as read for a user.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with the number of notifications marked as read
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(@PathVariable String userId) {
        logger.info("Marking all notifications as read for user: {}", userId);

        try {
            int updatedCount = notificationService.markAllNotificationsAsRead(userId);
            logger.debug("Marked {} notifications as read for user", updatedCount);
            
            return ResponseEntity.ok("Marked " + updatedCount + " notifications as read");
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to mark notifications as read: " + e.getMessage());
        }
    }

    /**
     * Delete a notification.
     * 
     * @param notificationId The notification's ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        logger.info("Deleting notification: {}", notificationId);

        try {
            notificationService.deleteNotification(notificationId);
            logger.debug("Notification deleted successfully");
            
            return ResponseEntity.ok("Notification deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Notification not found: {}", notificationId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete notification: " + e.getMessage());
        }
    }

    /**
     * Get unread notification count for a user.
     * 
     * @param userId The user's ID
     * @return ResponseEntity with unread count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable String userId) {
        logger.info("Getting unread notification count for user: {}", userId);

        try {
            long unreadCount = notificationService.getUnreadNotificationCount(userId);
            logger.debug("User {} has {} unread notifications", userId, unreadCount);
            
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            logger.error("Error getting unread notification count: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to get unread count: " + e.getMessage());
        }
    }

    /**
     * Get notifications by type for a user.
     * 
     * @param userId The user's ID
     * @param type The notification type
     * @return ResponseEntity with notifications of the specified type
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<?> getNotificationsByType(
            @PathVariable String userId,
            @PathVariable String type) {
        
        logger.info("Retrieving {} notifications for user: {}", type, userId);

        try {
            List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
            logger.debug("Retrieved {} {} notifications for user", notifications.size(), type);
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error retrieving notifications by type: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve notifications: " + e.getMessage());
        }
    }

    /**
     * Admin endpoint to clean up old notifications.
     * Deletes notifications older than the specified number of days.
     * 
     * @param daysOld Number of days to keep notifications (default: 30)
     * @return ResponseEntity with cleanup results
     */
    @DeleteMapping("/admin/cleanup")
    public ResponseEntity<?> cleanupOldNotifications(@RequestParam(defaultValue = "30") int daysOld) {
        logger.info("Cleaning up notifications older than {} days", daysOld);

        try {
            int deletedCount = notificationService.cleanupOldNotifications(daysOld);
            logger.debug("Cleaned up {} old notifications", deletedCount);
            
            return ResponseEntity.ok("Deleted " + deletedCount + " old notifications");
        } catch (Exception e) {
            logger.error("Error cleaning up old notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to cleanup notifications: " + e.getMessage());
        }
    }

    /**
     * Batch send notifications to multiple users.
     * 
     * @param userIds List of user IDs
     * @param notificationDTO The notification data (without userId)
     * @return ResponseEntity with success message
     */
    @PostMapping("/batch-send")
    public ResponseEntity<?> batchSendNotifications(
            @RequestParam List<String> userIds,
            @Valid @RequestBody NotificationDTO notificationDTO) {
        
        logger.info("Batch sending notification to {} users", userIds.size());

        try {
            int sentCount = 0;
            for (String userId : userIds) {
                try {
                    NotificationDTO userNotification = new NotificationDTO();
                    userNotification.setUserId(userId);
                    userNotification.setTitle(notificationDTO.getTitle());
                    userNotification.setMessage(notificationDTO.getMessage());
                    userNotification.setType(notificationDTO.getType());
                    userNotification.setPriority(notificationDTO.getPriority());
                    userNotification.setRelatedEntityId(notificationDTO.getRelatedEntityId());
                    
                    notificationService.sendNotification(userNotification);
                    sentCount++;
                } catch (Exception e) {
                    logger.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
                }
            }
            
            logger.debug("Successfully sent {} out of {} notifications", sentCount, userIds.size());
            return ResponseEntity.ok("Sent " + sentCount + " out of " + userIds.size() + " notifications");
        } catch (Exception e) {
            logger.error("Error batch sending notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to batch send notifications: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for notification service.
     * 
     * @return ResponseEntity with service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Notification service health check");
        return ResponseEntity.ok("Notification Service is running");
    }
}
