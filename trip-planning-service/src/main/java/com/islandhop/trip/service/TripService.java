package com.islandhop.trip.service;

import com.islandhop.trip.dto.AddPlaceResponse;
import com.islandhop.trip.dto.CreateTripRequest;
import com.islandhop.trip.dto.CreateTripResponse;
import com.islandhop.trip.dto.SuggestionResponse;
import com.islandhop.trip.dto.TripPlanResponse;
import com.islandhop.trip.dto.UpdateCityRequest;
import com.islandhop.trip.dto.UpdateCityResponse;
import com.islandhop.trip.exception.InvalidDayException;
import com.islandhop.trip.exception.InvalidTypeException;
import com.islandhop.trip.exception.TripNotFoundException;
import com.islandhop.trip.exception.UnauthorizedTripAccessException;
import com.islandhop.trip.model.DailyPlan;
import com.islandhop.trip.model.Location;
import com.islandhop.trip.model.Place;
import com.islandhop.trip.model.TripPlan;
import com.islandhop.trip.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for handling trip planning operations.
 * Contains business logic for creating and managing trip plans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripPlanRepository tripPlanRepository;
    private final MongoTemplate mongoTemplate;
    private final ExternalApiService externalApiService;
    private final RedisTemplate<String, Object> redisTemplate;

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



    /**
     * Updates the city for a specific day in a trip plan.
     * Validates trip existence, user ownership, and day validity before updating.
     *
     * @param tripId The ID of the trip to update
     * @param day The day number to update (1-based)
     * @param userId The ID of the user making the request
     * @param request The update request containing the new city
     * @return UpdateCityResponse with the updated details
     * @throws TripNotFoundException if the trip doesn't exist
     * @throws UnauthorizedTripAccessException if the user doesn't own the trip
     * @throws InvalidDayException if the day number is invalid
     */
    public UpdateCityResponse updateCity(String tripId, int day, String userId, UpdateCityRequest request) {
        log.info("Updating city for trip: {}, day: {}, user: {}, city: {}", 
                tripId, day, userId, request.getCity());

        // Validate input parameters
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (day < 1) {
            throw new InvalidDayException("Day number must be positive (1-based indexing)");
        }

        // Find the trip and validate ownership
        Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
        if (tripOptional.isEmpty()) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            throw new TripNotFoundException("Trip not found with ID: " + tripId);
        }

        TripPlan trip = tripOptional.get();
        if (!trip.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} trying to access trip {} owned by {}", 
                    userId, tripId, trip.getUserId());
            throw new UnauthorizedTripAccessException("You are not authorized to modify this trip");
        }

        // Validate day number against trip duration
        if (day > trip.getDailyPlans().size()) {
            log.warn("Invalid day number: {} for trip: {} which has {} days", 
                    day, tripId, trip.getDailyPlans().size());
            throw new InvalidDayException("Day " + day + " is invalid. Trip has only " + 
                    trip.getDailyPlans().size() + " days");
        }

        // Update the specific day's city using MongoDB array update
        Query query = new Query(Criteria.where("id").is(tripId)
                .and("dailyPlans.day").is(day));
        
        Update update = new Update()
                .set("dailyPlans.$.city", request.getCity().trim())
                .set("dailyPlans.$.userSelected", true)
                .set("lastUpdated", Instant.now());

        try {
            mongoTemplate.updateFirst(query, update, TripPlan.class);
            log.info("Successfully updated city for trip: {}, day: {}, new city: {}", 
                    tripId, day, request.getCity());
        } catch (Exception e) {
            log.error("Failed to update city for trip: {}, day: {}", tripId, day, e);
            throw new RuntimeException("Failed to update trip plan", e);
        }

        return new UpdateCityResponse(
                "success",
                tripId,
                day,
                request.getCity().trim(),
                "City updated successfully"
        );
    }

    /**
     * Fetches preference-based suggestions for a specific day and type.
     * Uses caching to improve performance and reduce external API calls.
     *
     * @param tripId The trip ID
     * @param day The day number (1-based)
     * @param type The suggestion type (attractions, hotels, restaurants)
     * @param userId The user ID for ownership validation
     * @return List of suggestions filtered by user preferences
     * @throws TripNotFoundException if the trip doesn't exist
     * @throws UnauthorizedTripAccessException if the user doesn't own the trip
     * @throws InvalidDayException if the day number is invalid
     * @throws IllegalArgumentException if the type is invalid or city is not set
     */
    @Cacheable(value = "suggestions", 
               key = "#tripId + ':' + #day + ':' + #type + ':' + #userId + ':' + @tripService.getCacheKey(#tripId, #day)",
               condition = "#result != null && !#result.isEmpty()")
    public List<SuggestionResponse> getSuggestions(String tripId, int day, String type, String userId) {
        log.info("Fetching {} suggestions for trip: {}, day: {}, user: {}", type, tripId, day, userId);

        // Check negative cache first (for empty results) - handle Redis connection failures gracefully
        try {
            String negativeCacheKey = "empty:" + tripId + ":" + day + ":" + type + ":" + userId + ":" + getCacheKey(tripId, day);
            Boolean hasEmptyCache = redisTemplate.hasKey(negativeCacheKey);
            if (Boolean.TRUE.equals(hasEmptyCache)) {
                log.info("Returning empty result from negative cache for trip: {}, day: {}, type: {}", tripId, day, type);
                return List.of();
            }
        } catch (Exception e) {
            log.warn("Redis connection failed, skipping negative cache check: {}", e.getMessage());
        }

        // Validate input parameters
        if (!List.of("attractions", "hotels", "restaurants").contains(type)) {
            throw new IllegalArgumentException("Invalid suggestion type: " + type + 
                    ". Must be one of: attractions, hotels, restaurants");
        }
        if (day < 1) {
            throw new InvalidDayException("Day number must be positive (1-based indexing)");
        }
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // Find the trip and validate ownership
        Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
        if (tripOptional.isEmpty()) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            throw new TripNotFoundException("Trip not found with ID: " + tripId);
        }

        TripPlan trip = tripOptional.get();
        if (!trip.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} trying to access trip {} owned by {}", 
                    userId, tripId, trip.getUserId());
            throw new UnauthorizedTripAccessException("You are not authorized to access this trip");
        }

        // Validate day number against trip duration
        if (day > trip.getDailyPlans().size()) {
            log.warn("Invalid day number: {} for trip: {} which has {} days", 
                    day, tripId, trip.getDailyPlans().size());
            throw new InvalidDayException("Day " + day + " is invalid. Trip has only " + 
                    trip.getDailyPlans().size() + " days");
        }

        // Get the specific day's plan
        DailyPlan dailyPlan = trip.getDailyPlans().get(day - 1);
        String city = dailyPlan.getCity();
        
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City not set for day " + day + 
                    ". Please set a city first before getting suggestions.");
        }

        // Get user preferences
        List<String> preferredActivities = trip.getPreferredActivities() != null ? 
                trip.getPreferredActivities() : List.of();
        List<String> preferredTerrains = trip.getPreferredTerrains() != null ? 
                trip.getPreferredTerrains() : List.of();
        String budgetLevel = trip.getBudgetLevel() != null ? trip.getBudgetLevel() : "Medium";

        try {
            // Fetch suggestions from external APIs
            List<SuggestionResponse> suggestions = externalApiService.fetchSuggestions(
                    type, city.trim(), preferredActivities, preferredTerrains, budgetLevel);

            // Sort by relevance (distance, rating, preferences match)
            suggestions = suggestions.stream()
                    .sorted((a, b) -> {
                        // Primary sort: recommendation status
                        if (!a.getIsRecommended().equals(b.getIsRecommended())) {
                            return Boolean.compare(b.getIsRecommended(), a.getIsRecommended());
                        }
                        // Secondary sort: distance
                        if (a.getDistanceKm() != null && b.getDistanceKm() != null) {
                            return Double.compare(a.getDistanceKm(), b.getDistanceKm());
                        }
                        // Tertiary sort: rating
                        if (a.getRating() != null && b.getRating() != null) {
                            return Double.compare(b.getRating(), a.getRating());
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} {} suggestions for trip: {}, day: {}", 
                    suggestions.size(), type, tripId, day);
            
            // Cache empty results with shorter TTL (negative caching) - handle Redis connection failures gracefully
            if (suggestions.isEmpty()) {
                try {
                    String emptyCacheKey = "empty:" + tripId + ":" + day + ":" + type + ":" + userId + ":" + getCacheKey(tripId, day);
                    redisTemplate.opsForValue().set(emptyCacheKey, true, 
                            java.time.Duration.ofSeconds(300)); // 5 minutes TTL for empty results
                    log.debug("Cached empty result for key: {}", emptyCacheKey);
                } catch (Exception cacheException) {
                    log.warn("Failed to cache empty result, Redis connection failed: {}", cacheException.getMessage());
                }
            }
            
            return suggestions;

        } catch (Exception e) {
            log.error("Failed to fetch suggestions for trip: {}, day: {}, type: {}", 
                    tripId, day, type, e);
            throw new RuntimeException("Failed to fetch suggestions: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a cache key that includes city and preferences hash for consistent caching.
     * This method is used by the @Cacheable annotation to create stable cache keys.
     *
     * @param tripId The trip ID
     * @param day The day number
     * @return A cache key component including city and preferences
     */
    public String getCacheKey(String tripId, int day) {
        try {
            Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
            if (tripOptional.isEmpty()) {
                return "notfound";
            }

            TripPlan trip = tripOptional.get();
            if (day > trip.getDailyPlans().size()) {
                return "invalidday";
            }

            DailyPlan dailyPlan = trip.getDailyPlans().get(day - 1);
            String city = dailyPlan.getCity();
            if (city == null || city.trim().isEmpty()) {
                return "nocity";
            }

            // Create a hash of preferences to ensure cache uniqueness when preferences change
            List<String> preferredActivities = trip.getPreferredActivities() != null ? 
                    trip.getPreferredActivities() : List.of();
            List<String> preferredTerrains = trip.getPreferredTerrains() != null ? 
                    trip.getPreferredTerrains() : List.of();
            String budgetLevel = trip.getBudgetLevel() != null ? trip.getBudgetLevel() : "Medium";

            // Create a simple hash of preferences for cache key
            String preferencesString = preferredActivities.toString() + preferredTerrains.toString() + budgetLevel;
            int preferencesHash = preferencesString.hashCode();

            return city.trim().toLowerCase() + ":" + preferencesHash;
        } catch (Exception e) {
            log.warn("Error generating cache key for trip: {}, day: {}", tripId, day, e);
            return "error:" + System.currentTimeMillis(); // Prevent caching on error
        }
    }

    /**
     * Adds a selected place to a specific day and type in the trip itinerary.
     * Validates trip ownership, day validity, and converts SuggestionResponse to Place.
     *
     * @param tripId The trip ID
     * @param day The day number (1-based)
     * @param type The place type (attractions, hotels, restaurants)
     * @param userId The user ID for ownership validation
     * @param suggestionResponse The place data to add
     * @return AddPlaceResponse with confirmation details
     * @throws TripNotFoundException if the trip doesn't exist
     * @throws UnauthorizedTripAccessException if the user doesn't own the trip
     * @throws InvalidDayException if the day number is invalid
     * @throws InvalidTypeException if the type is invalid
     * @throws IllegalArgumentException if required fields are missing
     */
    public AddPlaceResponse addPlaceToItinerary(String tripId, int day, String type, String userId, SuggestionResponse suggestionResponse) {
        log.info("Adding place to itinerary for trip: {}, day: {}, type: {}, user: {}, place: {}", 
                tripId, day, type, userId, suggestionResponse.getName());

        // Validate input parameters
        if (suggestionResponse == null || suggestionResponse.getId() == null || 
            suggestionResponse.getName() == null || suggestionResponse.getCategory() == null) {
            throw new IllegalArgumentException("Place must include id, name, and category");
        }

        // Validate type
        String normalizedType = type.toLowerCase();
        if (!List.of("attractions", "hotels", "restaurants").contains(normalizedType)) {
            throw new InvalidTypeException("Invalid suggestion type: " + type + 
                    ". Must be one of: attractions, hotels, restaurants");
        }

        if (day < 1) {
            throw new InvalidDayException("Day number must be positive (1-based indexing)");
        }
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // Find the trip and validate ownership
        Optional<TripPlan> tripOptional = tripPlanRepository.findById(tripId);
        if (tripOptional.isEmpty()) {
            log.warn("Trip not found: {} for user: {}", tripId, userId);
            throw new TripNotFoundException("Trip not found with ID: " + tripId);
        }

        TripPlan trip = tripOptional.get();
        if (!trip.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} trying to access trip {} owned by {}", 
                    userId, tripId, trip.getUserId());
            throw new UnauthorizedTripAccessException("You are not authorized to modify this trip");
        }

        // Validate day number against trip duration
        if (day > trip.getDailyPlans().size()) {
            log.warn("Invalid day number: {} for trip: {} which has {} days", 
                    day, tripId, trip.getDailyPlans().size());
            throw new InvalidDayException("Day " + day + " is invalid. Trip has only " + 
                    trip.getDailyPlans().size() + " days");
        }

        // Get the specific day's plan
        DailyPlan dailyPlan = trip.getDailyPlans().get(day - 1);

        // Convert SuggestionResponse to Place
        Place place = convertSuggestionToPlace(suggestionResponse);

        // Add place to the appropriate list based on type
        try {
            switch (normalizedType) {
                case "attractions":
                    dailyPlan.getAttractions().add(place);
                    break;
                case "hotels":
                    dailyPlan.getHotels().add(place);
                    break;
                case "restaurants":
                    dailyPlan.getRestaurants().add(place);
                    break;
            }

            // Update lastUpdated timestamp
            trip.setLastUpdated(Instant.now());

            // Save the updated trip plan
            tripPlanRepository.save(trip);

            log.info("Successfully added place {} to {} for trip: {}, day: {}", 
                    place.getName(), normalizedType, tripId, day);

            return new AddPlaceResponse(
                    "success",
                    "Place added to itinerary successfully",
                    place.getPlaceId(),
                    tripId,
                    day,
                    normalizedType
            );

        } catch (Exception e) {
            log.error("Failed to add place to itinerary for trip: {}, day: {}, type: {}", 
                    tripId, day, type, e);
            throw new RuntimeException("Failed to add place to itinerary: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a SuggestionResponse DTO to a Place entity for persistence.
     * Maps relevant fields and handles missing data gracefully.
     *
     * @param suggestion The suggestion response to convert
     * @return Place entity ready for persistence
     */
    private Place convertSuggestionToPlace(SuggestionResponse suggestion) {
        Place place = new Place();
        
        // Basic information
        place.setName(suggestion.getName());
        place.setType(suggestion.getCategory());
        place.setPlaceId(suggestion.getId());
        place.setGooglePlaceId(suggestion.getGooglePlaceId());
        place.setSource(suggestion.getSource() != null ? suggestion.getSource() : "External API");
        place.setUserSelected(true); // Mark as user-selected since they're adding it
        
        // Location data
        if (suggestion.getLatitude() != null && suggestion.getLongitude() != null) {
            Location location = new Location();
            location.setLat(suggestion.getLatitude());
            location.setLng(suggestion.getLongitude());
            place.setLocation(location);
        }
        
        // Distance and rating
        place.setDistanceFromCenterKm(suggestion.getDistanceKm());
        place.setRating(suggestion.getRating());
        
        // Operational information
        place.setOpenHours(suggestion.getOpenHours());
        place.setPopularityLevel(suggestion.getPopularityLevel());
        
        // Media
        place.setThumbnailUrl(suggestion.getImage());
        
        // Activity and terrain tags from matched preferences
        place.setActivityTags(suggestion.getMatchedActivities() != null ? 
                suggestion.getMatchedActivities() : List.of());
        place.setTerrainTags(suggestion.getMatchedTerrains() != null ? 
                suggestion.getMatchedTerrains() : List.of());
        
        // Additional tags
        place.setWarnings(List.of()); // No warnings for user-selected places
        
        // Duration estimation based on type
        if (suggestion.getDuration() != null) {
            try {
                // Try to extract minutes from duration string (e.g., "3-4 hours" -> 180-240 minutes)
                String duration = suggestion.getDuration().toLowerCase();
                if (duration.contains("hour")) {
                    // Simple estimation: take first number found and multiply by 60
                    String[] parts = duration.split("\\D+");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        int hours = Integer.parseInt(parts[0]);
                        place.setVisitDurationMinutes(hours * 60);
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse duration '{}' for place '{}'", suggestion.getDuration(), suggestion.getName());
            }
        }
        
        // Default visit duration if not specified
        if (place.getVisitDurationMinutes() == null) {
            switch (suggestion.getCategory().toLowerCase()) {
                case "restaurant":
                case "cafe":
                    place.setVisitDurationMinutes(90); // 1.5 hours
                    break;
                case "hotel":
                    place.setVisitDurationMinutes(0); // Hotels don't have visit duration
                    break;
                default:
                    place.setVisitDurationMinutes(120); // 2 hours for attractions
            }
        }
        
        return place;
    }

    /**
     * Retrieves the complete trip plan information for a given trip ID.
     * Validates trip existence and user ownership before returning the trip details.
     *
     * @param tripId The ID of the trip to retrieve
     * @param userId The ID of the user requesting the trip information
     * @return TripPlanResponse containing the complete trip details
     * @throws TripNotFoundException if the trip doesn't exist
     * @throws UnauthorizedTripAccessException if the user doesn't own the trip
     */
    public TripPlanResponse getTripPlan(String tripId, String userId) {
        log.info("Retrieving trip plan for tripId: {} and userId: {}", tripId, userId);

        // Validate input parameters
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // Find the trip in database
        Optional<TripPlan> tripPlanOpt = tripPlanRepository.findById(tripId);
        if (tripPlanOpt.isEmpty()) {
            log.warn("Trip not found: {}", tripId);
            throw new TripNotFoundException("Trip not found with ID: " + tripId);
        }

        TripPlan tripPlan = tripPlanOpt.get();

        // Verify user authorization
        if (!tripPlan.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} for trip {}", userId, tripId);
            throw new UnauthorizedTripAccessException("You are not authorized to access this trip");
        }

        // Parse dates and calculate number of days
        LocalDate startDate = LocalDate.parse(tripPlan.getStartDate());
        LocalDate endDate = LocalDate.parse(tripPlan.getEndDate());
        int numberOfDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Create and return response
        TripPlanResponse response = new TripPlanResponse(
                tripPlan.getId(),
                tripPlan.getUserId(),
                tripPlan.getBaseCity(), // Using baseCity as destination
                startDate,
                endDate,
                numberOfDays,
                tripPlan.getDailyPlans()
        );

        log.info("Successfully retrieved trip plan for tripId: {} with {} daily plans", 
                tripId, tripPlan.getDailyPlans().size());

        return response;
    }

    // ...existing methods...
}
