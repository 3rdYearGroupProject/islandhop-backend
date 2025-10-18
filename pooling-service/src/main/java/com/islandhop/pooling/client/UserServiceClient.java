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
                profile.setDob((String) response.get("dob")); // ISO date string
                
                // Handle profileCompletion - could be Integer or int
                Object profileCompletionObj = response.get("profileCompletion");
                if (profileCompletionObj instanceof Number) {
                    profile.setProfileCompletion(((Number) profileCompletionObj).intValue());
                }
                
                // Handle languages list
                @SuppressWarnings("unchecked")
                java.util.List<String> languages = (java.util.List<String>) response.get("languages");
                profile.setLanguages(languages);
                
                // Handle profile picture (byte array) - note: might be null
                Object profilePicObj = response.get("profilePic");
                if (profilePicObj instanceof byte[]) {
                    profile.setProfilePic((byte[]) profilePicObj);
                }
                
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
                profile.setDob((String) response.get("dob")); // ISO date string
                
                // Handle profileCompletion - could be Integer or int
                Object profileCompletionObj = response.get("profileCompletion");
                if (profileCompletionObj instanceof Number) {
                    profile.setProfileCompletion(((Number) profileCompletionObj).intValue());
                }
                
                // Handle languages list
                @SuppressWarnings("unchecked")
                java.util.List<String> languages = (java.util.List<String>) response.get("languages");
                profile.setLanguages(languages);
                
                // Handle profile picture (byte array) - note: might be null
                Object profilePicObj = response.get("profilePic");
                if (profilePicObj instanceof byte[]) {
                    profile.setProfilePic((byte[]) profilePicObj);
                }
                
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
    
    /**
     * Gets Firebase UID from email address.
     * Calls the user service to get the UID for a given email.
     * 
     * @param email The user's email
     * @return Firebase UID or null if not found
     */
    public String getUidByEmail(String email) {
        try {
            String url = userServiceBaseUrl + "/api/v1/tourist/uid-by-email?email=" + email;
            log.debug("Fetching UID for email from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("uid")) {
                return (String) response.get("uid");
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("UID not found for email: {}", email);
        } catch (Exception e) {
            log.error("Error fetching UID for email {}: {}", email, e.getMessage());
        }
        
        return null;
    }
    
    @Data
    public static class UserProfile {
        private String uid; // Firebase UID
        private String email;
        private String firstName;
        private String lastName;
        private String nationality;
        private String dob; // Date of birth as ISO string
        private Integer profileCompletion; // Profile completion percentage
        private java.util.List<String> languages; // User's languages
        private byte[] profilePic; // Profile picture as byte array
        
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return email;
        }
    }
}
