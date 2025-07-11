package com.islandhop.chat.service;

import com.islandhop.chat.dto.NotificationDTO;
import com.islandhop.chat.model.Notification;
import com.islandhop.chat.repository.jpa.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling notification operations.
 * Manages notification persistence, delivery, and real-time broadcasting.
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send a new notification to a user.
     * Saves notification to PostgreSQL and broadcasts via Redis and WebSocket.
     * 
     * @param notificationDTO The notification data transfer object
     * @return The saved Notification entity
     */
    public Notification sendNotification(NotificationDTO notificationDTO) {
        logger.info("Sending notification to user {}: {}", notificationDTO.getUserId(), notificationDTO.getTitle());

        // Create and save notification
        Notification notification = new Notification(
                notificationDTO.getUserId(),
                notificationDTO.getTitle(),
                notificationDTO.getMessage(),
                notificationDTO.getType()
        );
        notification.setPriority(notificationDTO.getPriority());
        notification.setRelatedEntityId(notificationDTO.getRelatedEntityId());

        Notification savedNotification = notificationRepository.save(notification);
        logger.debug("Notification saved with ID: {}", savedNotification.getId());

        // Broadcast via Redis pub/sub
        broadcastNotificationViaRedis(savedNotification);

        // Send via WebSocket
        sendNotificationViaWebSocket(savedNotification);

        return savedNotification;
    }

    /**
     * Get all notifications for a user.
     * 
     * @param userId The user's ID
     * @return List of notifications for the user
     */
    public List<Notification> getUserNotifications(String userId) {
        logger.info("Retrieving notifications for user: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        logger.debug("Found {} notifications for user", notifications.size());
        
        return notifications;
    }

    /**
     * Get unread notifications for a user.
     * 
     * @param userId The user's ID
     * @return List of unread notifications
     */
    public List<Notification> getUnreadNotifications(String userId) {
        logger.info("Retrieving unread notifications for user: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        logger.debug("Found {} unread notifications for user", notifications.size());
        
        return notifications;
    }

    /**
     * Mark a notification as read.
     * 
     * @param notificationId The notification's ID
     * @return The updated Notification entity
     */
    public Notification markNotificationAsRead(Long notificationId) {
        logger.info("Marking notification {} as read", notificationId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new IllegalArgumentException("Notification not found");
        }

        Notification notification = notificationOpt.get();
        notification.setRead(true);
        
        Notification updatedNotification = notificationRepository.save(notification);
        logger.debug("Notification {} marked as read", notificationId);

        return updatedNotification;
    }

    /**
     * Mark all notifications as read for a user.
     * 
     * @param userId The user's ID
     * @return Number of notifications marked as read
     */
    public int markAllNotificationsAsRead(String userId) {
        logger.info("Marking all notifications as read for user: {}", userId);

        int updatedCount = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        logger.debug("Marked {} notifications as read for user", updatedCount);

        return updatedCount;
    }

    /**
     * Delete a notification.
     * 
     * @param notificationId The notification's ID
     */
    public void deleteNotification(Long notificationId) {
        logger.info("Deleting notification: {}", notificationId);

        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("Notification not found");
        }

        notificationRepository.deleteById(notificationId);
        logger.debug("Notification {} deleted", notificationId);
    }

    /**
     * Get notification count for a user.
     * 
     * @param userId The user's ID
     * @return Number of unread notifications
     */
    public long getUnreadNotificationCount(String userId) {
        logger.info("Getting unread notification count for user: {}", userId);
        
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        logger.debug("User {} has {} unread notifications", userId, count);
        
        return count;
    }

    /**
     * Get notifications by type for a user.
     * 
     * @param userId The user's ID
     * @param type The notification type
     * @return List of notifications of the specified type
     */
    public List<Notification> getNotificationsByType(String userId, String type) {
        logger.info("Retrieving {} notifications for user: {}", type, userId);
        
        List<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        logger.debug("Found {} {} notifications for user", notifications.size(), type);
        
        return notifications;
    }

    /**
     * Clean up old notifications.
     * Deletes notifications older than the specified number of days.
     * 
     * @param daysOld Number of days to keep notifications
     * @return Number of deleted notifications
     */
    public int cleanupOldNotifications(int daysOld) {
        logger.info("Cleaning up notifications older than {} days", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
        
        logger.debug("Deleted {} old notifications", deletedCount);
        return deletedCount;
    }

    /**
     * Broadcast notification via Redis pub/sub.
     * 
     * @param notification The notification to broadcast
     */
    private void broadcastNotificationViaRedis(Notification notification) {
        try {
            String userChannel = "notifications:user:" + notification.getUserId();
            redisTemplate.convertAndSend(userChannel, notification);
            logger.debug("Notification broadcasted via Redis to channel: {}", userChannel);
        } catch (Exception e) {
            logger.error("Failed to broadcast notification via Redis: {}", e.getMessage());
        }
    }

    /**
     * Send notification via WebSocket.
     * 
     * @param notification The notification to send
     */
    private void sendNotificationViaWebSocket(Notification notification) {
        try {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId(),
                    "/queue/notifications",
                    notification
            );
            logger.debug("Notification sent via WebSocket to user: {}", notification.getUserId());
        } catch (Exception e) {
            logger.error("Failed to send notification via WebSocket: {}", e.getMessage());
        }
    }
}
