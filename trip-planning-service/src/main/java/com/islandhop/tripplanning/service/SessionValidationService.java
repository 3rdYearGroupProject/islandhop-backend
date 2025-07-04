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
                    // Use email as userId for now (can be changed to actual userId later)
                    return email;
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
}
