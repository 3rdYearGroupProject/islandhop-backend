package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideProfileRepository extends JpaRepository<GuideProfile, UUID> {
    GuideProfile findByEmail(String email);
    boolean existsByEmail(String email);
    List<GuideProfile> findByProfileCompletion(Integer profileCompletion);
    List<GuideProfile> findByAvailabilityStatus(String availabilityStatus);
    List<GuideProfile> findByBaseLocation(String baseLocation);
    
    @Query("SELECT g FROM GuideProfile g WHERE :language MEMBER OF g.spokenLanguages")
    List<GuideProfile> findBySpokenLanguagesContaining(@Param("language") String language);
    
    @Query("SELECT g FROM GuideProfile g WHERE :specialization MEMBER OF g.specializations")
    List<GuideProfile> findBySpecializationsContaining(@Param("specialization") String specialization);
}