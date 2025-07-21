package com.islandhop.trip.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to load environment variables from .env file.
 * This ensures that API keys and other environment variables are available to Spring Boot.
 */
@Configuration
@Slf4j
public class DotEnvConfig {

    @PostConstruct
    public void loadEnvVars() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")  // Look in current directory
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Set system properties so Spring can pick them up
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Only set if not already set as system property
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    log.debug("Loaded environment variable: {}", key);
                }
            });

            // Verify key API keys are loaded
            String tripAdvisorKey = System.getProperty("TRIPADVISOR_API_KEY");
            String googlePlacesKey = System.getProperty("GOOGLE_PLACES_API_KEY");
            
            if (tripAdvisorKey != null && !tripAdvisorKey.equals("demo-key")) {
                log.info("TripAdvisor API key loaded successfully");
            } else {
                log.warn("TripAdvisor API key not found or using demo value");
            }
            
            if (googlePlacesKey != null && !googlePlacesKey.equals("demo-key")) {
                log.info("Google Places API key loaded successfully");
            } else {
                log.warn("Google Places API key not found or using demo value");
            }
            
        } catch (Exception e) {
            log.warn("Could not load .env file: {}", e.getMessage());
        }
    }
}
