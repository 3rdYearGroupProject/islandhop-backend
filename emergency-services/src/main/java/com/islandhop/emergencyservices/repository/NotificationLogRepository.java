package com.islandhop.emergencyservices.repository;

import com.islandhop.emergencyservices.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByAlertId(Long alertId);
    List<NotificationLog> findByRecipientId(String recipientId);
    List<NotificationLog> findByAlertIdAndRead(Long alertId, boolean read);
} 