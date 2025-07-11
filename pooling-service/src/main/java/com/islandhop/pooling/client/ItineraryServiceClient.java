package com.islandhop.pooling.client;

import com.islandhop.pooling.dto.SuggestionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client for communicating with the itinerary service.
 */
@Component
@Slf4j
public class ItineraryServiceClient {
    
    private final WebClient webClient;
    
    public ItineraryServiceClient(WebClient.Builder webClientBuilder,
                                  @Value("${app.itinerary-service.url}") String itineraryServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(itineraryServiceUrl).build();
    }
    
    /**
     * Get trip plan details from the itinerary service.
     */
    @Cacheable(value = "tripPlans", key = "#tripId")
    public Mono<Map<String, Object>> getTripPlan(String tripId, String userId) {
        log.info("Fetching trip plan {} for user {}", tripId, userId);
        
        return webClient.get()
                .uri("/api/v1/itinerary/{tripId}?userId={userId}", tripId, userId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully fetched trip plan {}", tripId))
                .doOnError(error -> log.error("Error fetching trip plan {}: {}", tripId, error.getMessage()));
    }
    
    /**
     * Get trip summary from the itinerary service.
     */
    @Cacheable(value = "tripSummaries", key = "#tripId")
    public Mono<Map<String, Object>> getTripSummary(String tripId) {
        log.info("Fetching trip summary for {}", tripId);
        
        return webClient.get()
                .uri("/api/v1/itinerary/{tripId}/summary", tripId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully fetched trip summary {}", tripId))
                .doOnError(error -> log.error("Error fetching trip summary {}: {}", tripId, error.getMessage()));
    }
    
    /**
     * Create a new trip plan.
     */
    public Mono<Map<String, Object>> createTripPlan(String userId, Map<String, Object> tripData) {
        log.info("Creating new trip plan for user {}", userId);
        
        return webClient.post()
                .uri("/api/v1/itinerary?userId={userId}", userId)
                .bodyValue(tripData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully created trip plan for user {}", userId))
                .doOnError(error -> log.error("Error creating trip plan for user {}: {}", userId, error.getMessage()));
    }
    
    /**
     * Add a place to a trip's daily plan.
     */
    public Mono<Map<String, Object>> addPlaceToTrip(String tripId, int day, String type, 
                                                    SuggestionResponse place, String userId) {
        log.info("Adding place to trip {} day {} type {} for user {}", tripId, day, type, userId);
        
        return webClient.post()
                .uri("/api/v1/itinerary/{tripId}/day/{day}/{type}?userId={userId}", tripId, day, type, userId)
                .bodyValue(place)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully added place to trip {}", tripId))
                .doOnError(error -> log.error("Error adding place to trip {}: {}", tripId, error.getMessage()));
    }
    
    /**
     * Update city for a day in the trip.
     */
    public Mono<Map<String, Object>> updateCityForDay(String tripId, int day, String city, String userId) {
        log.info("Updating city for trip {} day {} to {} for user {}", tripId, day, city, userId);
        
        Map<String, String> requestBody = Map.of("city", city);
        
        return webClient.patch()
                .uri("/api/v1/itinerary/{tripId}/day/{day}/city?userId={userId}", tripId, day, userId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully updated city for trip {}", tripId))
                .doOnError(error -> log.error("Error updating city for trip {}: {}", tripId, error.getMessage()));
    }
    
    /**
     * Get trips by user ID.
     */
    public Mono<Map<String, Object>> getTripsByUserId(String userId) {
        log.info("Fetching trips for user {}", userId);
        
        return webClient.get()
                .uri("/api/v1/itineraries?userId={userId}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Successfully fetched trips for user {}", userId))
                .doOnError(error -> log.error("Error fetching trips for user {}: {}", userId, error.getMessage()));
    }
}
