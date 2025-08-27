package com.islandhop.payment.repository;

import com.islandhop.payment.model.PaidTrip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaidTrip entity
 */
@Repository
public interface PaidTripRepository extends MongoRepository<PaidTrip, String> {
    
    /**
     * Find paid trips by user ID
     * @param userId User ID
     * @return List of PaidTrip
     */
    List<PaidTrip> findByUserId(String userId);
    
    /**
     * Find paid trip by trip ID
     * @param id Trip ID
     * @return Optional PaidTrip
     */
    Optional<PaidTrip> findById(String id);
    
    /**
     * Find paid trips by driver status
     * @param driverStatus Driver status
     * @return List of PaidTrip
     */
    List<PaidTrip> findByDriverStatus(String driverStatus);
    
    /**
     * Find paid trips by guide status
     * @param guideStatus Guide status
     * @return List of PaidTrip
     */
    List<PaidTrip> findByGuideStatus(String guideStatus);
    
    /**
     * Find paid trips by base city
     * @param baseCity Base city
     * @return List of PaidTrip
     */
    List<PaidTrip> findByBaseCity(String baseCity);
}
