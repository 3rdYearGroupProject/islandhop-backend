package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Request DTO for responding to an invitation.
 */
@Data
public class InvitationResponseRequest {
    
    private String userId;
    
    private String invitationId;
    
    private String action; // "accept" or "reject"
    
    private String message; // Optional message when rejecting
}
