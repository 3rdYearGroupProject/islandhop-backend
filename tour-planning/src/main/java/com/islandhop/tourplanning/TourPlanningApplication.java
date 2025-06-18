package com.islandhop.tourplanning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class TourPlanningApplication {
    public static void main(String[] args) {
        SpringApplication.run(TourPlanningApplication.class, args);
    }
} 