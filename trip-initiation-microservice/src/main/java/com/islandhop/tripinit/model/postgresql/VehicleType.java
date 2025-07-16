package com.islandhop.tripinit.model.postgresql;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Vehicle type entity for PostgreSQL database.
 * Stores vehicle information with pricing per kilometer.
 */
@Entity
@Table(name = "vehicle_types")
@Data
public class VehicleType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "type_name", nullable = false)
    private String typeName;
    
    @Column(name = "price_per_km", nullable = false)
    private Double pricePerKm;
}