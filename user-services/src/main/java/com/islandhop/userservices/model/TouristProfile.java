package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "tourist_profiles")
public class TouristProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String nationality;

    @ElementCollection
    @CollectionTable(name = "tourist_profile_languages", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "language")
    private List<String> languages;
}