package com.islandhop.adminservice.dto;

import java.time.Instant;

/**
 * DTO for vehicle type responses.
 */
public class VehicleTypeResponse {

    private Long id;
    private Integer capacity;
    private String description;
    private String fuelType;
    private Boolean isAvailable;
    private Double pricePerKm;
    private String typeName;
    
    public VehicleTypeResponse() {}
    
    public VehicleTypeResponse(Long id, Integer capacity, String description, String fuelType,
                             Boolean isAvailable, Double pricePerKm, String typeName) {
        this.id = id;
        this.capacity = capacity;
        this.description = description;
        this.fuelType = fuelType;
        this.isAvailable = isAvailable;
        this.pricePerKm = pricePerKm;
        this.typeName = typeName;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Double getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }
    
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    // Added missing methods
    public void setVehicleType(String vehicleType) {
        this.typeName = vehicleType;
    }

    public void setAvailable(Boolean available) {
        this.isAvailable = available;
    }

    public void setCreatedAt(Instant createdAt) {
        // Assuming createdAt is a new field
    }

    public void setUpdatedAt(Instant updatedAt) {
        // Assuming updatedAt is a new field
    }
}
