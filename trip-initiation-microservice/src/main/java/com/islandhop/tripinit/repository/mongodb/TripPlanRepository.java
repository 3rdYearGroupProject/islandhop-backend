package com.islandhop.tripinit.repository.mongodb;

import com.islandhop.tripinit.model.mongodb.TripPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for trip plans in MongoDB.
 */
@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {
    Optional<TripPlan> findByIdAndUserId(String id, String userId);
}