package com.islandhop.trip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
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
    private static final String PLACES_NEW_API_BASE_URL = "https://places.googleapis.com/v1/places";

    /**
     * Search for places by text query (e.g., city name).
     * Uses the new Google Places API.
     *
     * @param query The search query
     * @return Location coordinates and place info
     */
    public Mono<GooglePlacesResponse> searchByText(String query) {
        // First try the new Places API with text search
        return searchByTextNewApi(query)
                .onErrorResume(error -> {
                    log.warn("New Places API failed, trying legacy API: {}", error.getMessage());
                    return searchByTextLegacyApi(query);
                });
    }

    /**
     * Search using the new Google Places API (v1).
     */
    private Mono<GooglePlacesResponse> searchByTextNewApi(String query) {
        try {
            log.info("🆕 Trying new Places API for: {}", query);
            
            // New Places API uses POST with JSON body
            String url = PLACES_NEW_API_BASE_URL + ":searchText";
            
            String requestBody = String.format("""
                {
                    "textQuery": "%s",
                    "maxResultCount": 10,
                    "locationBias": {
                        "rectangle": {
                            "low": {
                                "latitude": 5.9,
                                "longitude": 79.8
                            },
                            "high": {
                                "latitude": 9.8,
                                "longitude": 81.9
                            }
                        }
                    }
                }
                """, query);

            return webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", googlePlacesConfig.getApiKey())
                    .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.priceLevel,places.types,places.photos,places.regularOpeningHours")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class) // Get raw response first
                    .doOnSuccess(response -> log.debug("🆕 New Places API raw response: {}", response.substring(0, Math.min(500, response.length()))))
                    .map(this::convertNewApiResponse)
                    .timeout(Duration.ofSeconds(15));
        } catch (Exception e) {
            log.error("Failed to call new Places API: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * Search using the legacy Google Places API.
     */
    private Mono<GooglePlacesResponse> searchByTextLegacyApi(String query) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = String.format("%s/textsearch/json?query=%s&key=%s",
                    PLACES_API_BASE_URL, encodedQuery, googlePlacesConfig.getApiKey());

            log.debug("🔗 Legacy Google Places API URL: {}", url.replace(googlePlacesConfig.getApiKey(), "***API_KEY***"));
            log.debug("Searching Google Places (legacy) for: {} (encoded: {})", query, encodedQuery);

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GooglePlacesResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> {
                        log.debug("Google Places legacy text search completed for: {}", query);
                        if (response != null) {
                            log.debug("📊 Legacy API Response - Status: {}, Results: {}", 
                                    response.getStatus(), 
                                    response.getResults() != null ? response.getResults().size() : 0);
                        }
                    })
                    .doOnError(error -> log.error("Google Places legacy text search failed for {}: {}", query, error.getMessage()));
        } catch (Exception e) {
            log.error("Failed to encode query for Google Places legacy search: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * Convert new Places API response to legacy format.
     */
    private GooglePlacesResponse convertNewApiResponse(String rawResponse) {
        log.info("🔄 Converting new API response (length: {})", rawResponse.length());
        
        try {
            // Parse the JSON response from new Places API v1
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            
            GooglePlacesResponse response = new GooglePlacesResponse();
            response.setStatus("OK");
            
            List<GooglePlacesResponse.PlaceResult> results = new ArrayList<>();
            
            // Parse "places" array from new API
            JsonNode placesArray = rootNode.get("places");
            if (placesArray != null && placesArray.isArray()) {
                for (JsonNode placeNode : placesArray) {
                    GooglePlacesResponse.PlaceResult result = new GooglePlacesResponse.PlaceResult();
                    
                    // Set place ID
                    JsonNode idNode = placeNode.get("id");
                    if (idNode != null) {
                        result.setPlaceId(idNode.asText());
                    }
                    
                    // Set name from displayName
                    JsonNode displayNameNode = placeNode.get("displayName");
                    if (displayNameNode != null && displayNameNode.get("text") != null) {
                        result.setName(displayNameNode.get("text").asText());
                    }
                    
                    // Set formatted address
                    JsonNode addressNode = placeNode.get("formattedAddress");
                    if (addressNode != null) {
                        result.setFormattedAddress(addressNode.asText());
                    }
                    
                    // Set rating
                    JsonNode ratingNode = placeNode.get("rating");
                    if (ratingNode != null) {
                        result.setRating(ratingNode.asDouble());
                    }
                    
                    // Set user ratings total
                    JsonNode userRatingCountNode = placeNode.get("userRatingCount");
                    if (userRatingCountNode != null) {
                        result.setUserRatingsTotal(userRatingCountNode.asInt());
                    }
                    
                    // Set location (geometry)
                    JsonNode locationNode = placeNode.get("location");
                    if (locationNode != null) {
                        GooglePlacesResponse.Geometry geometry = new GooglePlacesResponse.Geometry();
                        GooglePlacesResponse.Location location = new GooglePlacesResponse.Location();
                        
                        JsonNode latNode = locationNode.get("latitude");
                        JsonNode lngNode = locationNode.get("longitude");
                        
                        if (latNode != null && lngNode != null) {
                            location.setLat(latNode.asDouble());
                            location.setLng(lngNode.asDouble());
                        }
                        
                        geometry.setLocation(location);
                        result.setGeometry(geometry);
                    }
                    
                    // Set types
                    JsonNode typesNode = placeNode.get("types");
                    if (typesNode != null && typesNode.isArray()) {
                        List<String> types = new ArrayList<>();
                        for (JsonNode typeNode : typesNode) {
                            types.add(typeNode.asText());
                        }
                        result.setTypes(types);
                    }
                    
                    // Set opening hours
                    JsonNode openingHoursNode = placeNode.get("regularOpeningHours");
                    if (openingHoursNode != null) {
                        GooglePlacesResponse.OpeningHours openingHours = new GooglePlacesResponse.OpeningHours();
                        
                        JsonNode openNowNode = openingHoursNode.get("openNow");
                        if (openNowNode != null) {
                            openingHours.setOpenNow(openNowNode.asBoolean());
                        }
                        
                        result.setOpeningHours(openingHours);
                    }
                    
                    results.add(result);
                    log.debug("✅ Parsed place: {} at {},{}", 
                            result.getName(), 
                            result.getGeometry() != null && result.getGeometry().getLocation() != null ? 
                                result.getGeometry().getLocation().getLat() : "null",
                            result.getGeometry() != null && result.getGeometry().getLocation() != null ? 
                                result.getGeometry().getLocation().getLng() : "null");
                }
            }
            
            response.setResults(results);
            log.info("✅ Successfully converted {} places from new API format", results.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("💥 Failed to parse new API response: {}", e.getMessage());
            log.debug("Raw response: {}", rawResponse.substring(0, Math.min(500, rawResponse.length())));
            
            // Fallback to empty response
            GooglePlacesResponse response = new GooglePlacesResponse();
            response.setStatus("ZERO_RESULTS");
            response.setResults(new ArrayList<>());
            return response;
        }
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

        log.info("🔍 Searching nearby {} places at {},{} within {}m", type, latitude, longitude, radius);
        log.debug("🔗 Nearby search URL: {}", url.replace(googlePlacesConfig.getApiKey(), "***API_KEY***"));

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GooglePlacesResponse.class)
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(response -> {
                    int resultCount = response.getResults() != null ? response.getResults().size() : 0;
                    log.info("📍 Found {} nearby {} places", resultCount, type);
                    if (response != null) {
                        log.debug("📊 Nearby API Response - Status: {}, Results: {}", 
                                response.getStatus(), resultCount);
                        if (response.getStatus() != null && !response.getStatus().equals("OK")) {
                            log.warn("⚠️ Google Places API returned status: {}", response.getStatus());
                        }
                    }
                })
                .doOnError(error -> log.error("💥 Nearby places search failed: {}", error.getMessage()));
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
