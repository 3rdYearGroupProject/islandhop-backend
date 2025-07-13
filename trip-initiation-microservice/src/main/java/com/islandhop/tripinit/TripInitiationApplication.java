package com.islandhop.tripinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main application class for Trip Initiation Microservice.
 * Handles trip plan initiation with route calculation and cost estimation.
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.islandhop.tripinit.repository.mongodb")
@EnableJpaRepositories(basePackages = "com.islandhop.tripinit.repository.postgresql")
public class TripInitiationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripInitiationApplication.class, args);
    }
}