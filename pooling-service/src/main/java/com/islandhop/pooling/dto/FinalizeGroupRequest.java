package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Request DTO for finalizing a group with trip cost and logistics details.
 */
@Data
public class FinalizeGroupRequest {
    
    private String groupId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Action is required")
    private String action; // "finalize"
    
    @NotNull(message = "Average driver cost is required")
    @DecimalMin(value = "0.0", message = "Average driver cost must be non-negative")
    private Double averageDriverCost;
    
    @NotNull(message = "Average guide cost is required")
    @DecimalMin(value = "0.0", message = "Average guide cost must be non-negative")
    private Double averageGuideCost;
    
    @NotNull(message = "Total cost is required")
    @DecimalMin(value = "0.0", message = "Total cost must be non-negative")
    private Double totalCost;
    
    @NotNull(message = "Cost per person is required")
    @DecimalMin(value = "0.0", message = "Cost per person must be non-negative")
    private Double costPerPerson;
    
    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;
    
    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;
    
    @NotNull(message = "Need driver flag is required")
    private Boolean needDriver;
    
    @NotNull(message = "Need guide flag is required")
    private Boolean needGuide;
}
