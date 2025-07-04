package com.islandhop.pooling.service;

import com.islandhop.pooling.client.TripPlanningServiceClient;
import com.islandhop.pooling.client.UserServiceClient;
import com.islandhop.pooling.client.TripDto;
import com.islandhop.pooling.client.TouristProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Enhanced integration service that properly handles session validation
 * and uses the existing endpoints from other microservices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DirectIntegrationService {

    private final TripPlanningServiceClient tripPlanningClient;
    private final UserServiceClient userServiceClient;

    /**
     * Get all trips for a user with proper validation
     */
    public List<TripDto> getUserTrips(String userId) {
        log.info("Getting trips for user: {}", userId);
        try {
            // First validate the user exists
            if (!validateUserExists(userId)) {
                log.warn("User {} does not exist or is invalid", userId);
                return List.of();
            }
            
            return tripPlanningClient.getUserTrips(userId);
        } catch (Exception e) {
            log.error("Error getting trips for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get a specific trip by ID and user ID with validation
     */
    public TripDto getTripByIdAndUserId(String tripId, String userId) {
        log.info("Getting trip {} for user: {}", tripId, userId);
        try {
            // Validate user first
            if (!validateUserExists(userId)) {
                log.warn("User {} does not exist or is invalid", userId);
                return null;
            }
            
            return tripPlanningClient.getTripByIdAndUserId(tripId, userId);
        } catch (Exception e) {
            log.error("Error getting trip {} for user {}: {}", tripId, userId, e.getMessage());
            return null;
        }
    }

    /**
     * Get trips within a date range
     */
    public List<TripDto> getTripsInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Getting trips in date range: {} to {}", startDate, endDate);
        try {
            return tripPlanningClient.getTripsInDateRange(startDate, endDate);
        } catch (Exception e) {
            log.error("Error getting trips in date range {} to {}: {}", startDate, endDate, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get trips by base city
     */
    public List<TripDto> getTripsByBaseCity(String baseCity) {
        log.info("Getting trips for base city: {}", baseCity);
        try {
            return tripPlanningClient.getTripsByBaseCity(baseCity);
        } catch (Exception e) {
            log.error("Error getting trips for base city {}: {}", baseCity, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get tourist profile by email with validation
     */
    public TouristProfileDto getTouristProfileByEmail(String email) {
        log.info("Getting tourist profile for email: {}", email);
        try {
            return userServiceClient.getTouristProfileByEmail(email);
        } catch (Exception e) {
            log.error("Error getting tourist profile for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Validate if user exists and has valid session
     */
    public boolean validateUserExists(String userIdOrEmail) {
        log.info("Validating user: {}", userIdOrEmail);
        try {
            // If it's an email, try to get profile
            if (userIdOrEmail.contains("@")) {
                TouristProfileDto profile = userServiceClient.getTouristProfileByEmail(userIdOrEmail);
                return profile != null;
            }
            
            // If it's a user ID, try to validate session
            return userServiceClient.validateUserSession(userIdOrEmail);
        } catch (Exception e) {
            log.error("Error validating user {}: {}", userIdOrEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Get user ID from email (using profile ID)
     */
    public String getUserIdFromEmail(String email) {
        log.info("Getting user ID for email: {}", email);
        try {
            TouristProfileDto profile = userServiceClient.getTouristProfileByEmail(email);
            if (profile != null) {
                return profile.getId();
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting user ID for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Check if both services are available (for health checks)
     */
    public boolean areServicesAvailable() {
        try {
            boolean tripServiceHealthy = tripPlanningClient.isServiceHealthy();
            boolean userServiceHealthy = userServiceClient.isServiceHealthy();
            
            log.info("Service availability - Trip Planning: {}, User Service: {}", 
                    tripServiceHealthy, userServiceHealthy);
            
            return tripServiceHealthy && userServiceHealthy;
        } catch (Exception e) {
            log.error("Error checking service availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Enhanced user validation that checks both account and profile
     */
    public boolean validateUserWithProfile(String email) {
        log.info("Validating user with profile: {}", email);
        try {
            // Check if profile exists and is complete
            TouristProfileDto profile = userServiceClient.getTouristProfileByEmail(email);
            if (profile == null) {
                log.warn("No profile found for email: {}", email);
                return false;
            }

            // Check if profile has required fields
            if (profile.getFirstName() == null || profile.getLastName() == null) {
                log.warn("Incomplete profile for email: {}", email);
                return false;
            }

            log.info("User validation successful for email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Error validating user with profile {}: {}", email, e.getMessage());
            return false;
        }
    }
}
