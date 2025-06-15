package com.islandhop.emergencyservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "emergency_alerts")
public class EmergencyAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private String triggeredBy; // USER, DRIVER, or GUIDE

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        status = AlertStatus.PENDING;
    }
} 