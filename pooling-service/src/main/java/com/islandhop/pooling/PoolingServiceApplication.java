package com.islandhop.pooling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the IslandHop Pooling Service.
 * Handles group-based trip planning, public trip sharing, and personalized trip suggestions.
 */
@SpringBootApplication
public class PoolingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoolingServiceApplication.class, args);
    }
}
