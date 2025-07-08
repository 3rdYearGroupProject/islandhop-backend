package com.islandhop.trip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the IslandHop Trip Planning Service.
 * Enables cross-origin requests from frontend applications.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS for all endpoints with comprehensive coverage
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001", 
                    "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders(
                    "Origin",
                    "Content-Type", 
                    "Accept",
                    "Authorization",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers",
                    "X-Requested-With",
                    "Cache-Control"
                )
                .exposedHeaders(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Allow-Headers"
                )
                .allowCredentials(true)
                .maxAge(3600);
                
        // Specific mapping for API endpoints to ensure coverage
        registry.addMapping("/v1/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001", 
                    "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
