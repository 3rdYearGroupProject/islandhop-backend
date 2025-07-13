package com.islandhop.tripinit.repository.postgresql;

import com.islandhop.tripinit.model.postgresql.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {
    // Additional query methods can be defined here if needed
}