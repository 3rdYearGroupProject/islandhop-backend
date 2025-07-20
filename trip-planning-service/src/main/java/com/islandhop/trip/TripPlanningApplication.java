package com.islandhop.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Main application class for the Trip Planning Service.
 * Handles travel itinerary planning for the IslandHop system.
 */
@SpringBootApplication
@PropertySource("classpath:api-keys.properties")
public class TripPlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripPlanningApplication.class, args);
    }
}
