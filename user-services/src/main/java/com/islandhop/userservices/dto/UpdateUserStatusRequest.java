package com.islandhop.userservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for updating user account status
 * Used by support/admin users to manage user accounts
 */
@Data
public class UpdateUserStatusRequest {
    
    /**
     * Email of the user whose status needs to be updated
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    /**
     * New status for the user account
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|DEACTIVATED|SUSPENDED|PENDING)$", 
             message = "Status must be one of: ACTIVE, DEACTIVATED, SUSPENDED, PENDING")
    private String status;
}