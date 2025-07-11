package com.islandhop.pooling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when trip suggestions are generated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSuggestionsGeneratedEvent {
    
    private String userId;
    
    private Integer suggestionsCount;
    
    private Instant timestamp;
    
    public static TripSuggestionsGeneratedEvent create(String userId, Integer suggestionsCount) {
        return new TripSuggestionsGeneratedEvent(userId, suggestionsCount, Instant.now());
    }
}
