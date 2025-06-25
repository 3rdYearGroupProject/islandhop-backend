package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, UUID> {
    Optional<Tourist> findByFirebaseUid(String firebaseUid);
    Optional<Tourist> findByEmail(String email);
    boolean existsByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
} 