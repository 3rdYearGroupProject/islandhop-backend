package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String fullName;
    private String contactNumber;
    private String nicPassport;
    
    private String vehicleType;
    private String vehicleNumber;
    private String bodyType;
    private String acAvailable;
    private Integer numberOfSeats;
    
    private String profilePictureUrl;
    
    @Builder.Default
    private Integer profileCompletion = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}