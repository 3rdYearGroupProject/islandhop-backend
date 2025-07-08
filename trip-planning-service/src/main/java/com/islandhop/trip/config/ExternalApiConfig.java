package com.islandhop.trip.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for external API integrations.
 * Manages API keys and base URLs for TripAdvisor and Google Places.
 */
@Configuration
@Slf4j
public class ExternalApiConfig {

    @Bean
    @ConfigurationProperties(prefix = "external-apis.tripadvisor")
    public TripAdvisorConfig tripAdvisorConfig() {
        return new TripAdvisorConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "external-apis.google-places")
    public GooglePlacesConfig googlePlacesConfig() {
        return new GooglePlacesConfig();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
    @Data
    public static class TripAdvisorConfig {
        private String baseUrl;
        private String apiKey;
    }

    @Data
    public static class GooglePlacesConfig {
        private String baseUrl;
        private String apiKey;
    }
}
