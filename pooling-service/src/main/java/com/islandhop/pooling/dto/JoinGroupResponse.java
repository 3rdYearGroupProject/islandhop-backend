package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Response DTO for join group request.
 * Follows the same patterns as other response DTOs for consistency.
 */
@Data
public class JoinGroupResponse {
    
    private String status;
    
    private String groupId;
    
    private String message;
}
