package com.islandhop.pooling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for inviting a user to a private group.
 * Supports both user ID and email-based invitations.
 */
@Data
public class InviteUserRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String invitedUserId; // Optional, for direct user ID invitations
    
    @Email(message = "Valid email address is required")
    private String invitedEmail; // For email-based invitations
    
    private String message; // Optional invitation message
    
    private int expirationDays = 7; // Invitation expires in 7 days by default
}
