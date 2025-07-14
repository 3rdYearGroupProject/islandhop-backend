package com.islandhop.adminservice.controller;

import com.islandhop.adminservice.dto.*;
import com.islandhop.adminservice.service.VehicleTypeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Vehicle Type operations.
 * Provides full CRUD operations for vehicle types.
 */
@RestController
@RequestMapping("/admin/vehicle-types")
@CrossOrigin(origins = "*")
public class VehicleTypeController {

    private static final Logger log = LoggerFactory.getLogger(VehicleTypeController.class);
    private final VehicleTypeService vehicleTypeService;
    
    public VehicleTypeController(VehicleTypeService vehicleTypeService) {
        this.vehicleTypeService = vehicleTypeService;
    }

    /**
     * Create a new vehicle type.
     *
     * @param request the create vehicle type request
     * @return response with created vehicle type data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> createVehicleType(
            @Valid @RequestBody CreateVehicleTypeRequest request) {
        
        log.info("POST /admin/vehicle-types - Creating vehicle type: {}", request.getTypeName());
        
        try {
            VehicleTypeResponse response = vehicleTypeService.createVehicleType(request);
            ApiResponse<VehicleTypeResponse> apiResponse = ApiResponse.success(
                    "Vehicle type created successfully", response);
            
            log.info("Successfully created vehicle type with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Bad request while creating vehicle type: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error while creating vehicle type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get all vehicle types.
     *
     * @return response with list of all vehicle types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getAllVehicleTypes() {
        
        log.info("GET /admin/vehicle-types - Retrieving all vehicle types");
        
        try {
            List<VehicleTypeResponse> vehicleTypes = vehicleTypeService.getAllVehicleTypes();
            ApiResponse<List<VehicleTypeResponse>> apiResponse = ApiResponse.success(
                    "Vehicle types retrieved successfully", vehicleTypes);
            
            log.info("Successfully retrieved {} vehicle types", vehicleTypes.size());
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Internal server error while retrieving vehicle types: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get vehicle type by ID.
     *
     * @param id the vehicle type ID
     * @return response with vehicle type data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> getVehicleTypeById(@PathVariable Long id) {
        
        log.info("GET /admin/vehicle-types/{} - Retrieving vehicle type", id);
        
        try {
            VehicleTypeResponse response = vehicleTypeService.getVehicleTypeById(id);
            ApiResponse<VehicleTypeResponse> apiResponse = ApiResponse.success(
                    "Vehicle type retrieved successfully", response);
            
            log.info("Successfully retrieved vehicle type with ID: {}", id);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Vehicle type not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error while retrieving vehicle type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Update an existing vehicle type.
     *
     * @param id the vehicle type ID to update
     * @param request the update vehicle type request
     * @return response with updated vehicle type data
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleTypeResponse>> updateVehicleType(
            @PathVariable Long id, @Valid @RequestBody UpdateVehicleTypeRequest request) {
        
        log.info("PUT /admin/vehicle-types/{} - Updating vehicle type", id);
        
        try {
            VehicleTypeResponse response = vehicleTypeService.updateVehicleType(id, request);
            ApiResponse<VehicleTypeResponse> apiResponse = ApiResponse.success(
                    "Vehicle type updated successfully", response);
            
            log.info("Successfully updated vehicle type with ID: {}", id);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Bad request while updating vehicle type: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error while updating vehicle type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Delete a vehicle type by ID.
     *
     * @param id the vehicle type ID to delete
     * @return response confirming deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleType(@PathVariable Long id) {
        
        log.info("DELETE /admin/vehicle-types/{} - Deleting vehicle type", id);
        
        try {
            vehicleTypeService.deleteVehicleType(id);
            ApiResponse<Void> apiResponse = ApiResponse.success("Vehicle type deleted successfully");
            
            log.info("Successfully deleted vehicle type with ID: {}", id);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Vehicle type not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error while deleting vehicle type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get available vehicle types only.
     *
     * @return response with list of available vehicle types
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getAvailableVehicleTypes() {
        
        log.info("GET /admin/vehicle-types/available - Retrieving available vehicle types");
        
        try {
            List<VehicleTypeResponse> vehicleTypes = vehicleTypeService.getAvailableVehicleTypes();
            ApiResponse<List<VehicleTypeResponse>> apiResponse = ApiResponse.success(
                    "Available vehicle types retrieved successfully", vehicleTypes);
            
            log.info("Successfully retrieved {} available vehicle types", vehicleTypes.size());
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Internal server error while retrieving available vehicle types: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get vehicle types by fuel type.
     *
     * @param fuelType the fuel type to filter by
     * @return response with list of vehicle types with the specified fuel type
     */
    @GetMapping("/fuel-type/{fuelType}")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getVehicleTypesByFuelType(
            @PathVariable String fuelType) {
        
        log.info("GET /admin/vehicle-types/fuel-type/{} - Retrieving vehicle types by fuel type", fuelType);
        
        try {
            List<VehicleTypeResponse> vehicleTypes = vehicleTypeService.getVehicleTypesByFuelType(fuelType);
            ApiResponse<List<VehicleTypeResponse>> apiResponse = ApiResponse.success(
                    "Vehicle types by fuel type retrieved successfully", vehicleTypes);
            
            log.info("Successfully retrieved {} vehicle types with fuel type: {}", vehicleTypes.size(), fuelType);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Internal server error while retrieving vehicle types by fuel type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    /**
     * Get vehicle types within a price range.
     *
     * @param minPrice minimum price per km
     * @param maxPrice maximum price per km
     * @return response with list of vehicle types within the price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> getVehicleTypesByPriceRange(
            @RequestParam Double minPrice, @RequestParam Double maxPrice) {
        
        log.info("GET /admin/vehicle-types/price-range - Retrieving vehicle types within price range: {} - {}", 
                minPrice, maxPrice);
        
        try {
            List<VehicleTypeResponse> vehicleTypes = vehicleTypeService.getVehicleTypesByPriceRange(minPrice, maxPrice);
            ApiResponse<List<VehicleTypeResponse>> apiResponse = ApiResponse.success(
                    "Vehicle types by price range retrieved successfully", vehicleTypes);
            
            log.info("Successfully retrieved {} vehicle types within price range: {} - {}", 
                    vehicleTypes.size(), minPrice, maxPrice);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid price range parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error while retrieving vehicle types by price range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }
}
