package com.islandhop.trip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating city in a daily plan.
 * Used by the POST /api/v1/itinerary/{tripId}/day/{day}/city endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCityRequest {
    
    /**
     * User ID to verify trip ownership
     */
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    /**
     * City name to set for the specified day
     */
    @NotBlank(message = "City cannot be blank")
    private String city;
}
