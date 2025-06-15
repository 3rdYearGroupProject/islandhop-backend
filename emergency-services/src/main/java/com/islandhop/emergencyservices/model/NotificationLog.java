package com.islandhop.emergencyservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long alertId;

    @Column(nullable = false)
    private String recipientId;

    @Column(nullable = false)
    private LocalDateTime deliveryTime;

    @Column(nullable = false)
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @PrePersist
    protected void onCreate() {
        deliveryTime = LocalDateTime.now();
        read = false;
    }
} 