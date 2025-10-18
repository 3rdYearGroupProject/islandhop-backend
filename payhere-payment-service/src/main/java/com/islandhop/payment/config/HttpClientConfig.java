package com.islandhop.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for HTTP clients
 */
@Configuration
public class HttpClientConfig {
    
    /**
     * RestTemplate bean for making HTTP requests
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
