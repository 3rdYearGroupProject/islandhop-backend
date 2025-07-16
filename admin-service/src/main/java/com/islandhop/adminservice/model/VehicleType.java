package com.islandhop.adminservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

/**
 * Entity representing a vehicle type in the system.
 * Maps to the vehicle_types table in the PostgreSQL database.
 */
@Entity
@Table(name = "vehicle_types")
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity")
    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;

    @Column(name = "description")
    private String description;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "is_available", nullable = false)
    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @Column(name = "price_per_km", nullable = false)
    @NotNull(message = "Price per km is required")
    @PositiveOrZero(message = "Price per km must be zero or positive")
    private Double pricePerKm;

    @Column(name = "type_name", nullable = false, unique = true)
    @NotBlank(message = "Type name is required and cannot be blank")
    private String typeName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public VehicleType() {}
    
    public VehicleType(Long id, Integer capacity, String description, String fuelType,
                      Boolean isAvailable, Double pricePerKm, String typeName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.capacity = capacity;
        this.description = description;
        this.fuelType = fuelType;
        this.isAvailable = isAvailable;
        this.pricePerKm = pricePerKm;
        this.typeName = typeName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    public String getVehicleType() {
        return typeName;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
