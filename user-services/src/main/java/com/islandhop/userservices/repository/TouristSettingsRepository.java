package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.TouristSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing TouristSettings entities.
 * Provides CRUD operations for tourist user settings.
 */
@Repository
public interface TouristSettingsRepository extends JpaRepository<TouristSettings, UUID> {
    
    /**
     * Find tourist settings by email address.
     * 
     * @param email The email address to search for
     * @return Optional containing TouristSettings if found, empty otherwise
     */
    Optional<TouristSettings> findByEmail(String email);
    
    /**
     * Check if settings exist for the given email.
     * 
     * @param email The email address to check
     * @return true if settings exist, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Delete settings by email address.
     * 
     * @param email The email address of settings to delete
     */
    void deleteByEmail(String email);
}
