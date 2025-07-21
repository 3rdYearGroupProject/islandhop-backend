package com.islandhop.pooling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the IslandHop Pooling Service.
 * Enables cross-origin requests from frontend applications.
 * Follows the same patterns as trip planning service.
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
                .allowCredentials(true)
                .maxAge(3600);
    }
}
