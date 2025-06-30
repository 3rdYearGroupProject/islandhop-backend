package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "support_profiles")
public class SupportProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String contactNo;
    private String address;
    
    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture; // Base64 encoded image data
}