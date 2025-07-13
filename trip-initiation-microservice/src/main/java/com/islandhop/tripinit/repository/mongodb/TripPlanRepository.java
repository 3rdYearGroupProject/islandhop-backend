package com.islandhop.tripinit.repository.mongodb;

import com.islandhop.tripinit.model.mongodb.TripPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {
    TripPlan findByUserIdAndTripId(String userId, String tripId);
}