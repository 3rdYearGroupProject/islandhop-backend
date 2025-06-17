package com.islandhop.emergencyservices.service.impl;

import com.islandhop.emergencyservices.dto.NotificationRequest;
import com.islandhop.emergencyservices.model.NotificationLog;
import com.islandhop.emergencyservices.model.EmergencyAlert;
import com.islandhop.emergencyservices.repository.NotificationLogRepository;
import com.islandhop.emergencyservices.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final SnsClient snsClient;

    @Override
    public NotificationLog sendNotification(NotificationRequest request) {
        log.info("Sending notification: {}", request);
        
        // Create notification log
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setAlertId(request.getAlertId());
        notificationLog.setRecipientId(request.getRecipientId());
        notificationLog.setType(request.getType());
        
        // Send notification via SNS
        try {
            PublishRequest snsRequest = PublishRequest.builder()
                .topicArn(request.getTopicArn())
                .message(request.getMessage())
                .build();
            
            snsClient.publish(snsRequest);
            notificationLog.setCreatedAt(LocalDateTime.now());
            notificationLog.setRead(false);
            
            return notificationLogRepository.save(notificationLog);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Override
    public void sendEmergencyAlert(Long alertId, String message) {
        log.info("Sending emergency alert for alert ID: {}", alertId);
        
        // Send emergency alert via SNS
        try {
            PublishRequest snsRequest = PublishRequest.builder()
                .topicArn("arn:aws:sns:us-east-1:123456789012:emergency-alerts")
                .message(message)
                .build();
            
            snsClient.publish(snsRequest);
        } catch (Exception e) {
            log.error("Failed to send emergency alert: {}", e.getMessage());
            throw new RuntimeException("Failed to send emergency alert", e);
        }
    }

    @Override
    public void sendEmergencyNotifications(EmergencyAlert alert) {
        log.info("Sending emergency notifications for alert: {}", alert);
        
        // Create notification request
        NotificationRequest request = new NotificationRequest();
        request.setAlertId(alert.getId());
        request.setMessage(String.format("Emergency alert at location: %s, %s. Triggered by: %s", 
            alert.getLatitude(), alert.getLongitude(), alert.getTriggeredBy()));
        request.setTopicArn("arn:aws:sns:us-east-1:123456789012:emergency-alerts");
        
        // Send notification
        sendNotification(request);
    }
} 