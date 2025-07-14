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
     * Create a new vehicle type
     */
    public VehicleTypeResponse createVehicleType(CreateVehicleTypeRequest request) {
        log.debug("Creating new vehicle type: {}", request.getVehicleType());
        
        try {
            VehicleType vehicleType = new VehicleType();
            vehicleType.setVehicleType(request.getVehicleType());
            vehicleType.setDescription(request.getDescription());
            vehicleType.setFuelType(request.getFuelType());
            vehicleType.setCapacity(request.getCapacity());
            vehicleType.setPricePerKm(request.getPricePerKm());
            vehicleType.setAvailable(request.getAvailable());
            
            VehicleType saved = vehicleTypeRepository.save(vehicleType);
            log.info("Vehicle type created successfully with ID: {}", saved.getId());
            
            return convertToResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating vehicle type", e);
            throw new RuntimeException("Vehicle type with this name already exists", e);
        }
    }

    /**
     * Get all vehicle types
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getAllVehicleTypes() {
        log.debug("Fetching all vehicle types");
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        log.info("Found {} vehicle types", vehicleTypes.size());
        
        return vehicleTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle type by ID
     */
    @Transactional(readOnly = true)
    public VehicleTypeResponse getVehicleTypeById(Long id) {
        log.debug("Fetching vehicle type with ID: {}", id);
        
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle type not found with ID: " + id));
        
        log.info("Found vehicle type: {}", vehicleType.getVehicleType());
        return convertToResponse(vehicleType);
    }

    /**
     * Update vehicle type
     */
    public VehicleTypeResponse updateVehicleType(Long id, UpdateVehicleTypeRequest request) {
        log.debug("Updating vehicle type with ID: {}", id);
        
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle type not found with ID: " + id));
        
        // Update fields
        vehicleType.setVehicleType(request.getVehicleType());
        vehicleType.setDescription(request.getDescription());
        vehicleType.setFuelType(request.getFuelType());
        vehicleType.setCapacity(request.getCapacity());
        vehicleType.setPricePerKm(request.getPricePerKm());
        vehicleType.setAvailable(request.getAvailable());
        
        try {
            VehicleType updated = vehicleTypeRepository.save(vehicleType);
            log.info("Vehicle type updated successfully with ID: {}", updated.getId());
            
            return convertToResponse(updated);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating vehicle type", e);
            throw new RuntimeException("Vehicle type with this name already exists", e);
        }
    }

    /**
     * Delete vehicle type
     */
    public void deleteVehicleType(Long id) {
        log.debug("Deleting vehicle type with ID: {}", id);
        
        if (!vehicleTypeRepository.existsById(id)) {
            throw new RuntimeException("Vehicle type not found with ID: " + id);
        }
        
        vehicleTypeRepository.deleteById(id);
        log.info("Vehicle type deleted successfully with ID: {}", id);
    }

    /**
     * Get available vehicle types
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getAvailableVehicleTypes() {
        log.debug("Fetching available vehicle types");
        
        List<VehicleType> availableTypes = vehicleTypeRepository.findByAvailableTrue();
        log.info("Found {} available vehicle types", availableTypes.size());
        
        return availableTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle types by fuel type
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getVehicleTypesByFuelType(String fuelType) {
        log.debug("Fetching vehicle types with fuel type: {}", fuelType);
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByFuelType(fuelType);
        log.info("Found {} vehicle types with fuel type: {}", vehicleTypes.size(), fuelType);
        
        return vehicleTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle types by capacity range
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getVehicleTypesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        log.debug("Fetching vehicle types with capacity between {} and {}", minCapacity, maxCapacity);
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByCapacityBetween(minCapacity, maxCapacity);
        log.info("Found {} vehicle types in capacity range", vehicleTypes.size());
        
        return vehicleTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle types by price range
     */
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> getVehicleTypesByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Fetching vehicle types with price between {} and {}", minPrice, maxPrice);
        
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByPricePerKmBetween(minPrice, maxPrice);
        log.info("Found {} vehicle types in price range", vehicleTypes.size());
        
        return vehicleTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert VehicleType entity to VehicleTypeResponse DTO
     */
    private VehicleTypeResponse convertToResponse(VehicleType vehicleType) {
        VehicleTypeResponse response = new VehicleTypeResponse();
        response.setId(vehicleType.getId());
        response.setVehicleType(vehicleType.getVehicleType());
        response.setDescription(vehicleType.getDescription());
        response.setFuelType(vehicleType.getFuelType());
        response.setCapacity(vehicleType.getCapacity());
        response.setPricePerKm(vehicleType.getPricePerKm());
        response.setAvailable(vehicleType.getAvailable());
        response.setCreatedAt(vehicleType.getCreatedAt());
        response.setUpdatedAt(vehicleType.getUpdatedAt());
        return response;
    }
}
