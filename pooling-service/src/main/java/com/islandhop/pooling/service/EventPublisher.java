package com.islandhop.pooling.service;

import com.islandhop.pooling.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for publishing events to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Async
    public void publishPublicGroupCreatedEvent(PublicGroupCreatedEvent event) {
        try {
            kafkaTemplate.send("public-group-created", event.getGroupId(), event);
            log.info("Published PublicGroupCreatedEvent for group {}", event.getGroupId());
        } catch (Exception e) {
            log.error("Failed to publish PublicGroupCreatedEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishUserInvitedEvent(UserInvitedEvent event) {
        try {
            kafkaTemplate.send("user-invited", event.getGroupId(), event);
            log.info("Published UserInvitedEvent for group {} and user {}", event.getGroupId(), event.getInvitedUserId());
        } catch (Exception e) {
            log.error("Failed to publish UserInvitedEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishGroupItineraryUpdatedEvent(GroupItineraryUpdatedEvent event) {
        try {
            kafkaTemplate.send("group-itinerary-updated", event.getGroupId(), event);
            log.info("Published GroupItineraryUpdatedEvent for group {}", event.getGroupId());
        } catch (Exception e) {
            log.error("Failed to publish GroupItineraryUpdatedEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishJoinRequestEvent(JoinRequestEvent event) {
        try {
            kafkaTemplate.send("join-request", event.getGroupId(), event);
            log.info("Published JoinRequestEvent for group {} and user {}", event.getGroupId(), event.getJoinerUserId());
        } catch (Exception e) {
            log.error("Failed to publish JoinRequestEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishJoinApprovedEvent(JoinApprovedEvent event) {
        try {
            kafkaTemplate.send("join-approved", event.getGroupId(), event);
            log.info("Published JoinApprovedEvent for group {} and user {}", event.getGroupId(), event.getJoinerUserId());
        } catch (Exception e) {
            log.error("Failed to publish JoinApprovedEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishJoinRejectedEvent(JoinRejectedEvent event) {
        try {
            kafkaTemplate.send("join-rejected", event.getGroupId(), event);
            log.info("Published JoinRejectedEvent for group {} and user {}", event.getGroupId(), event.getJoinerUserId());
        } catch (Exception e) {
            log.error("Failed to publish JoinRejectedEvent for group {}: {}", event.getGroupId(), e.getMessage());
        }
    }
    
    @Async
    public void publishTripSuggestionsGeneratedEvent(TripSuggestionsGeneratedEvent event) {
        try {
            kafkaTemplate.send("trip-suggestions-generated", event.getUserId(), event);
            log.info("Published TripSuggestionsGeneratedEvent for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish TripSuggestionsGeneratedEvent for user {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
