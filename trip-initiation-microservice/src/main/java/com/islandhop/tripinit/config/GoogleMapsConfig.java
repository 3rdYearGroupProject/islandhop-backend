package com.islandhop.tripinit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GoogleMapsConfig {
    
    private static final Logger log = LoggerFactory.getLogger(GoogleMapsConfig.class);

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public String googleMapsApiKey() {
        // Log API key for debugging (mask most characters for security)
        String maskedKey = apiKey != null && apiKey.length() > 8 ? 
            apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4) : "null or too short";
        log.info("Configuring Google Maps API with key: {}", maskedKey);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("Google Maps API key is null or empty!");
            throw new IllegalStateException("Google Maps API key must be configured");
        }
        
        return apiKey;
    }
}