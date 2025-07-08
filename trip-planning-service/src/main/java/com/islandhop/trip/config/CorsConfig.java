package com.islandhop.trip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the IslandHop Trip Planning Service.
 * Enables cross-origin requests from frontend applications.
 * 
 * Configuration is externalized via application properties:
 * - cors.allowed-origins: Comma-separated list of allowed origins
 * - cors.allowed-methods: Comma-separated list of allowed HTTP methods
 * - cors.allowed-headers: Comma-separated list of allowed headers
 * - cors.allow-credentials: Whether to allow credentials
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;
    
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS for all endpoints
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(3600);
                
        // Also explicitly configure for /api/** to ensure coverage
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }
}
