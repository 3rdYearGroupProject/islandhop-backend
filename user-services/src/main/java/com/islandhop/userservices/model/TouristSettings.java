package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing tourist user settings such as currency and units preferences.
 * Stores user-specific configuration for the IslandHop application.
 */
@Data
@Entity
@Table(name = "tourist_settings")
public class TouristSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    @Column(name = "units", length = 20)
    private String units = "Imperial";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
