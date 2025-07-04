package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.TouristProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TouristProfileRepository extends JpaRepository<TouristProfile, UUID> {
    TouristProfile findByEmail(String email);
    boolean existsByEmail(String email);
}