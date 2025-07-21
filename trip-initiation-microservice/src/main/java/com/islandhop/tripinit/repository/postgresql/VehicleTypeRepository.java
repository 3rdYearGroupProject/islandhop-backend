package com.islandhop.tripinit.repository.postgresql;

import com.islandhop.tripinit.model.postgresql.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for vehicle types in PostgreSQL.
 */
@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    Optional<VehicleType> findById(Long id);
}