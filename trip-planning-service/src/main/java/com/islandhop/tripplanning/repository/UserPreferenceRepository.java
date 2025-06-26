package com.islandhop.tripplanning.repository;

import com.islandhop.tripplanning.model.UserPreferenceProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreferenceProfile, String> {
    
    Optional<UserPreferenceProfile> findByUserId(String userId);
}
