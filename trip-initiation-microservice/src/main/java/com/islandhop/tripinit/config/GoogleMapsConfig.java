package com.islandhop.tripinit.config;

import com.google.maps.GeoApiContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GoogleMapsConfig {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext() {
        // Log API key for debugging (mask most characters for security)
        String maskedKey = apiKey != null && apiKey.length() > 8 ? 
            apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4) : "null or too short";
        log.info("Configuring Google Maps API with key: {}", maskedKey);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("Google Maps API key is null or empty!");
            throw new IllegalStateException("Google Maps API key must be configured");
        }
        
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public String googleMapsApiKey() {
        return apiKey;
    }
}