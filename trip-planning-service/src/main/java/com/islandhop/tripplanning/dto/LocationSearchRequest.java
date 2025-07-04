package com.islandhop.tripplanning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Request DTO for location search operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchRequest {
    
    @NotEmpty(message = "Search query cannot be empty")
    private String query;
    
    private String city;
    
    // Optional bias location for proximity-based results
    private Double biasLatitude;
    private Double biasLongitude;
    
    @Min(value = 1, message = "Maximum results must be at least 1")
    @Max(value = 50, message = "Maximum results cannot exceed 50")
    private Integer maxResults = 10;
    
    // Search filters
    private String placeType; // "restaurant", "hotel", "attraction", etc.
    private Double radiusKm; // Search radius in kilometers
    private Boolean sriLankaOnly = true; // Restrict to Sri Lanka only
}
