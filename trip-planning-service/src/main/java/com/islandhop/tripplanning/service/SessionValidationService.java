package com.islandhop.tripplanning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.web.server.WebSession;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionValidationService {
    
    private final WebClient webClient;
    
    @Value("${user.service.base-url}")
    private String userServiceBaseUrl;
    
    @Value("${user.service.validate-session-endpoint}")
    private String validateSessionEndpoint;
    
    /**
     * Validates session with user-services and returns userId
     */
    public String validateSessionAndGetUserId(WebSession session) {
        try {
            // Extract session data
            Boolean isAuthenticated = session.getAttribute("isAuthenticated");
            String email = session.getAttribute("userEmail");
            
            if (isAuthenticated == null || !isAuthenticated || email == null) {
                throw new SecurityException("No valid session found");
            }
            
            // Call user-services to validate session
            String validationUrl = userServiceBaseUrl + validateSessionEndpoint;
            log.debug("Validating session with user-services at: {}", validationUrl);
            
            Mono<Map> responseMono = webClient.get()
                    .uri(validationUrl)
                    .header("Cookie", "JSESSIONID=" + session.getId())
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block(); // Blocking call for simplicity
            
            if (response != null && Boolean.TRUE.equals(response.get("valid"))) {
                String validatedEmail = (String) response.get("email");
                if (email.equals(validatedEmail)) {
                    // Extract userId from user-services response
                    String userId = (String) response.get("userId");
                    if (userId != null && !userId.isEmpty()) {
                        log.debug("Session validated for userId: {}, email: {}", userId, email);
                        return userId; // Return actual userId from user-services
                    } else {
                        // Fallback to email if userId not provided by user-services
                        log.warn("No userId provided by user-services, using email as identifier");
                        return email;
                    }
                } else {
                    throw new SecurityException("Session email mismatch");
                }
            } else {
                throw new SecurityException("Session validation failed");
            }
            
        } catch (Exception e) {
            log.error("Session validation error: {}", e.getMessage());
            throw new SecurityException("Session validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Alternative method for direct session validation without external call
     * Use this if you want to skip user-services validation during development
     */
    public String validateLocalSession(WebSession session) {
        Boolean isAuthenticated = session.getAttribute("isAuthenticated");
        String email = session.getAttribute("userEmail");
        
        if (isAuthenticated != null && isAuthenticated && email != null) {
            return email; // Return email as userId
        } else {
            throw new SecurityException("No valid local session found");
        }
    }
    
    /**
     * Lightweight session validation - just checks if user is authenticated
     * Use this when frontend provides userId to avoid repeated user-service calls
     */
    public void validateSessionExists(WebSession session) {
        Boolean isAuthenticated = session.getAttribute("isAuthenticated");
        String email = session.getAttribute("userEmail");
        
        if (isAuthenticated == null || !isAuthenticated || email == null) {
            throw new SecurityException("No valid session found");
        }
        
        log.debug("Session validated for user: {}", email);
    }
    
    /**
     * Enhanced validation - verifies userId from frontend matches session
     * Use this for critical operations that require extra security
     */
    public void validateUserIdMatchesSession(String providedUserId, WebSession session) {
        String sessionUserId = validateSessionAndGetUserId(session);
        
        if (!sessionUserId.equals(providedUserId)) {
            log.warn("UserId mismatch: provided={}, session={}", providedUserId, sessionUserId);
            throw new SecurityException("UserId does not match authenticated session");
        }
        
        log.debug("UserId validated successfully: {}", providedUserId);
    }
    
    /**
     * Expected response format from user-services validation endpoint:
     * {
     *   "valid": true,
     *   "userId": "user123", // ← This is the unique user identifier we need
     *   "email": "user@example.com",
     *   "role": "TOURIST",
     *   "sessionExpiry": "2024-12-01T15:30:00Z"
     * }
     * 
     * The trip-planning service should use the userId field for all database operations
     * to ensure proper user isolation and data consistency across microservices.
     */
}
