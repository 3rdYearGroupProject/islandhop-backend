package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideProfileRepository extends JpaRepository<GuideProfile, UUID> {
    GuideProfile findByEmail(String email);
    boolean existsByEmail(String email);
    List<GuideProfile> findByProfileCompletion(Integer profileCompletion);
}
