package com.islandhop.emergencyservices.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "recipient_id")
    private String recipientId;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "read")
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;
} 