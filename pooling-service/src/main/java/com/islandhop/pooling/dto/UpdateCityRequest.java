package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating city in group itinerary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCityRequest {
    
    @NotBlank(message = "City name is required")
    private String city;
}
