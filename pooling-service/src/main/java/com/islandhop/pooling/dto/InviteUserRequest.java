package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for inviting a user to a private group.
 * Follows the same patterns as other request DTOs for consistency.
 */
@Data
public class InviteUserRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Invited user ID is required")
    private String invitedUserId;
}
