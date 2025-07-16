package com.islandhop.adminservice.service;

import com.islandhop.adminservice.dto.CreateVehicleTypeRequest;
import com.islandhop.adminservice.dto.UpdateVehicleTypeRequest;
import com.islandhop.adminservice.dto.VehicleTypeResponse;
import com.islandhop.adminservice.model.VehicleType;
import com.islandhop.adminservice.repository.VehicleTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for vehicle type operations.
 * Handles business logic, validation, and data transformation.
 */
@Service
@Transactional
public class VehicleTypeService {

    private static final Logger log = LoggerFactory.getLogger(VehicleTypeService.class);
    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeService(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    /**
     * Create a new vehicle type.
     *
     * @param request the create request containing vehicle type details
     * @return the created vehicle type response
     * @throws IllegalArgumentException if vehicle type name already exists
     */
    public VehicleTypeResponse createVehicleType(CreateVehicleTypeRequest request) {
        log.debug("Creating new vehicle type with name: {}", request.getTypeName());

        // Check if vehicle type with same name already exists
        if (vehicleTypeRepository.existsByTypeNameIgnoreCase(request.getTypeName())) {
            log.warn("Vehicle type with name '{}' already exists", request.getTypeName());
            throw new IllegalArgumentException("Vehicle type with name '" + request.getTypeName() + "' already exists");
        }

        try {
            VehicleType vehicleType = mapToEntity(request);
            VehicleType savedVehicleType = vehicleTypeRepository.save(vehicleType);
            
            log.info("Successfully created vehicle type with ID: {} and name: {}", 
                    savedVehicleType.getId(), savedVehicleType.getTypeName());
            
            return mapToResponse(savedVehicleType);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating vehicle type: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid data provided for vehicle type creation", e);
        }
    }

    /**
     * Get all vehicle types.
     *
     * @return list of all vehicle types
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getAllVehicleTypes() {
        log.debug("Retrieving all vehicle types");
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        log.info("Found {} vehicle types", vehicleTypes.size());
        
        return vehicleTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle type by ID.
     *
     * @param id the vehicle type ID
     * @return the vehicle type response
     * @throws IllegalArgumentException if vehicle type not found
     */
    @Transactional(readOnly = true)
    public VehicleTypeResponse getVehicleTypeById(Long id) {
        log.debug("Retrieving vehicle type with ID: {}", id);
        
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle type with ID {} not found", id);
                    return new IllegalArgumentException("Vehicle type with ID " + id + " not found");
                });
        
        log.info("Successfully retrieved vehicle type with ID: {}", id);
        return mapToResponse(vehicleType);
    }

    /**
     * Update an existing vehicle type.
     *
     * @param id the vehicle type ID to update
     * @param request the update request containing new vehicle type details
     * @return the updated vehicle type response
     * @throws IllegalArgumentException if vehicle type not found or name already exists
     */
    public VehicleTypeResponse updateVehicleType(Long id, UpdateVehicleTypeRequest request) {
        log.debug("Updating vehicle type with ID: {}", id);
        
        VehicleType existingVehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle type with ID {} not found for update", id);
                    return new IllegalArgumentException("Vehicle type with ID " + id + " not found");
                });

        // Check if the new type name conflicts with existing ones (excluding current record)
        if (!existingVehicleType.getTypeName().equalsIgnoreCase(request.getTypeName()) &&
            vehicleTypeRepository.existsByTypeNameIgnoreCase(request.getTypeName())) {
            log.warn("Vehicle type with name '{}' already exists during update", request.getTypeName());
            throw new IllegalArgumentException("Vehicle type with name '" + request.getTypeName() + "' already exists");
        }

        try {
            // Update fields
            existingVehicleType.setCapacity(request.getCapacity());
            existingVehicleType.setDescription(request.getDescription());
            existingVehicleType.setFuelType(request.getFuelType());
            existingVehicleType.setIsAvailable(request.getIsAvailable());
            existingVehicleType.setPricePerKm(request.getPricePerKm());
            existingVehicleType.setTypeName(request.getTypeName());

            VehicleType updatedVehicleType = vehicleTypeRepository.save(existingVehicleType);
            
            log.info("Successfully updated vehicle type with ID: {}", id);
            return mapToResponse(updatedVehicleType);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating vehicle type: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid data provided for vehicle type update", e);
        }
    }

    /**
     * Delete a vehicle type by ID.
     *
     * @param id the vehicle type ID to delete
     * @throws IllegalArgumentException if vehicle type not found
     */
    public void deleteVehicleType(Long id) {
        log.debug("Deleting vehicle type with ID: {}", id);
        
        if (!vehicleTypeRepository.existsById(id)) {
            log.warn("Vehicle type with ID {} not found for deletion", id);
            throw new IllegalArgumentException("Vehicle type with ID " + id + " not found");
        }

        vehicleTypeRepository.deleteById(id);
        log.info("Successfully deleted vehicle type with ID: {}", id);
    }

    /**
     * Get available vehicle types only.
     *
     * @return list of available vehicle types
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getAvailableVehicleTypes() {
        log.debug("Retrieving available vehicle types");
        
        List<VehicleType> availableVehicleTypes = vehicleTypeRepository.findByIsAvailableTrue();
        log.info("Found {} available vehicle types", availableVehicleTypes.size());
        
        return availableVehicleTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle types by fuel type.
     *
     * @param fuelType the fuel type to filter by
     * @return list of vehicle types with the specified fuel type
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getVehicleTypesByFuelType(String fuelType) {
        log.debug("Retrieving vehicle types with fuel type: {}", fuelType);
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByFuelTypeIgnoreCase(fuelType);
        log.info("Found {} vehicle types with fuel type: {}", vehicleTypes.size(), fuelType);
        
        return vehicleTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle types within a price range.
     *
     * @param minPrice minimum price per km
     * @param maxPrice maximum price per km
     * @return list of vehicle types within the price range
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getVehicleTypesByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Retrieving vehicle types with price range: {} - {}", minPrice, maxPrice);
        
        if (minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByPricePerKmBetween(minPrice, maxPrice);
        log.info("Found {} vehicle types within price range: {} - {}", vehicleTypes.size(), minPrice, maxPrice);
        
        return vehicleTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map CreateVehicleTypeRequest to VehicleType entity.
     */
    private VehicleType mapToEntity(CreateVehicleTypeRequest request) {
        return new VehicleType(
                null, // ID will be generated
                request.getCapacity(),
                request.getDescription(),
                request.getFuelType(),
                request.getIsAvailable(),
                request.getPricePerKm(),
                request.getTypeName(),
                Instant.now(), // createdAt
                Instant.now()  // updatedAt
        );
    }

    /**
     * Map VehicleType entity to VehicleTypeResponse.
     */
    private VehicleTypeResponse mapToResponse(VehicleType vehicleType) {
        return new VehicleTypeResponse(
                vehicleType.getId(),
                vehicleType.getCapacity(),
                vehicleType.getDescription(),
                vehicleType.getFuelType(),
                vehicleType.getIsAvailable(),
                vehicleType.getPricePerKm(),
                vehicleType.getTypeName()
        );
    }
}
