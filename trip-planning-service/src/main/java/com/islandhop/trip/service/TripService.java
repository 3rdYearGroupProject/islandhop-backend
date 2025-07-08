package com.islandhop.trip.service;

import com.islandhop.trip.dto.CreateTripRequest;
import com.islandhop.trip.dto.CreateTripResponse;
import com.islandhop.trip.model.DailyPlan;
import com.islandhop.trip.model.TripPlan;
import com.islandhop.trip.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for handling trip planning operations.
 * Contains business logic for creating and managing trip plans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripPlanRepository tripPlanRepository;

    /**
     * Creates a new trip plan based on the provided request.
     * Validates input data, generates daily plans, and saves to MongoDB.
     *
     * @param request The trip creation request containing user input
     * @return CreateTripResponse with the generated trip ID and status
     * @throws IllegalArgumentException if validation fails
     */
    public CreateTripResponse createTrip(CreateTripRequest request) {
        log.info("Creating new trip for user: {} with name: {}", request.getUserId(), request.getTripName());

        // Validate input data
        validateTripRequest(request);

        // Generate unique trip ID
        String tripId = UUID.randomUUID().toString();

        // Parse dates and calculate daily plans
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        List<DailyPlan> dailyPlans = generateDailyPlans(startDate, endDate);

        // Create trip plan entity
        TripPlan tripPlan = buildTripPlan(request, tripId, dailyPlans);

        // Save to MongoDB
        try {
            tripPlanRepository.save(tripPlan);
            log.info("Successfully created trip with ID: {} for user: {}", tripId, request.getUserId());
        } catch (Exception e) {
            log.error("Failed to save trip plan for user: {}", request.getUserId(), e);
            throw new RuntimeException("Failed to create trip plan", e);
        }

        return new CreateTripResponse("success", tripId, "Trip created successfully");
    }

    /**
     * Validates the trip creation request.
     * Checks date formats, date range, and other business rules.
     *
     * @param request The request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTripRequest(CreateTripRequest request) {
        // Validate date formats and range
        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate());
            LocalDate endDate = LocalDate.parse(request.getEndDate());

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }

            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format");
        }

        // Validate arrival time if provided
        if (request.getArrivalTime() != null && !request.getArrivalTime().isEmpty()) {
            try {
                LocalTime.parse(request.getArrivalTime());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid arrival time format. Use HH:mm format");
            }
        }

        // Check for duplicate trip names (optional business rule)
        if (tripPlanRepository.existsByUserIdAndTripName(request.getUserId(), request.getTripName())) {
            log.warn("User {} attempting to create duplicate trip name: {}", request.getUserId(), request.getTripName());
            // Note: Not throwing exception as per requirements, but logging for awareness
        }
    }

    /**
     * Generates empty daily plans for each day in the trip date range.
     *
     * @param startDate The trip start date
     * @param endDate   The trip end date
     * @return List of daily plans with empty content
     */
    private List<DailyPlan> generateDailyPlans(LocalDate startDate, LocalDate endDate) {
        List<DailyPlan> dailyPlans = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayNumber = 1;

        while (!currentDate.isAfter(endDate)) {
            DailyPlan dailyPlan = new DailyPlan();
            dailyPlan.setDay(dayNumber);
            dailyPlan.setCity("");
            dailyPlan.setUserSelected(false);
            dailyPlan.setAttractions(List.of());
            dailyPlan.setRestaurants(List.of());
            dailyPlan.setHotels(List.of());
            dailyPlan.setNotes(List.of());

            dailyPlans.add(dailyPlan);
            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }

        log.debug("Generated {} daily plans for trip from {} to {}", dailyPlans.size(), startDate, endDate);
        return dailyPlans;
    }

    /**
     * Builds a TripPlan entity from the request data.
     *
     * @param request    The trip creation request
     * @param tripId     The generated trip ID
     * @param dailyPlans The generated daily plans
     * @return Complete TripPlan entity ready for saving
     */
    private TripPlan buildTripPlan(CreateTripRequest request, String tripId, List<DailyPlan> dailyPlans) {
        TripPlan tripPlan = new TripPlan();
        Instant now = Instant.now();

        tripPlan.setId(tripId);
        tripPlan.setUserId(request.getUserId());
        tripPlan.setTripName(request.getTripName());
        tripPlan.setStartDate(request.getStartDate());
        tripPlan.setEndDate(request.getEndDate());
        tripPlan.setArrivalTime(request.getArrivalTime() != null ? request.getArrivalTime() : "");
        tripPlan.setBaseCity(request.getBaseCity());
        tripPlan.setMultiCityAllowed(request.getMultiCityAllowed() != null ? request.getMultiCityAllowed() : true);
        tripPlan.setActivityPacing(request.getActivityPacing() != null ? request.getActivityPacing() : "Normal");
        tripPlan.setBudgetLevel(request.getBudgetLevel() != null ? request.getBudgetLevel() : "Medium");
        tripPlan.setPreferredTerrains(request.getPreferredTerrains() != null ? request.getPreferredTerrains() : List.of());
        tripPlan.setPreferredActivities(request.getPreferredActivities() != null ? request.getPreferredActivities() : List.of());
        tripPlan.setDailyPlans(dailyPlans);
        tripPlan.setMapData(List.of());
        tripPlan.setCreatedAt(now);
        tripPlan.setLastUpdated(now);

        return tripPlan;
    }
}
