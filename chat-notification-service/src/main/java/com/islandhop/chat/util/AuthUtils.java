package com.islandhop.chat.util;

import com.islandhop.chat.service.FirebaseAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for extracting authenticated user information from requests.
 */
public class AuthUtils {

    /**
     * Get the current authenticated user's ID from the request context.
     * 
     * @return User ID if authenticated, null otherwise
     */
    public static String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return (String) request.getAttribute("userId");
            }
        } catch (Exception e) {
            // Silently handle any errors
        }
        return null;
    }

    /**
     * Get the current authenticated user's information from the request context.
     * 
     * @return UserInfo if authenticated, null otherwise
     */
    public static FirebaseAuthService.UserInfo getCurrentUserInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return (FirebaseAuthService.UserInfo) request.getAttribute("firebaseUser");
            }
        } catch (Exception e) {
            // Silently handle any errors
        }
        return null;
    }

    /**
     * Check if the current user is authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    /**
     * Validate that the given user ID matches the authenticated user.
     * 
     * @param userId The user ID to validate
     * @return true if the user ID matches the authenticated user
     * @throws SecurityException if user IDs don't match
     */
    public static boolean validateUserAccess(String userId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User not authenticated");
        }
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("Access denied: User ID mismatch");
        }
        return true;
    }

    /**
     * Extract user ID from request or use authenticated user ID as fallback.
     * 
     * @param requestUserId User ID from request parameter/body
     * @return The validated user ID
     * @throws SecurityException if validation fails
     */
    public static String getValidatedUserId(String requestUserId) {
        String authenticatedUserId = getCurrentUserId();
        
        if (authenticatedUserId == null) {
            throw new SecurityException("User not authenticated");
        }
        
        // If no user ID provided in request, use authenticated user ID
        if (requestUserId == null || requestUserId.trim().isEmpty()) {
            return authenticatedUserId;
        }
        
        // Validate that request user ID matches authenticated user ID
        if (!authenticatedUserId.equals(requestUserId)) {
            throw new SecurityException("Access denied: Cannot access resources for different user");
        }
        
        return requestUserId;
    }
}
