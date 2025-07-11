package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a public group is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicGroupCreatedEvent {
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private String creatorUserId;
    
    private Map<String, Object> preferences;
    
    private Instant timestamp;
    
    public static PublicGroupCreatedEvent create(String groupId, String groupName, 
                                                String tripId, String creatorUserId, 
                                                Map<String, Object> preferences) {
        return new PublicGroupCreatedEvent(groupId, groupName, tripId, creatorUserId, 
                                          preferences, Instant.now());
    }
}
