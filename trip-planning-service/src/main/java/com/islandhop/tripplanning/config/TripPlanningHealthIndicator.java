package com.islandhop.tripplanning.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("tripPlanningHealthIndicator")
public class TripPlanningHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Basic service health check - always return UP if the application is running
            return Health.up()
                    .withDetail("service", "trip-planning-service")
                    .withDetail("status", "running")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .withDetail("version", "0.0.1-SNAPSHOT")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
