package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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
    
    // Personal Information
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String emergencyContactName;
    private String emergencyContactNumber;
    
    // Profile Picture
    @Column(columnDefinition = "TEXT")
    private String profilePictureUrl;
    
    // Driving License
    @Column(columnDefinition = "TEXT")
    private String drivingLicenseImage;
    private String drivingLicenseNumber;
    private LocalDate drivingLicenseExpiryDate;
    private LocalDate drivingLicenseUploadedDate;
    @Builder.Default
    private Integer drivingLicenseVerified = 0; // 0 = not verified, 1 = verified
    
    // SLTDA License
    @Column(columnDefinition = "TEXT")
    private String sltdaLicenseImage;
    private String sltdaLicenseNumber;
    private LocalDate sltdaLicenseExpiryDate;
    private LocalDate sltdaLicenseUploadedDate;
    @Builder.Default
    private Integer sltdaLicenseVerified = 0; // 0 = not verified, 1 = verified
    
    // Trip Preferences
    @Builder.Default
    private Integer acceptPartialTrips = 0; // 0 = no, 1 = yes
    @Builder.Default
    private Integer autoAcceptTrips = 0; // 0 = no, 1 = yes
    private Integer maximumTripDistance;
    
    // Driver Statistics
    @Builder.Default
    private Double rating = 0.0;
    @Builder.Default
    private Integer numberOfReviews = 0;
    @Builder.Default
    private Integer totalCompletedTrips = 0;
    
    // Profile Completion Status
    @Builder.Default
    private Integer profileCompletion = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}