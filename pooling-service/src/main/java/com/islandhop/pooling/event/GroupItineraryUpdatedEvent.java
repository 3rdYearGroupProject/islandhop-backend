package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a group itinerary is updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupItineraryUpdatedEvent {
    
    private String groupId;
    
    private String tripId;
    
    private String userId;
    
    private String actionType;
    
    private Integer day;
    
    private String type;
    
    private String details;
    
    private Instant timestamp;
    
    public static GroupItineraryUpdatedEvent create(String groupId, String tripId, String userId, 
                                                   String actionType, Integer day, String type, String details) {
        return new GroupItineraryUpdatedEvent(groupId, tripId, userId, actionType, day, type, details, Instant.now());
    }
}
