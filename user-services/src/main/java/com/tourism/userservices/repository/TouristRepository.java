package com.tourism.userservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourism.userservices.entity.Tourist;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, UUID> {
    Optional<Tourist> findByFirebaseUid(String firebaseUid);
    Optional<Tourist> findByEmail(String email);
}