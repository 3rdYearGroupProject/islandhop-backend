package com.islandhop.tripplanning.service.external;

import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for integrating with Google Places API for location search and validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlacesService {
    
    private final WebClient webClient;
    
    @Value("${api.google.places.key:your-google-places-key}")
    private String apiKey;
    
    @Value("${api.google.places.url:https://maps.googleapis.com/maps/api/place}")
    private String baseUrl;
    
    /**
     * Search for places by text query with optional location bias
     */
    public List<LocationSearchResult> searchPlacesByText(String query, Double biasLat, Double biasLng, Integer maxResults) {
        log.info("Searching places by text: '{}' with bias lat: {}, lng: {}", query, biasLat, biasLng);
        
        try {
            if (!"your-google-places-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return searchPlacesByTextViaAPI(query, biasLat, biasLng, maxResults);
            } else {
                log.warn("Google Places API key not configured, returning mock results");
                return getMockSearchResults(query);
            }
        } catch (Exception e) {
            log.error("Error searching places by text: {}", e.getMessage(), e);
            return getMockSearchResults(query);
        }
    }
    
    /**
     * Get place details by place ID
     */
    public PlaceDetails getPlaceDetails(String placeId) {
        log.info("Getting place details for place ID: {}", placeId);
        
        try {
            if (!"your-google-places-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return getPlaceDetailsViaAPI(placeId);
            } else {
                log.warn("Google Places API key not configured, returning mock details");
                return getMockPlaceDetails(placeId);
            }
        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage(), e);
            return getMockPlaceDetails(placeId);
        }
    }
    
    /**
     * Geocode an address to get coordinates
     */
    public GeocodingResult geocodeAddress(String address) {
        log.info("Geocoding address: {}", address);
        
        try {
            if (!"your-google-places-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return geocodeAddressViaAPI(address);
            } else {
                log.warn("Google Places API key not configured, returning mock geocoding");
                return getMockGeocodingResult(address);
            }
        } catch (Exception e) {
            log.error("Error geocoding address: {}", e.getMessage(), e);
            return getMockGeocodingResult(address);
        }
    }
    
    /**
     * Reverse geocode coordinates to get address
     */
    public GeocodingResult reverseGeocode(double latitude, double longitude) {
        log.info("Reverse geocoding coordinates: {}, {}", latitude, longitude);
        
        try {
            if (!"your-google-places-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return reverseGeocodeViaAPI(latitude, longitude);
            } else {
                log.warn("Google Places API key not configured, returning mock reverse geocoding");
                return getMockReverseGeocodingResult(latitude, longitude);
            }
        } catch (Exception e) {
            log.error("Error reverse geocoding: {}", e.getMessage(), e);
            return getMockReverseGeocodingResult(latitude, longitude);
        }
    }
    
    /**
     * Validate if coordinates are within Sri Lanka bounds
     */
    public boolean isWithinSriLanka(double latitude, double longitude) {
        // Sri Lanka approximate bounds
        double minLat = 5.9;
        double maxLat = 9.9;
        double minLng = 79.6;
        double maxLng = 81.9;
        
        return latitude >= minLat && latitude <= maxLat && 
               longitude >= minLng && longitude <= maxLng;
    }
    
    // Google Places API implementation methods
    
    private List<LocationSearchResult> searchPlacesByTextViaAPI(String query, Double biasLat, Double biasLng, Integer maxResults) {
        String url = baseUrl + "/textsearch/json";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path(url)
                                .queryParam("query", query)
                                .queryParam("key", apiKey)
                                .queryParam("language", "en");
                        
                        if (biasLat != null && biasLng != null) {
                            builder = builder.queryParam("location", biasLat + "," + biasLng)
                                   .queryParam("radius", "50000"); // 50km radius
                        }
                        
                        if (maxResults != null) {
                            builder = builder.queryParam("pagesize", Math.min(maxResults, 20));
                        }
                        
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertGooglePlacesResponseToSearchResults(response);
            
        } catch (Exception e) {
            log.error("Google Places API error for text search: {}", e.getMessage());
            return getMockSearchResults(query);
        }
    }
    
    private PlaceDetails getPlaceDetailsViaAPI(String placeId) {
        String url = baseUrl + "/details/json";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("place_id", placeId)
                            .queryParam("key", apiKey)
                            .queryParam("fields", "name,formatted_address,geometry,place_id,rating,types,opening_hours,photos,price_level,reviews")
                            .queryParam("language", "en")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertGooglePlaceDetailsResponse(response);
            
        } catch (Exception e) {
            log.error("Google Places API error for place details: {}", e.getMessage());
            return getMockPlaceDetails(placeId);
        }
    }
    
    private GeocodingResult geocodeAddressViaAPI(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .queryParam("region", "lk") // Bias to Sri Lanka
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertGeocodingResponse(response);
            
        } catch (Exception e) {
            log.error("Google Geocoding API error: {}", e.getMessage());
            return getMockGeocodingResult(address);
        }
    }
    
    private GeocodingResult reverseGeocodeViaAPI(double latitude, double longitude) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("latlng", latitude + "," + longitude)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertGeocodingResponse(response);
            
        } catch (Exception e) {
            log.error("Google Reverse Geocoding API error: {}", e.getMessage());
            return getMockReverseGeocodingResult(latitude, longitude);
        }
    }
    
    // Response conversion methods
    
    private List<LocationSearchResult> convertGooglePlacesResponseToSearchResults(Map<String, Object> response) {
        List<LocationSearchResult> results = new ArrayList<>();
        
        if (response == null || !"OK".equals(response.get("status"))) {
            return results;
        }
        
        List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("results");
        if (places == null) return results;
        
        for (Map<String, Object> place : places) {
            try {
                LocationSearchResult result = new LocationSearchResult();
                result.setPlaceId((String) place.get("place_id"));
                result.setName((String) place.get("name"));
                result.setFormattedAddress((String) place.get("formatted_address"));
                
                Map<String, Object> geometry = (Map<String, Object>) place.get("geometry");
                if (geometry != null) {
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    if (location != null) {
                        result.setLatitude(((Number) location.get("lat")).doubleValue());
                        result.setLongitude(((Number) location.get("lng")).doubleValue());
                    }
                }
                
                if (place.get("rating") != null) {
                    result.setRating(((Number) place.get("rating")).doubleValue());
                }
                
                List<String> types = (List<String>) place.get("types");
                if (types != null) {
                    result.setTypes(types);
                }
                
                results.add(result);
            } catch (Exception e) {
                log.warn("Error converting place result: {}", e.getMessage());
            }
        }
        
        return results;
    }
    
    private PlaceDetails convertGooglePlaceDetailsResponse(Map<String, Object> response) {
        if (response == null || !"OK".equals(response.get("status"))) {
            return null;
        }
        
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        if (result == null) return null;
        
        PlaceDetails details = new PlaceDetails();
        details.setPlaceId((String) result.get("place_id"));
        details.setName((String) result.get("name"));
        details.setFormattedAddress((String) result.get("formatted_address"));
        
        Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
        if (geometry != null) {
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            if (location != null) {
                details.setLatitude(((Number) location.get("lat")).doubleValue());
                details.setLongitude(((Number) location.get("lng")).doubleValue());
            }
        }
        
        if (result.get("rating") != null) {
            details.setRating(((Number) result.get("rating")).doubleValue());
        }
        
        List<String> types = (List<String>) result.get("types");
        if (types != null) {
            details.setTypes(types);
        }
        
        return details;
    }
    
    private GeocodingResult convertGeocodingResponse(Map<String, Object> response) {
        if (response == null || !"OK".equals(response.get("status"))) {
            return null;
        }
        
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results == null || results.isEmpty()) return null;
        
        Map<String, Object> firstResult = results.get(0);
        
        GeocodingResult result = new GeocodingResult();
        result.setFormattedAddress((String) firstResult.get("formatted_address"));
        
        Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
        if (geometry != null) {
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            if (location != null) {
                result.setLatitude(((Number) location.get("lat")).doubleValue());
                result.setLongitude(((Number) location.get("lng")).doubleValue());
            }
        }
        
        return result;
    }
    
    // Mock data methods for testing
    
    private List<LocationSearchResult> getMockSearchResults(String query) {
        List<LocationSearchResult> results = new ArrayList<>();
        
        // Mock Sri Lankan places based on query
        if (query.toLowerCase().contains("colombo")) {
            results.add(createMockResult("mock_1", "Gangaramaya Temple", "Colombo 02, Sri Lanka", 6.9164, 79.8561, 4.5));
            results.add(createMockResult("mock_2", "Galle Face Green", "Colombo 03, Sri Lanka", 6.9271, 79.8442, 4.2));
        } else if (query.toLowerCase().contains("kandy")) {
            results.add(createMockResult("mock_3", "Temple of the Sacred Tooth Relic", "Kandy, Sri Lanka", 7.2936, 80.6408, 4.6));
            results.add(createMockResult("mock_4", "Royal Botanic Gardens", "Peradeniya, Sri Lanka", 7.2691, 80.5967, 4.4));
        } else {
            results.add(createMockResult("mock_general", query, "Sri Lanka", 7.8731, 80.7718, 4.0));
        }
        
        return results;
    }
    
    private LocationSearchResult createMockResult(String id, String name, String address, double lat, double lng, double rating) {
        LocationSearchResult result = new LocationSearchResult();
        result.setPlaceId(id);
        result.setName(name);
        result.setFormattedAddress(address);
        result.setLatitude(lat);
        result.setLongitude(lng);
        result.setRating(rating);
        result.setTypes(List.of("tourist_attraction", "point_of_interest"));
        return result;
    }
    
    private PlaceDetails getMockPlaceDetails(String placeId) {
        PlaceDetails details = new PlaceDetails();
        details.setPlaceId(placeId);
        details.setName("Mock Place");
        details.setFormattedAddress("Sri Lanka");
        details.setLatitude(7.8731);
        details.setLongitude(80.7718);
        details.setRating(4.0);
        details.setTypes(List.of("tourist_attraction"));
        return details;
    }
    
    private GeocodingResult getMockGeocodingResult(String address) {
        GeocodingResult result = new GeocodingResult();
        result.setFormattedAddress(address + ", Sri Lanka");
        result.setLatitude(7.8731);
        result.setLongitude(80.7718);
        return result;
    }
    
    private GeocodingResult getMockReverseGeocodingResult(double latitude, double longitude) {
        GeocodingResult result = new GeocodingResult();
        result.setFormattedAddress("Sri Lanka");
        result.setLatitude(latitude);
        result.setLongitude(longitude);
        return result;
    }
    
    // Data transfer objects
    
    public static class LocationSearchResult {
        private String placeId;
        private String name;
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        private Double rating;
        private List<String> types;
        
        // Getters and setters
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }
    
    public static class PlaceDetails {
        private String placeId;
        private String name;
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        private Double rating;
        private List<String> types;
        
        // Getters and setters
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }
    
    public static class GeocodingResult {
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        
        // Getters and setters
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
}
