package com.islandhop.tripinit.model.postgresql;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Guide fee entity for PostgreSQL database.
 * Stores guide pricing information per city per day.
 */
@Entity
@Table(name = "guide_fees")
@Data
public class GuideFee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;
}