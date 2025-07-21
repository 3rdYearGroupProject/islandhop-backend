package com.islandhop.adminservice.repository;

import com.islandhop.adminservice.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for VehicleType entity operations.
 * Provides CRUD operations and custom queries for the vehicle_types table.
 */
@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {

    /**
     * Find vehicle type by type name (case insensitive).
     *
     * @param typeName the type name to search for
     * @return Optional containing the vehicle type if found
     */
    Optional<VehicleType> findByTypeNameIgnoreCase(String typeName);

    /**
     * Check if a vehicle type exists with the given type name (case insensitive).
     *
     * @param typeName the type name to check
     * @return true if exists, false otherwise
     */
    boolean existsByTypeNameIgnoreCase(String typeName);

    /**
     * Find all available vehicle types.
     *
     * @return List of available vehicle types
     */
    List<VehicleType> findByIsAvailableTrue();

    /**
     * Find vehicle types by fuel type.
     *
     * @param fuelType the fuel type to search for
     * @return List of vehicle types with the specified fuel type
     */
    List<VehicleType> findByFuelTypeIgnoreCase(String fuelType);

    /**
     * Find vehicle types within a price range.
     *
     * @param minPrice minimum price per km
     * @param maxPrice maximum price per km
     * @return List of vehicle types within the price range
     */
    @Query("SELECT v FROM VehicleType v WHERE v.pricePerKm BETWEEN :minPrice AND :maxPrice")
    List<VehicleType> findByPricePerKmBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Find vehicle types by minimum capacity.
     *
     * @param capacity minimum capacity required
     * @return List of vehicle types with capacity greater than or equal to the specified value
     */
    List<VehicleType> findByCapacityGreaterThanEqual(Integer capacity);
}
