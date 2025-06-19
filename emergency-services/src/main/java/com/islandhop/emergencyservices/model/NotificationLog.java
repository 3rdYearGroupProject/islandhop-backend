package com.islandhop.emergencyservices.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationLog {
    private Long id;
    private Long alertId;
    private String recipientId;
    private NotificationType type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
