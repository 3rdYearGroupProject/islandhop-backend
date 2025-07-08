package com.islandhop.trip.repository;

import com.islandhop.trip.model.TripPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository interface for TripPlan entities.
 * Provides CRUD operations and custom query methods for trip plans.
 */
@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {

    /**
     * Find all trip plans for a specific user.
     *
     * @param userId The user ID to search for
     * @return List of trip plans belonging to the user
     */
    List<TripPlan> findByUserId(String userId);

    /**
     * Check if a trip plan exists for a specific user and trip name.
     *
     * @param userId   The user ID
     * @param tripName The trip name
     * @return true if a trip plan exists, false otherwise
     */
    boolean existsByUserIdAndTripName(String userId, String tripName);
}
