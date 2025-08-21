package com.islandhop.pooling.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

/**
 * REST client for communicating with the User Services microservice.
 * Handles user profile retrieval by email.
 */
@Service
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;
    
    public UserServiceClient(RestTemplate restTemplate, 
                           @Value("${services.user-service.base-url:http://localhost:8083}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }
    
    /**
     * Gets user profile information by email.
     * Note: User service currently only supports lookup by email, not user ID.
     * 
     * @param email The user's email
     * @return UserProfile containing name and other details, or null if not found
     */
    public UserProfile getUserByEmail(String email) {
        try {
            String url = userServiceBaseUrl + "/api/v1/tourist/profile?email=" + email;
            log.debug("Fetching user profile from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                UserProfile profile = new UserProfile();
                profile.setEmail((String) response.get("email"));
                profile.setFirstName((String) response.get("firstName"));
                profile.setLastName((String) response.get("lastName"));
                profile.setNationality((String) response.get("nationality"));
                
                return profile;
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User profile not found for email: {}", email);
        } catch (Exception e) {
            log.error("Error fetching user profile for email {}: {}", email, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Gets user full name by email.
     * 
     * @param email The user's email
     * @return Full name (firstName + lastName) or email if name not available
     */
    public String getUserNameByEmail(String email) {
        UserProfile profile = getUserByEmail(email);
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return email; // Fallback to email if name not available
    }
    
    /**
     * Gets user profile information by Firebase UID.
     * 
     * @param uid The user's Firebase UID
     * @return UserProfile containing name and other details, or null if not found
     */
    public UserProfile getUserByUid(String uid) {
        try {
            String url = userServiceBaseUrl + "/api/v1/tourist/profile/by-uid/" + uid;
            log.debug("Fetching user profile from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                UserProfile profile = new UserProfile();
                profile.setEmail((String) response.get("email"));
                profile.setFirstName((String) response.get("firstName"));
                profile.setLastName((String) response.get("lastName"));
                profile.setNationality((String) response.get("nationality"));
                
                return profile;
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User profile not found for UID: {}", uid);
        } catch (Exception e) {
            log.error("Error fetching user profile for UID {}: {}", uid, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Gets user full name by Firebase UID.
     * 
     * @param uid The user's Firebase UID
     * @return Full name (firstName + lastName) or UID if name not available
     */
    public String getUserNameByUid(String uid) {
        UserProfile profile = getUserByUid(uid);
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return uid; // Fallback to UID if name not available
    }
    
    @Data
    public static class UserProfile {
        private String email;
        private String firstName;
        private String lastName;
        private String nationality;
        
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return email;
        }
    }
}
