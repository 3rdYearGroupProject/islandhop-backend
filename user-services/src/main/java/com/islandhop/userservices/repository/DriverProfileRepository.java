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
    List<DriverProfile> findByVehicleType(String vehicleType);
    List<DriverProfile> findByAcAvailable(String acAvailable);
    List<DriverProfile> findByNumberOfSeats(Integer numberOfSeats);
    List<DriverProfile> findByBodyType(String bodyType);
    
    @Query("SELECT d FROM DriverProfile d WHERE d.numberOfSeats >= :minSeats")
    List<DriverProfile> findByMinimumSeats(@Param("minSeats") Integer minSeats);
    
    @Query("SELECT d FROM DriverProfile d WHERE d.acAvailable = 'Yes' AND d.vehicleType = :vehicleType")
    List<DriverProfile> findByVehicleTypeWithAC(@Param("vehicleType") String vehicleType);
}