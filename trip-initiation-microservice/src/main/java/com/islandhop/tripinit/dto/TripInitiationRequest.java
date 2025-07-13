package com.islandhop.tripinit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for trip initiation endpoint.
 * Contains user preferences for driver, guide, and vehicle type.
 */
@Data
public class TripInitiationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Trip ID is required")
    private String tripId;
    
    @NotNull(message = "Set driver flag is required")
    @Min(value = 0, message = "Set driver must be 0 or 1")
    @Max(value = 1, message = "Set driver must be 0 or 1")
    private Integer setDriver;
    
    @NotNull(message = "Set guide flag is required")
    @Min(value = 0, message = "Set guide must be 0 or 1")
    @Max(value = 1, message = "Set guide must be 0 or 1")
    private Integer setGuide;
    
    @NotBlank(message = "Preferred vehicle type ID is required")
    @Pattern(regexp = "\\d+", message = "Vehicle type ID must be a valid number")
    private String preferredVehicleTypeId;
}