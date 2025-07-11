package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a user is invited to a private group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitedEvent {
    
    private String groupId;
    
    private String groupName;
    
    private String invitedUserId;
    
    private String inviterUserId;
    
    private Instant timestamp;
    
    public static UserInvitedEvent create(String groupId, String groupName, 
                                         String invitedUserId, String inviterUserId) {
        return new UserInvitedEvent(groupId, groupName, invitedUserId, inviterUserId, Instant.now());
    }
}
