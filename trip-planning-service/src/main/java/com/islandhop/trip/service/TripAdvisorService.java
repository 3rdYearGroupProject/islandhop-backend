package com.islandhop.trip.service;

import com.islandhop.trip.config.ExternalApiConfig;
import com.islandhop.trip.dto.SuggestionResponse;
import com.islandhop.trip.dto.external.TripAdvisorResponse;
import com.islandhop.trip.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for integrating with TripAdvisor Content API.
 * Handles location search and nearby attractions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripAdvisorService {

    private final WebClient webClient;
    private final ExternalApiConfig.TripAdvisorConfig tripAdvisorConfig;

    private static final String TRIPADVISOR_API_BASE_URL = "https://api.content.tripadvisor.com/api/v1/location";

    /**
     * Search for locations by text query.
     *
     * @param query The search query
     * @return TripAdvisor search response
     */
    public Mono<TripAdvisorResponse> searchLocations(String query) {
        String url = String.format("%s/search?key=%s&searchQuery=%s&language=en",
                TRIPADVISOR_API_BASE_URL, tripAdvisorConfig.getApiKey(), query);

        log.debug("Searching TripAdvisor locations for: {}", query);

        return webClient.get()
                .uri(url)
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(TripAdvisorResponse.class)
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(response -> log.debug("TripAdvisor location search completed for: {}", query))
                .doOnError(error -> log.error("TripAdvisor location search failed for {}: {}", query, error.getMessage()));
    }

    /**
     * Find nearby attractions for a specific location.
     *
     * @param locationId The TripAdvisor location ID
     * @return List of nearby attractions
     */
    public Mono<TripAdvisorResponse> findNearbyAttractions(String locationId) {
        String url = String.format("%s/%s/nearby_search?key=%s&language=en&category=attractions&radius=15",
                TRIPADVISOR_API_BASE_URL, locationId, tripAdvisorConfig.getApiKey());

        log.debug("Searching TripAdvisor nearby attractions for location: {}", locationId);

        return webClient.get()
                .uri(url)
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(TripAdvisorResponse.class)
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(response -> log.debug("Found {} nearby attractions for location {}",
                        response.getData() != null ? response.getData().size() : 0, locationId))
                .doOnError(error -> log.error("TripAdvisor nearby search failed for location {}: {}", locationId, error.getMessage()));
    }

    /**
     * Get detailed information about a specific location.
     *
     * @param locationId The TripAdvisor location ID
     * @return Detailed location information
     */
    public Mono<TripAdvisorResponse.LocationData> getLocationDetails(String locationId) {
        String url = String.format("%s/%s/details?key=%s&language=en",
                TRIPADVISOR_API_BASE_URL, locationId, tripAdvisorConfig.getApiKey());

        log.debug("Getting TripAdvisor location details for: {}", locationId);

        return webClient.get()
                .uri(url)
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(TripAdvisorResponse.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> response.getData() != null && !response.getData().isEmpty()
                        ? response.getData().get(0) : null)
                .doOnSuccess(result -> log.debug("Location details retrieved for: {}", locationId))
                .doOnError(error -> log.error("Location details failed for {}: {}", locationId, error.getMessage()));
    }

    /**
     * Convert TripAdvisor location data to SuggestionResponse.
     *
     * @param attractions List of TripAdvisor location data
     * @param centerLat Center latitude for distance calculation
     * @param centerLng Center longitude for distance calculation
     * @return List of suggestions
     */
    public List<SuggestionResponse> convertToSuggestions(List<TripAdvisorResponse.LocationData> attractions,
                                                        double centerLat, double centerLng) {
        return attractions.stream()
                .map(attraction -> convertLocationToSuggestion(attraction, centerLat, centerLng))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private SuggestionResponse convertLocationToSuggestion(TripAdvisorResponse.LocationData attraction,
                                                          double centerLat, double centerLng) {
        try {
            SuggestionResponse suggestion = new SuggestionResponse();

            suggestion.setId("tripadvisor_" + attraction.getLocationId());
            suggestion.setName(attraction.getName());
            suggestion.setDescription(attraction.getDescription());
            suggestion.setCategory("Attraction");
            suggestion.setSource("TripAdvisor");

            // Set location details
            if (attraction.getAddressObj() != null) {
                StringBuilder address = new StringBuilder();
                if (attraction.getAddressObj().getStreet1() != null) {
                    address.append(attraction.getAddressObj().getStreet1());
                }
                if (attraction.getAddressObj().getCity() != null) {
                    if (address.length() > 0) address.append(", ");
                    address.append(attraction.getAddressObj().getCity());
                }
                suggestion.setAddress(address.toString());
                suggestion.setLocation(attraction.getAddressObj().getCity());
            }

            // Set coordinates and calculate distance
            if (attraction.getLatitude() != null && attraction.getLongitude() != null) {
                try {
                    double lat = Double.parseDouble(attraction.getLatitude());
                    double lng = Double.parseDouble(attraction.getLongitude());
                    
                    // Validate coordinates before setting
                    if (GeoUtils.areValidCoordinates(lat, lng)) {
                        suggestion.setLatitude(lat);
                        suggestion.setLongitude(lng);

                        double distance = GeoUtils.calculateDistance(centerLat, centerLng, lat, lng);
                        suggestion.setDistanceKm(distance);
                    } else {
                        log.warn("Invalid coordinates for attraction {}: lat={}, lng={}",
                                attraction.getName(), attraction.getLatitude(), attraction.getLongitude());
                        suggestion.setDistanceKm(Double.MAX_VALUE); // Push to end of sorted list
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid coordinate format for attraction {}: lat={}, lng={}",
                            attraction.getName(), attraction.getLatitude(), attraction.getLongitude());
                    suggestion.setDistanceKm(Double.MAX_VALUE); // Push to end of sorted list
                }
            } else {
                log.warn("Missing coordinates for attraction: {}", attraction.getName());
                suggestion.setDistanceKm(Double.MAX_VALUE); // Push to end of sorted list
            }

            // Set rating if available
            if (attraction.getRating() != null) {
                try {
                    suggestion.setRating(Double.parseDouble(attraction.getRating()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid rating for attraction {}: {}", attraction.getName(), attraction.getRating());
                }
            }

            // Set review count if available
            if (attraction.getNumReviews() != null) {
                try {
                    suggestion.setReviews(Integer.parseInt(attraction.getNumReviews()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid review count for attraction {}: {}", attraction.getName(), attraction.getNumReviews());
                }
            }

            // Set image if available
            if (attraction.getPhoto() != null && attraction.getPhoto().getImages() != null &&
                    !attraction.getPhoto().getImages().isEmpty()) {
                suggestion.setImage(attraction.getPhoto().getImages().get(0).getUrl());
            }

            // Set web URL
            suggestion.setBookingUrl(attraction.getWebUrl());

            // Set opening hours if available
            if (attraction.getHours() != null && attraction.getHours().getWeekdayText() != null &&
                    !attraction.getHours().getWeekdayText().isEmpty()) {
                suggestion.setOpenHours(String.join(", ", attraction.getHours().getWeekdayText()));
            }

            // Set default values
            suggestion.setDuration("2-3 hours");
            suggestion.setPrice(mapTripAdvisorPrice(attraction.getPriceLevel()));
            suggestion.setIsRecommended(suggestion.getRating() != null && suggestion.getRating() >= 4.0);
            suggestion.setPopularityLevel(suggestion.getReviews() != null && suggestion.getReviews() >= 100 ? "High" : "Medium");

            return suggestion;
        } catch (Exception e) {
            log.error("Error converting TripAdvisor attraction {}: {}", attraction.getName(), e.getMessage());
            return null;
        }
    }

    private String mapTripAdvisorPrice(String priceLevel) {
        if (priceLevel == null) return "$10-25";
        return switch (priceLevel.toLowerCase()) {
            case "$", "inexpensive" -> "$5-15";
            case "$$", "moderate" -> "$15-30";
            case "$$$", "expensive" -> "$30-50";
            case "$$$$", "very expensive" -> "$50+";
            default -> "$10-25";
        };
    }

    /**
     * Handle API errors gracefully.
     */
    public List<SuggestionResponse> handleApiError(Throwable error, String city) {
        if (error instanceof WebClientResponseException webEx) {
            log.error("TripAdvisor API error for {}: {} - {}", city, webEx.getStatusCode(), webEx.getResponseBodyAsString());
        } else {
            log.error("TripAdvisor API error for {}: {}", city, error.getMessage());
        }
        return Collections.emptyList();
    }
}
