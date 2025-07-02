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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "guide_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuideProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String nicPassport;
    private String nationality;
    private LocalDate dateOfBirth;
    
    private Integer yearsOfExperience;
    
    @ElementCollection
    @CollectionTable(name = "guide_specializations", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "specialization")
    private List<String> specializations;
    
    @ElementCollection
    @CollectionTable(name = "guide_languages", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "language")
    private List<String> spokenLanguages;
    
    private String guideLicenseNumber;
    private String touristBoardRegistration;
    private String baseLocation;
    
    @ElementCollection
    @CollectionTable(name = "guide_service_areas", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "service_area")
    private List<String> serviceAreas;
    
    private String availabilityStatus;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private Double hourlyRate;
    private Double dailyRate;
    
    private String profilePictureUrl;
    
    @Builder.Default
    private Integer profileCompletion = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}