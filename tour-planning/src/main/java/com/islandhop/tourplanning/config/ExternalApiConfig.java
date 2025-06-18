package com.islandhop.tourplanning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class ExternalApiConfig {

    @Value("${tripadvisor.api.key}")
    private String tripAdvisorApiKey;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    @Bean
    public String tripAdvisorApiKey() {
        return tripAdvisorApiKey;
    }

    @Bean
    public String googleMapsApiKey() {
        return googleMapsApiKey;
    }
} 