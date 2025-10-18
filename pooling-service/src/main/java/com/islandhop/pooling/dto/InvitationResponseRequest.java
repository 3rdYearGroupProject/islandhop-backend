package com.islandhop.pooling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for responding to an invitation.
 */
@Data
public class InvitationResponseRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId; // Firebase UID of the user responding
    
    @NotBlank(message = "Invitation ID is required")
    private String invitationId;
    
    @Email(message = "Valid email address is required")
    @NotBlank(message = "User email is required")
    private String userEmail; // Email of the user responding (to verify and fetch profile)
    
    @NotBlank(message = "Action is required (accept or reject)")
    private String action; // "accept" or "reject"
    
    private String message; // Optional message when rejecting
}
