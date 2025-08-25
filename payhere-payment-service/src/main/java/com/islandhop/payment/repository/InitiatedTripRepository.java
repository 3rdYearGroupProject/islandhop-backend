package com.islandhop.payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

/**
 * Repository for accessing initiated trips from trip-initiation-microservice
 */
@Repository
public interface InitiatedTripRepository extends MongoRepository<Map<String, Object>, String> {
    
    /**
     * Find initiated trip by ID
     * @param tripId Trip ID
     * @return Optional Map representing the trip data
     */
    Optional<Map<String, Object>> findById(String tripId);
}
