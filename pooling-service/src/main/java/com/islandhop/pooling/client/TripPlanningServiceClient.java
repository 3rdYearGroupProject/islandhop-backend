package com.islandhop.pooling.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
public class TripPlanningServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(TripPlanningServiceClient.class);
    
    private final WebClient webClient;
    
    public TripPlanningServiceClient(@Value("${services.trip-planning.url}") String tripPlanningServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(tripPlanningServiceUrl)
                .build();
    }
    
    /**
     * Get all trips for a specific user
     */
    public List<TripDto> getUserTrips(String userId) {
        logger.info("Fetching trips for user: {}", userId);
        
        try {
            return webClient.get()
                    .uri("/trip/user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TripDto>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching trips for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get a specific trip by ID and user ID
     */
    public TripDto getTripByIdAndUserId(String tripId, String userId) {
        logger.info("Fetching trip {} for user: {}", tripId, userId);
        
        try {
            return webClient.get()
                    .uri("/trip/{tripId}", tripId)
                    .header("userId", userId)
                    .retrieve()
                    .bodyToMono(TripDto.class)
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching trip {} for user {}: {}", tripId, userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all trips within a date range (for finding potential matches)
     */
    public List<TripDto> getTripsInDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching trips in date range: {} to {}", startDate, endDate);
        
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/trip/search/date-range")
                            .queryParam("startDate", startDate.toString())
                            .queryParam("endDate", endDate.toString())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TripDto>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching trips in date range {} to {}: {}", startDate, endDate, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get trips by base city
     */
    public List<TripDto> getTripsByBaseCity(String baseCity) {
        logger.info("Fetching trips for base city: {}", baseCity);
        
        try {
            return webClient.get()
                    .uri("/trip/search/city/{baseCity}", baseCity)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TripDto>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching trips for base city {}: {}", baseCity, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Health check for trip planning service
     */
    public boolean isServiceHealthy() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "OK".equals(response);
        } catch (Exception e) {
            logger.warn("Trip planning service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
