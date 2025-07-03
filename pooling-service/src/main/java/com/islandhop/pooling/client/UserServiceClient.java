package com.islandhop.pooling.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final WebClient webClient;
    
    public UserServiceClient(@Value("${services.user-services.url}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }
    
    /**
     * Get tourist profile by email
     */
    public TouristProfileDto getTouristProfileByEmail(String email) {
        logger.info("Fetching tourist profile for email: {}", email);
        
        try {
            return webClient.get()
                    .uri("/tourist/profile/{email}", email)
                    .retrieve()
                    .bodyToMono(TouristProfileDto.class)
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching tourist profile for email {}: {}", email, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user ID from email (for compatibility)
     */
    public String getUserIdByEmail(String email) {
        logger.info("Getting user ID for email: {}", email);
        
        try {
            TouristProfileDto profile = getTouristProfileByEmail(email);
            return profile != null ? profile.getId() : null;
        } catch (Exception e) {
            logger.error("Error getting user ID for email {}: {}", email, e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate user session
     */
    public boolean validateUserSession(String userId) {
        logger.info("Validating session for user: {}", userId);
        
        try {
            // This would need to be implemented in your user service
            // For now, we'll assume the user is valid if we can fetch their profile
            return getTouristProfileByEmail(userId) != null;
        } catch (Exception e) {
            logger.error("Error validating session for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check for user service
     */
    public boolean isServiceHealthy() {
        try {
            String response = webClient.get()
                    .uri("/tourist/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "OK".equals(response);
        } catch (Exception e) {
            logger.warn("User service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
