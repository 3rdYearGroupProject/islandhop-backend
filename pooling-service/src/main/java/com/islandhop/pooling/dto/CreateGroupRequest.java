package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    
    @NotBlank(message = "Group name is required")
    private String groupName;
    
    private String tripId; // Optional, will create new trip if not provided
    
    @Pattern(regexp = "^(private|public)$", message = "Visibility must be either 'private' or 'public'")
    private String visibility = "private";
    
    private Map<String, Object> preferences; // Optional, for public groups
}
