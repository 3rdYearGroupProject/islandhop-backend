package com.islandhop.pooling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for REST clients and external service communication.
 */
@Configuration
public class RestClientConfig {
    
    /**
     * RestTemplate bean for making HTTP requests to other microservices.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
