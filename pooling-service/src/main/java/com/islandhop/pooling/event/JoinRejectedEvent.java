package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a join request is rejected.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRejectedEvent {
    
    private String groupId;
    
    private String groupName;
    
    private String joinerUserId;
    
    private String rejecterUserId;
    
    private Instant timestamp;
    
    public static JoinRejectedEvent create(String groupId, String groupName, 
                                          String joinerUserId, String rejecterUserId) {
        return new JoinRejectedEvent(groupId, groupName, joinerUserId, rejecterUserId, Instant.now());
    }
}
