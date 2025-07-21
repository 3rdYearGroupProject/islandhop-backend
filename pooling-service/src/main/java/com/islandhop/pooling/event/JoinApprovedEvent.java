package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a join request is approved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinApprovedEvent {
    
    private String groupId;
    
    private String groupName;
    
    private String joinerUserId;
    
    private String approverUserId;
    
    private Instant timestamp;
    
    public static JoinApprovedEvent create(String groupId, String groupName, 
                                          String joinerUserId, String approverUserId) {
        return new JoinApprovedEvent(groupId, groupName, joinerUserId, approverUserId, Instant.now());
    }
}
