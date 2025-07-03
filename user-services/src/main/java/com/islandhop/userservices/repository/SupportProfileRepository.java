package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.SupportProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SupportProfileRepository extends JpaRepository<SupportProfile, UUID> {
    Optional<SupportProfile> findByEmail(String email);
}