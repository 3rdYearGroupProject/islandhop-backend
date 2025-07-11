package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for join request decision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDecisionResponse {
    
    private String status;
    
    private String groupId;
    
    private String message;
}
