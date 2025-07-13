package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for creating a new group.
 * Follows the same patterns as CreateTripRequest for consistency.
 */
@Data
public class CreateGroupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Trip ID is required")
    private String tripId; // Required - groups are always associated with trips
    
    @Pattern(regexp = "^(private|public)$", message = "Visibility must be either 'private' or 'public'")
    private String visibility = "private";
    
    private Map<String, Object> preferences; // Optional, for public groups
}
