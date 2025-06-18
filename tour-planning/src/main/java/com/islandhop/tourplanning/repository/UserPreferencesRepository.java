package com.islandhop.tourplanning.repository;

import com.islandhop.tourplanning.model.UserPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepository extends MongoRepository<UserPreferences, String> {
    UserPreferences findByUserId(String userId);
} 