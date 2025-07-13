package com.islandhop.tripinitiation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.maps.GeoApiContext;

@Configuration
public class GoogleMapsConfig {

    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey("YOUR_GOOGLE_MAPS_API_KEY") // Replace with your actual API key
                .build();
    }
}