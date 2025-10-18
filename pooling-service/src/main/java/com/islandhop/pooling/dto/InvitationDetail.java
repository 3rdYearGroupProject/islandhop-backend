package com.islandhop.pooling.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Detailed invitation information for the invitations response.
 * Contains all necessary information about a group invitation.
 */
@Data
public class InvitationDetail {
    
    private String invitationId;
    
    private String groupId;
    
    private String groupName;
    
    private String inviterName;
    
    private String inviterEmail;
    
    private Instant invitedAt;
    
    private Instant tripStartDate;
    
    private Instant tripEndDate;
    
    private int memberCount;
    
    private int maxMembers;
    
    private String message;
    
    private String groupDescription;
    
    private List<String> preferredActivities;
    
    private String baseCity;
    
    private String status; // "pending", "accepted", "rejected", "expired"
}
