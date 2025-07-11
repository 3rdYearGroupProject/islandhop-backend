package com.islandhop.trip.service;

import com.islandhop.trip.config.ExternalApiConfig;
import com.islandhop.trip.dto.SuggestionResponse;
import com.islandhop.trip.dto.external.GooglePlacesResponse;
import com.islandhop.trip.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for integrating with Google Places API.
 * Handles location search, nearby places, and place details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlacesService {

    private final WebClient webClient;
    private final ExternalApiConfig.GooglePlacesConfig googlePlacesConfig;

    private static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place";

    /**
     * Search for places by text query (e.g., city name).
     *
     * @param query The search query
     * @return Location coordinates and place info
     */
    public Mono<GooglePlacesResponse> searchByText(String query) {
        String url = String.format("%s/textsearch/json?query=%s&key=%s",
                PLACES_API_BASE_URL, query, googlePlacesConfig.getApiKey());

        log.debug("Searching Google Places for: {}", query);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GooglePlacesResponse.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.debug("Google Places text search completed for: {}", query))
                .doOnError(error -> log.error("Google Places text search failed for {}: {}", query, error.getMessage()));
    }

    /**
     * Find nearby places of a specific type.
     *
     * @param latitude The latitude
     * @param longitude The longitude
     * @param radius The search radius in meters
     * @param type The place type (e.g., restaurant, tourist_attraction, lodging)
     * @return List of nearby places
     */
    public Mono<GooglePlacesResponse> findNearbyPlaces(double latitude, double longitude, 
                                                      int radius, String type) {
        String url = String.format("%s/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
                PLACES_API_BASE_URL, latitude, longitude, radius, type, googlePlacesConfig.getApiKey());

        log.debug("Searching nearby {} places at {},{} within {}m", type, latitude, longitude, radius);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GooglePlacesResponse.class)
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(response -> log.debug("Found {} nearby {} places", 
                        response.getResults() != null ? response.getResults().size() : 0, type))
                .doOnError(error -> log.error("Nearby places search failed: {}", error.getMessage()));
    }

    /**
     * Get detailed information about a specific place.
     *
     * @param placeId The Google Place ID
     * @return Detailed place information
     */
    public Mono<GooglePlacesResponse.PlaceResult> getPlaceDetails(String placeId) {
        String url = String.format("%s/details/json?place_id=%s&fields=name,rating,formatted_phone_number," +
                "formatted_address,geometry,opening_hours,photos,price_level,types,user_ratings_total,website&key=%s",
                PLACES_API_BASE_URL, placeId, googlePlacesConfig.getApiKey());

        log.debug("Getting place details for: {}", placeId);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GooglePlacesResponse.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> response.getResults() != null && !response.getResults().isEmpty() 
                        ? response.getResults().get(0) : null)
                .doOnSuccess(result -> log.debug("Place details retrieved for: {}", placeId))
                .doOnError(error -> log.error("Place details failed for {}: {}", placeId, error.getMessage()));
    }

    /**
     * Get photo URL from photo reference.
     *
     * @param photoReference The photo reference from Google Places
     * @param maxWidth Maximum width of the photo
     * @return Photo URL
     */
    public String getPhotoUrl(String photoReference, int maxWidth) {
        return String.format("%s/photo?photoreference=%s&maxwidth=%d&key=%s",
                PLACES_API_BASE_URL, photoReference, maxWidth, googlePlacesConfig.getApiKey());
    }

    /**
     * Convert Google Places results to SuggestionResponse.
     *
     * @param places List of Google Places results
     * @param type The suggestion type
     * @param centerLat Center latitude for distance calculation
     * @param centerLng Center longitude for distance calculation
     * @return List of suggestions
     */
    public List<SuggestionResponse> convertToSuggestions(List<GooglePlacesResponse.PlaceResult> places,
                                                        String type, double centerLat, double centerLng) {
        return places.stream()
                .filter(place -> place.getBusinessStatus() == null || 
                               !"CLOSED_PERMANENTLY".equals(place.getBusinessStatus()))
                .map(place -> convertPlaceToSuggestion(place, type, centerLat, centerLng))
                .collect(Collectors.toList());
    }

    private SuggestionResponse convertPlaceToSuggestion(GooglePlacesResponse.PlaceResult place, 
                                                       String type, double centerLat, double centerLng) {
        SuggestionResponse suggestion = new SuggestionResponse();
        
        suggestion.setId("google_" + place.getPlaceId());
        suggestion.setName(place.getName());
        suggestion.setAddress(place.getFormattedAddress() != null ? place.getFormattedAddress() : place.getVicinity());
        suggestion.setRating(place.getRating());
        suggestion.setReviews(place.getUserRatingsTotal());
        suggestion.setGooglePlaceId(place.getPlaceId());
        suggestion.setSource("Google Places");

        // Set location
        if (place.getGeometry() != null && place.getGeometry().getLocation() != null) {
            double lat = place.getGeometry().getLocation().getLat();
            double lng = place.getGeometry().getLocation().getLng();
            
            // Validate coordinates before setting
            if (GeoUtils.areValidCoordinates(lat, lng)) {
                suggestion.setLatitude(lat);
                suggestion.setLongitude(lng);
                
                // Calculate distance using centralized utility
                double distance = GeoUtils.calculateDistance(centerLat, centerLng, lat, lng);
                suggestion.setDistanceKm(distance);
            } else {
                log.warn("Invalid coordinates for place {}: lat={}, lng={}", place.getName(), lat, lng);
                suggestion.setDistanceKm(Double.MAX_VALUE); // Push to end of sorted list
            }
        } else {
            log.warn("Missing geometry/location for place: {}", place.getName());
            suggestion.setDistanceKm(Double.MAX_VALUE); // Push to end of sorted list
        }

        // Set opening hours
        if (place.getOpeningHours() != null) {
            suggestion.setIsOpenNow(place.getOpeningHours().getOpenNow());
            if (place.getOpeningHours().getWeekdayText() != null && 
                !place.getOpeningHours().getWeekdayText().isEmpty()) {
                suggestion.setOpenHours(String.join(", ", place.getOpeningHours().getWeekdayText()));
            }
        }

        // Set image
        if (place.getPhotos() != null && !place.getPhotos().isEmpty()) {
            String photoRef = place.getPhotos().get(0).getPhotoReference();
            suggestion.setImage(getPhotoUrl(photoRef, 400));
        }

        // Set type-specific fields
        switch (type) {
            case "restaurants" -> {
                suggestion.setCategory("Restaurant");
                suggestion.setPriceLevel(mapPriceLevel(place.getPriceLevel()));
                suggestion.setPriceRange(mapPriceRange(place.getPriceLevel()));
                // Try to determine cuisine from types
                if (place.getTypes() != null) {
                    suggestion.setCuisine(determineCuisine(place.getTypes()));
                }
            }
            case "hotels" -> {
                suggestion.setCategory("Hotel");
                suggestion.setPriceLevel(mapPriceLevel(place.getPriceLevel()));
                suggestion.setPrice(mapHotelPrice(place.getPriceLevel()));
            }
            case "attractions" -> {
                suggestion.setCategory("Attraction");
                suggestion.setDuration("2-3 hours");
                suggestion.setPrice(mapAttractionPrice(place.getPriceLevel()));
            }
        }

        // Set recommendation status based on rating and review count
        suggestion.setIsRecommended(isHighlyRecommended(place.getRating(), place.getUserRatingsTotal()));
        suggestion.setPopularityLevel(determinePopularityLevel(place.getUserRatingsTotal()));

        return suggestion;
    }

    private String mapPriceLevel(Integer priceLevel) {
        if (priceLevel == null) return "Medium";
        return switch (priceLevel) {
            case 0, 1 -> "Low";
            case 2 -> "Medium";
            case 3, 4 -> "High";
            default -> "Medium";
        };
    }

    private String mapPriceRange(Integer priceLevel) {
        if (priceLevel == null) return "$15-30";
        return switch (priceLevel) {
            case 0 -> "Under $10";
            case 1 -> "$10-20";
            case 2 -> "$20-40";
            case 3 -> "$40-80";
            case 4 -> "$80+";
            default -> "$15-30";
        };
    }

    private String mapHotelPrice(Integer priceLevel) {
        if (priceLevel == null) return "$80-150/night";
        return switch (priceLevel) {
            case 0, 1 -> "$30-80/night";
            case 2 -> "$80-150/night";
            case 3 -> "$150-300/night";
            case 4 -> "$300+/night";
            default -> "$80-150/night";
        };
    }

    private String mapAttractionPrice(Integer priceLevel) {
        if (priceLevel == null) return "$10";
        return switch (priceLevel) {
            case 0 -> "Free";
            case 1 -> "$5-10";
            case 2 -> "$10-25";
            case 3 -> "$25-50";
            case 4 -> "$50+";
            default -> "$10";
        };
    }

    private String determineCuisine(List<String> types) {
        // Map Google Place types to cuisine types
        for (String type : types) {
            switch (type.toLowerCase()) {
                case "chinese_restaurant" -> { return "Chinese"; }
                case "indian_restaurant" -> { return "Indian"; }
                case "japanese_restaurant" -> { return "Japanese"; }
                case "italian_restaurant" -> { return "Italian"; }
                case "thai_restaurant" -> { return "Thai"; }
                case "seafood_restaurant" -> { return "Seafood"; }
                case "vegetarian_restaurant" -> { return "Vegetarian"; }
                case "fast_food_restaurant" -> { return "Fast Food"; }
                case "cafe" -> { return "Cafe"; }
                case "bakery" -> { return "Bakery"; }
            }
        }
        return "International";
    }

    private boolean isHighlyRecommended(Double rating, Integer reviewCount) {
        return (rating != null && rating >= 4.5) && (reviewCount != null && reviewCount >= 100);
    }

    private String determinePopularityLevel(Integer reviewCount) {
        if (reviewCount == null) return "Medium";
        if (reviewCount >= 500) return "High";
        if (reviewCount >= 100) return "Medium";
        return "Low";
    }

    /**
     * Map suggestion type to Google Places type.
     *
     * @param suggestionType The suggestion type (attractions, hotels, restaurants)
     * @return Google Places type
     */
    public String mapToGooglePlaceType(String suggestionType) {
        return switch (suggestionType) {
            case "attractions" -> "tourist_attraction";
            case "hotels" -> "lodging";
            case "restaurants" -> "restaurant";
            default -> throw new IllegalArgumentException("Unknown suggestion type: " + suggestionType);
        };
    }
}
