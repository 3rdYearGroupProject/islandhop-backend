package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Request DTO for approving or rejecting join requests.
 */
@Data
public class ApproveJoinRequestRequest {
    
    private String userId; // User making the decision (group creator/admin)
    
    private String joinRequestId;
    
    private String action; // "approve" or "reject"
    
    private String reason; // Optional reason for rejection
}
