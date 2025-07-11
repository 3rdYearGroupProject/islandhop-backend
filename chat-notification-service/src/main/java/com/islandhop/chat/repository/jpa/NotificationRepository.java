package com.islandhop.chat.repository.jpa;

import com.islandhop.chat.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for Notification entities.
 * Provides CRUD operations and custom queries for user notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user, ordered by creation date (newest first).
     * 
     * @param userId The user's ID
     * @return List of notifications for the user
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find unread notifications for a specific user.
     * 
     * @param userId The user's ID
     * @return List of unread notifications
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    /**
     * Find notifications by type for a specific user.
     * 
     * @param userId The user's ID
     * @param type The notification type
     * @return List of notifications of the specified type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, String type);

    /**
     * Count unread notifications for a specific user.
     * 
     * @param userId The user's ID
     * @return Number of unread notifications
     */
    long countByUserIdAndIsReadFalse(String userId);

    /**
     * Mark all notifications as read for a specific user.
     * 
     * @param userId The user's ID
     * @param readAt The timestamp when marked as read
     * @return Number of updated notifications
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old notifications (older than specified date).
     * 
     * @param cutoffDate The cutoff date for deletion
     * @return Number of deleted notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find notifications by priority for a specific user.
     * 
     * @param userId The user's ID
     * @param priority The notification priority
     * @return List of notifications with the specified priority
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(String userId, String priority);
}
