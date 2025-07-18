package com.islandhop.pooling.model;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a join request for a public group.
 * Nested within Group entity.
 * Follows consistent patterns with other model classes.
 * Enhanced with multi-member approval system.
 */
@Data
public class JoinRequest {
    
    private String id; // Unique ID for this join request
    
    private String userId;
    
    private String userEmail;
    
    private String userName;
    
    private Map<String, Object> userProfile;
    
    private String status; // "pending", "approved", "rejected"
    
    private String message; // Optional message from user when requesting to join
    
    private String rejectionReason; // Reason for rejection if applicable
    
    private Instant requestedAt;
    
    private Instant respondedAt;
    
    private String reviewedByUserId; // ID of the user who approved/rejected (for backward compatibility)
    
    // Multi-member approval system
    private List<MemberApproval> memberApprovals = new ArrayList<>(); // Track individual member approvals
    
    private boolean requiresAllMemberApproval = true; // Whether all members must approve
    
    /**
     * Check if the join request is pending.
     */
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    /**
     * Check if the join request is approved.
     */
    public boolean isApproved() {
        return "approved".equals(status);
    }
    
    /**
     * Check if the join request is rejected.
     */
    public boolean isRejected() {
        return "rejected".equals(status);
    }
    
    /**
     * Add a member's approval or rejection.
     */
    public void addMemberApproval(String memberId, String action, String reason) {
        // Remove existing approval from the same member
        memberApprovals.removeIf(approval -> approval.getMemberId().equals(memberId));
        
        MemberApproval approval = new MemberApproval();
        approval.setMemberId(memberId);
        approval.setAction(action); // "approve" or "reject"
        approval.setReason(reason);
        approval.setRespondedAt(Instant.now());
        
        memberApprovals.add(approval);
    }
    
    /**
     * Check if a specific member has approved this request.
     */
    public boolean hasMemberApproved(String memberId) {
        return memberApprovals.stream()
                .anyMatch(approval -> approval.getMemberId().equals(memberId) && "approve".equals(approval.getAction()));
    }
    
    /**
     * Check if a specific member has rejected this request.
     */
    public boolean hasMemberRejected(String memberId) {
        return memberApprovals.stream()
                .anyMatch(approval -> approval.getMemberId().equals(memberId) && "reject".equals(approval.getAction()));
    }
    
    /**
     * Check if a specific member has responded (either approved or rejected).
     */
    public boolean hasMemberResponded(String memberId) {
        return memberApprovals.stream()
                .anyMatch(approval -> approval.getMemberId().equals(memberId));
    }
    
    /**
     * Check if all required members have approved the request.
     */
    public boolean hasAllMembersApproved(List<String> allMemberIds) {
        if (!requiresAllMemberApproval) {
            return true; // If all-member approval not required, consider it approved
        }
        
        // Check if all members have approved
        for (String memberId : allMemberIds) {
            if (!hasMemberApproved(memberId)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if any member has rejected the request.
     */
    public boolean hasAnyMemberRejected() {
        return memberApprovals.stream()
                .anyMatch(approval -> "reject".equals(approval.getAction()));
    }
    
    /**
     * Get the list of members who haven't responded yet.
     */
    public List<String> getPendingMemberIds(List<String> allMemberIds) {
        return allMemberIds.stream()
                .filter(memberId -> !hasMemberResponded(memberId))
                .toList();
    }
    
    /**
     * Mark the join request as approved (legacy method for backward compatibility).
     */
    public void approve(String reviewerUserId) {
        this.status = "approved";
        this.respondedAt = Instant.now();
        this.reviewedByUserId = reviewerUserId;
    }
    
    /**
     * Mark the join request as rejected (legacy method for backward compatibility).
     */
    public void reject(String reviewerUserId, String reason) {
        this.status = "rejected";
        this.respondedAt = Instant.now();
        this.reviewedByUserId = reviewerUserId;
        this.rejectionReason = reason;
    }
    
    /**
     * Finalize the join request based on member approvals.
     */
    public void finalizeBasedOnApprovals(List<String> allMemberIds) {
        if (hasAnyMemberRejected()) {
            this.status = "rejected";
            this.respondedAt = Instant.now();
            this.rejectionReason = "Rejected by one or more group members";
        } else if (hasAllMembersApproved(allMemberIds)) {
            this.status = "approved";
            this.respondedAt = Instant.now();
        }
        // Otherwise, status remains "pending"
    }
}
