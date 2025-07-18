package com.islandhop.pooling.model;

import lombok.Data;

import java.time.Instant;

/**
 * Represents an individual member's approval or rejection of a join request.
 * Used within JoinRequest to track multi-member approval system.
 */
@Data
public class MemberApproval {
    
    private String memberId;
    
    private String action; // "approve" or "reject"
    
    private String reason; // Optional reason for the decision
    
    private Instant respondedAt;
}
