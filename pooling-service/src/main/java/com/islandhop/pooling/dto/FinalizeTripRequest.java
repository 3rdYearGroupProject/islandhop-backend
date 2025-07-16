package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for finalizing a trip with suggestions.
 */
@Data
public class FinalizeTripRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Action is required")
    private String action; // "finalize" or "join"
    
    private String targetGroupId; // Required if action is "join"
}
