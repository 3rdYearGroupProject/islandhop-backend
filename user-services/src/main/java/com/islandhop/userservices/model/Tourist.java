package com.islandhop.userservices.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "tourists")
public class Tourist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(unique = true)
    private String firebaseUid;
    
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String nationality;
    
    @ElementCollection
    @CollectionTable(name = "tourist_languages", joinColumns = @JoinColumn(name = "tourist_id"))
    @Column(name = "language")
    private List<String> languages;
    
    @NotNull
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private TouristStatus status = TouristStatus.ACTIVE;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 