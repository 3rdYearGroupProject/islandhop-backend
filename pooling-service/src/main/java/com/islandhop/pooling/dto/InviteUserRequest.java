package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for inviting a user to a private group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {
    
    @NotBlank(message = "Invited user ID is required")
    private String invitedUserId;
}
