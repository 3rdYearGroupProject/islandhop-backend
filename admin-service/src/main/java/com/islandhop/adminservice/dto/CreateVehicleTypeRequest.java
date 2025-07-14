package com.islandhop.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for creating a new vehicle type.
 */
public class CreateVehicleTypeRequest {

    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;

    private String description;

    private String fuelType;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @NotNull(message = "Price per km is required")
    @PositiveOrZero(message = "Price per km must be zero or positive")
    private Double pricePerKm;

    @NotBlank(message = "Type name is required and cannot be blank")
    private String typeName;
    
    public CreateVehicleTypeRequest() {}
    
    public CreateVehicleTypeRequest(Integer capacity, String description, String fuelType, 
                                  Boolean isAvailable, Double pricePerKm, String typeName) {
        this.capacity = capacity;
        this.description = description;
        this.fuelType = fuelType;
        this.isAvailable = isAvailable;
        this.pricePerKm = pricePerKm;
        this.typeName = typeName;
    }
    
    // Getters and Setters
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
    public String getVehicleType() {
        return typeName;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }
}
