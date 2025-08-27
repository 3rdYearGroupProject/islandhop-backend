package com.islandhop.chat.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.islandhop.chat.service.FirebaseAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to authenticate requests using Firebase ID tokens.
 * Validates the Authorization header and sets the security context.
 * 
 * DISABLED: This filter is currently disabled for development/testing.
 * To re-enable, uncomment @Component annotation and add back to SecurityConfig.
 */
// @Component - DISABLED FOR NO-AUTH MODE
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Skip authentication for health check endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String idToken = authHeader.substring(BEARER_PREFIX.length());
                
                // Verify the Firebase ID token
                FirebaseAuthService.UserInfo userInfo = firebaseAuthService.getUserInfo(idToken);
                
                if (userInfo != null) {
                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userInfo.getUid(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    
                    // Set additional details
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set user info as additional details
                    request.setAttribute("firebaseUser", userInfo);
                    request.setAttribute("userId", userInfo.getUid());
                    
                    // Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("Authentication successful for user: {}", userInfo.getUid());
                } else {
                    logger.warn("Invalid Firebase token provided");
                    handleAuthenticationError(response, "Invalid authentication token");
                    return;
                }
            } else {
                logger.warn("No Authorization header found or invalid format");
                handleAuthenticationError(response, "Missing or invalid Authorization header");
                return;
            }
            
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            handleAuthenticationError(response, "Authentication failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the endpoint is public and doesn't require authentication.
     */
    private boolean isPublicEndpoint(String path) {
        return path.contains("/health") || 
               path.contains("/actuator") ||
               path.contains("/ws") ||  // WebSocket endpoint
               path.equals("/") ||
               path.contains("/error");
    }

    /**
     * Handle authentication errors by sending appropriate HTTP response.
     */
    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"AUTHENTICATION_FAILED\", \"message\": \"" + message + "\"}");
    }
}
