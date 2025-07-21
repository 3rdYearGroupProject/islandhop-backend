package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a join request is submitted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestEvent {
    
    private String groupId;
    
    private String groupName;
    
    private String joinerUserId;
    
    private String creatorUserId;
    
    private Map<String, Object> userProfile;
    
    private Instant timestamp;
    
    public static JoinRequestEvent create(String groupId, String groupName, String joinerUserId, 
                                         String creatorUserId, Map<String, Object> userProfile) {
        return new JoinRequestEvent(groupId, groupName, joinerUserId, creatorUserId, userProfile, Instant.now());
    }
}
