package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for joining a public group.
 * Follows the same patterns as other request DTOs for consistency.
 */
@Data
public class JoinGroupRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private Map<String, Object> userProfile;
}
