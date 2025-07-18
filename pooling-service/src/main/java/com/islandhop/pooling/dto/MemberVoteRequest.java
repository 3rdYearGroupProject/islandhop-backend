package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for a group member to vote on a join request.
 * Allows each member to approve or reject individually.
 */
@Data
public class MemberVoteRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId; // The member casting the vote
    
    @NotBlank(message = "Join request ID is required")
    private String joinRequestId;
    
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(approve|reject)$", message = "Action must be either 'approve' or 'reject'")
    private String action; // "approve" or "reject"
    
    private String reason; // Optional reason for the decision
}
