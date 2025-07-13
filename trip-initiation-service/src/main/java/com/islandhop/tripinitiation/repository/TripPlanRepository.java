package com.islandhop.tripinitiation.repository;

import com.islandhop.tripinitiation.model.mongo.TripPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {
    TripPlan findByUserIdAndId(String userId, String tripId);
}