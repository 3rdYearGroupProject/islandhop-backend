package com.islandhop.tourplanning.repository;

import com.islandhop.tourplanning.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findByUserId(String userId);
    List<Trip> findByIsPublicTrue();
    List<Trip> findByUserIdAndStatus(String userId, String status);
} 