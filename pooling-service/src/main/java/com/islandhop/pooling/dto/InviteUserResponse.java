package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user invitation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserResponse {
    
    private String status;
    
    private String groupId;
    
    private String message;
}
