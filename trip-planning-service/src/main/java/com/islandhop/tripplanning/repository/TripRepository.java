package com.islandhop.tripplanning.repository;

import com.islandhop.tripplanning.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
    
    List<Trip> findByUserId(String userId);
    
    List<Trip> findByUserIdAndStatus(String userId, Trip.TripStatus status);
    
    Optional<Trip> findByTripIdAndUserId(String tripId, String userId);
    
    @Query("{'userId': ?0, 'startDate': {$gte: ?1}, 'endDate': {$lte: ?2}}")
    List<Trip> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    @Query("{'categories': {$in: ?0}}")
    List<Trip> findByCategories(List<String> categories);
    
    // For collaborative filtering - find users with similar preferences
    @Query("{'categories': {$in: ?0}, 'pacing': ?1}")
    List<Trip> findSimilarTrips(List<String> categories, Trip.ActivityPacing pacing);
    
    // For pooling service integration
    @Query("{ $or: [ " +
           "  { $and: [ { 'startDate': { $lte: ?1 } }, { 'endDate': { $gte: ?0 } } ] }, " +
           "  { $and: [ { 'startDate': { $lte: ?0 } }, { 'endDate': { $gte: ?1 } } ] } " +
           "] }")
    List<Trip> findTripsInDateRange(LocalDate startDate, LocalDate endDate);
    
    List<Trip> findByBaseCity(String baseCity);
}
