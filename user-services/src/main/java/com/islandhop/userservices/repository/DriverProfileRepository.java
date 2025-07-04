package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    DriverProfile findByEmail(String email);
    boolean existsByEmail(String email);
    List<DriverProfile> findByProfileCompletion(Integer profileCompletion);
    List<DriverProfile> findByRatingGreaterThanEqual(Double rating);
    List<DriverProfile> findByAcceptPartialTrips(Integer acceptPartialTrips);
    List<DriverProfile> findByAutoAcceptTrips(Integer autoAcceptTrips);
    List<DriverProfile> findByDrivingLicenseVerified(Integer verified);
    List<DriverProfile> findBySltdaLicenseVerified(Integer verified);
    
    @Query("SELECT d FROM DriverProfile d WHERE d.rating >= :minRating AND d.profileCompletion = 1")
    List<DriverProfile> findVerifiedDriversWithMinRating(@Param("minRating") Double minRating);
    
    @Query("SELECT d FROM DriverProfile d WHERE d.drivingLicenseVerified = 1 AND d.sltdaLicenseVerified = 1")
    List<DriverProfile> findFullyVerifiedDrivers();
    
    @Query("SELECT d FROM DriverProfile d WHERE d.maximumTripDistance >= :distance")
    List<DriverProfile> findByMinimumTripDistance(@Param("distance") Integer distance);
}