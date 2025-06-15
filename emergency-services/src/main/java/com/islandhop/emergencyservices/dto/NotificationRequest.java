package com.islandhop.emergencyservices.dto;

import com.islandhop.emergencyservices.entity.NotificationType;
import lombok.Data;

@Data
public class NotificationRequest {
    private Long alertId;
    private String recipientId;
    private String message;
    private String topicArn;
    private NotificationType type;
} 