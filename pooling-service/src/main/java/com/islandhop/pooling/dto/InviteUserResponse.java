package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for user invitation.
 * Follows the same patterns as other response DTOs for consistency.
 */
@Data
public class InviteUserResponse {
    
    private String status;
    
    private String groupId;
    
    private String invitedUserId;
    
    private String message;
}
