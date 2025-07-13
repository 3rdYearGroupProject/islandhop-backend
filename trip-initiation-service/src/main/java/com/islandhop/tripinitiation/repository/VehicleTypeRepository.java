package com.islandhop.tripinitiation.repository;

import com.islandhop.tripinitiation.model.postgres.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {
    // Additional query methods can be defined here if needed
}