package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.DriverVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverVehicleRepository extends JpaRepository<DriverVehicle, UUID> {
    Optional<DriverVehicle> findByEmail(String email);
    boolean existsByEmail(String email);
}
