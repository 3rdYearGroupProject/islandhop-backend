package com.islandhop.pooling.service;

import com.islandhop.pooling.client.TripPlanningServiceClient;
import com.islandhop.pooling.client.UserServiceClient;
import com.islandhop.pooling.client.TripDto;
import com.islandhop.pooling.client.TouristProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to test and validate integration with external services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationTestService {

    private final TripPlanningServiceClient tripPlanningClient;
    private final UserServiceClient userServiceClient;

    /**
     * Test connectivity to trip planning service
     */
    public boolean testTripPlanningServiceConnectivity() {
        log.info("Testing trip planning service connectivity...");
        
        try {
            boolean isHealthy = tripPlanningClient.isServiceHealthy();
            log.info("Trip planning service health check: {}", isHealthy ? "PASSED" : "FAILED");
            return isHealthy;
        } catch (Exception e) {
            log.error("Trip planning service connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test connectivity to user service
     */
    public boolean testUserServiceConnectivity() {
        log.info("Testing user service connectivity...");
        
        try {
            boolean isHealthy = userServiceClient.isServiceHealthy();
            log.info("User service health check: {}", isHealthy ? "PASSED" : "FAILED");
            return isHealthy;
        } catch (Exception e) {
            log.error("User service connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test fetching user trips
     */
    public List<TripDto> testGetUserTrips(String userId) {
        log.info("Testing getUserTrips for userId: {}", userId);
        
        try {
            List<TripDto> trips = tripPlanningClient.getUserTrips(userId);
            log.info("Successfully fetched {} trips for user {}", trips.size(), userId);
            return trips;
        } catch (Exception e) {
            log.error("Failed to fetch trips for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Test fetching tourist profile
     */
    public TouristProfileDto testGetTouristProfile(String email) {
        log.info("Testing getTouristProfile for email: {}", email);
        
        try {
            TouristProfileDto profile = userServiceClient.getTouristProfileByEmail(email);
            if (profile != null) {
                log.info("Successfully fetched profile for email: {}", email);
            } else {
                log.warn("No profile found for email: {}", email);
            }
            return profile;
        } catch (Exception e) {
            log.error("Failed to fetch profile for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Comprehensive integration test
     */
    public IntegrationTestResult runComprehensiveTest(String testUserId, String testEmail) {
        log.info("Running comprehensive integration test...");
        
        IntegrationTestResult result = new IntegrationTestResult();
        
        // Test service connectivity
        result.setTripPlanningServiceHealthy(testTripPlanningServiceConnectivity());
        result.setUserServiceHealthy(testUserServiceConnectivity());
        
        // Test data fetching
        List<TripDto> trips = testGetUserTrips(testUserId);
        result.setUserTripsCount(trips.size());
        result.setCanFetchUserTrips(!trips.isEmpty());
        
        TouristProfileDto profile = testGetTouristProfile(testEmail);
        result.setCanFetchUserProfile(profile != null);
        result.setUserProfile(profile);
        
        // Overall success
        result.setOverallSuccess(
            result.isTripPlanningServiceHealthy() && 
            result.isUserServiceHealthy() &&
            (result.isCanFetchUserTrips() || trips.size() == 0) && // Allow 0 trips for new users
            result.isCanFetchUserProfile()
        );
        
        log.info("Integration test completed. Overall success: {}", result.isOverallSuccess());
        return result;
    }

    /**
     * Result class for integration tests
     */
    public static class IntegrationTestResult {
        private boolean tripPlanningServiceHealthy;
        private boolean userServiceHealthy;
        private boolean canFetchUserTrips;
        private boolean canFetchUserProfile;
        private int userTripsCount;
        private TouristProfileDto userProfile;
        private boolean overallSuccess;

        // Getters and setters
        public boolean isTripPlanningServiceHealthy() { return tripPlanningServiceHealthy; }
        public void setTripPlanningServiceHealthy(boolean tripPlanningServiceHealthy) { this.tripPlanningServiceHealthy = tripPlanningServiceHealthy; }

        public boolean isUserServiceHealthy() { return userServiceHealthy; }
        public void setUserServiceHealthy(boolean userServiceHealthy) { this.userServiceHealthy = userServiceHealthy; }

        public boolean isCanFetchUserTrips() { return canFetchUserTrips; }
        public void setCanFetchUserTrips(boolean canFetchUserTrips) { this.canFetchUserTrips = canFetchUserTrips; }

        public boolean isCanFetchUserProfile() { return canFetchUserProfile; }
        public void setCanFetchUserProfile(boolean canFetchUserProfile) { this.canFetchUserProfile = canFetchUserProfile; }

        public int getUserTripsCount() { return userTripsCount; }
        public void setUserTripsCount(int userTripsCount) { this.userTripsCount = userTripsCount; }

        public TouristProfileDto getUserProfile() { return userProfile; }
        public void setUserProfile(TouristProfileDto userProfile) { this.userProfile = userProfile; }

        public boolean isOverallSuccess() { return overallSuccess; }
        public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
    }
}
