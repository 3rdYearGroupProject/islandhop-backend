package com.islandhop.tripplanning.config;

/**
 * Centralized CORS configuration constants.
 * This class provides a single place to manage CORS origins across all controllers.
 */
public class CorsConfigConstants {
    
    /**
     * The allowed origin for CORS requests.
     * Change this value to update the origin for all controllers.
     */
    public static final String ALLOWED_ORIGIN = "http://localhost:3000";
    
    /**
     * Whether to allow credentials in CORS requests.
     */
    public static final String ALLOW_CREDENTIALS = "true";
    
    private CorsConfigConstants() {
        // Utility class - prevent instantiation
    }
}
