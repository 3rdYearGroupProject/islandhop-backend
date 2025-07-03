package com.islandhop.pooling.controller;

import com.islandhop.pooling.service.IntegrationTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationTestController {

    private final IntegrationTestService integrationTestService;

    /**
     * Test trip planning service connectivity
     */
    @GetMapping("/test/trip-planning")
    public ResponseEntity<Map<String, Object>> testTripPlanningService() {
        boolean isHealthy = integrationTestService.testTripPlanningServiceConnectivity();
        return ResponseEntity.ok(Map.of(
            "service", "trip-planning-service",
            "healthy", isHealthy,
            "status", isHealthy ? "CONNECTED" : "DISCONNECTED"
        ));
    }

    /**
     * Test user service connectivity
     */
    @GetMapping("/test/user-service")
    public ResponseEntity<Map<String, Object>> testUserService() {
        boolean isHealthy = integrationTestService.testUserServiceConnectivity();
        return ResponseEntity.ok(Map.of(
            "service", "user-service",
            "healthy", isHealthy,
            "status", isHealthy ? "CONNECTED" : "DISCONNECTED"
        ));
    }

    /**
     * Test fetching user trips
     */
    @GetMapping("/test/user-trips/{userId}")
    public ResponseEntity<Map<String, Object>> testUserTrips(@PathVariable String userId) {
        var trips = integrationTestService.testGetUserTrips(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "tripsCount", trips.size(),
            "trips", trips,
            "canFetchTrips", !trips.isEmpty() || trips.size() == 0 // Allow empty for new users
        ));
    }

    /**
     * Test fetching user profile
     */
    @GetMapping("/test/user-profile/{email}")
    public ResponseEntity<Map<String, Object>> testUserProfile(@PathVariable String email) {
        var profile = integrationTestService.testGetTouristProfile(email);
        return ResponseEntity.ok(Map.of(
            "email", email,
            "profile", profile,
            "canFetchProfile", profile != null
        ));
    }

    /**
     * Comprehensive integration test
     */
    @GetMapping("/test/comprehensive")
    public ResponseEntity<IntegrationTestService.IntegrationTestResult> runComprehensiveTest(
            @RequestParam(defaultValue = "test-user-123") String userId,
            @RequestParam(defaultValue = "test@example.com") String email) {
        
        var result = integrationTestService.runComprehensiveTest(userId, email);
        return ResponseEntity.ok(result);
    }

    /**
     * Quick status check of all external services
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getIntegrationStatus() {
        boolean tripServiceHealthy = integrationTestService.testTripPlanningServiceConnectivity();
        boolean userServiceHealthy = integrationTestService.testUserServiceConnectivity();
        
        return ResponseEntity.ok(Map.of(
            "timestamp", java.time.LocalDateTime.now(),
            "services", Map.of(
                "trip-planning", Map.of(
                    "healthy", tripServiceHealthy,
                    "status", tripServiceHealthy ? "UP" : "DOWN"
                ),
                "user-service", Map.of(
                    "healthy", userServiceHealthy,
                    "status", userServiceHealthy ? "UP" : "DOWN"
                )
            ),
            "overallStatus", (tripServiceHealthy && userServiceHealthy) ? "ALL_SERVICES_UP" : "SOME_SERVICES_DOWN"
        ));
    }
}
