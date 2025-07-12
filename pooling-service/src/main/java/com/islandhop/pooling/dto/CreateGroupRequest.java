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
    
    @NotBlank(message = "Group name is required")
    private String groupName;
    
    private String tripId; // Optional, will create new trip if not provided
    
    @Pattern(regexp = "^(private|public)$", message = "Visibility must be either 'private' or 'public'")
    private String visibility = "private";
    
    private Map<String, Object> preferences; // Optional, for public groups
}
