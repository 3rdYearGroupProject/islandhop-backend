package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.dto.AddPlaceRequest;
import com.islandhop.tripplanning.model.PlannedPlace;
import com.islandhop.tripplanning.service.external.GooglePlacesService;
import com.islandhop.tripplanning.service.external.TripAdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced location service that combines multiple data sources and provides 
 * robust location search and validation capabilities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    
    private final GooglePlacesService googlePlacesService;
    private final TripAdvisorService tripAdvisorService;
    
    /**
     * Search for locations by text query with intelligent fallback
     */
    public List<LocationSearchResult> searchLocations(String query, String city, Double biasLat, Double biasLng, Integer maxResults) {
        log.info("Searching locations for query: '{}' in city: '{}' with bias: {},{}", query, city, biasLat, biasLng);
        
        List<LocationSearchResult> results = new ArrayList<>();
        String fullQuery = buildSearchQuery(query, city);
        
        try {
            // Primary: Google Places search
            List<GooglePlacesService.LocationSearchResult> googleResults = 
                googlePlacesService.searchPlacesByText(fullQuery, biasLat, biasLng, maxResults);
            
            for (GooglePlacesService.LocationSearchResult googleResult : googleResults) {
                LocationSearchResult result = convertFromGoogleResult(googleResult);
                if (result != null && isValidSriLankanLocation(result)) {
                    results.add(result);
                }
            }
            
            // Secondary: TripAdvisor search if we need more results
            if (results.size() < (maxResults != null ? maxResults : 10)) {
                List<PlannedPlace> tripAdvisorResults = tripAdvisorService.searchByName(query, city);
                
                for (PlannedPlace place : tripAdvisorResults) {
                    LocationSearchResult result = convertFromTripAdvisorResult(place);
                    if (result != null && isValidSriLankanLocation(result) && !isDuplicate(results, result)) {
                        results.add(result);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error in location search: {}", e.getMessage(), e);
        }
        
        // Sort by relevance and distance if bias location provided
        if (biasLat != null && biasLng != null) {
            results = sortByDistance(results, biasLat, biasLng);
        }
        
        // Limit results
        if (maxResults != null && results.size() > maxResults) {
            results = results.subList(0, maxResults);
        }
        
        log.info("Found {} location results for query: '{}'", results.size(), query);
        return results;
    }
    
    /**
     * Validate and enrich a place request with additional location data
     */
    public PlaceValidationResult validateAndEnrichPlace(AddPlaceRequest request) {
        log.info("Validating and enriching place: {}", request.getPlaceName());
        
        PlaceValidationResult validation = new PlaceValidationResult();
        validation.setOriginalRequest(request);
        validation.setValid(false);
        
        try {
            // If coordinates provided, validate them
            if (request.getLatitude() != null && request.getLongitude() != null) {
                if (!isValidCoordinates(request.getLatitude(), request.getLongitude())) {
                    validation.addWarning("Coordinates are outside Sri Lanka bounds");
                    return validation;
                }
                
                // Reverse geocode to validate and get formatted address
                GooglePlacesService.GeocodingResult geocodingResult = 
                    googlePlacesService.reverseGeocode(request.getLatitude(), request.getLongitude());
                
                if (geocodingResult != null) {
                    validation.setFormattedAddress(geocodingResult.getFormattedAddress());
                    validation.setValid(true);
                }
            } else {
                // No coordinates provided, try to find the place
                List<LocationSearchResult> searchResults = searchLocations(
                    request.getPlaceName(), request.getCity(), null, null, 5);
                
                if (searchResults.isEmpty()) {
                    validation.addError("Could not find location for: " + request.getPlaceName());
                    return validation;
                }
                
                LocationSearchResult bestMatch = findBestMatch(searchResults, request.getPlaceName());
                if (bestMatch != null) {
                    validation.setSuggestedCoordinates(bestMatch.getLatitude(), bestMatch.getLongitude());
                    validation.setFormattedAddress(bestMatch.getFormattedAddress());
                    validation.setPlaceId(bestMatch.getPlaceId());
                    validation.setSuggestions(searchResults);
                    validation.setValid(true);
                    
                    if (searchResults.size() > 1) {
                        validation.addWarning("Multiple locations found, using best match");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error validating place: {}", e.getMessage(), e);
            validation.addError("Error during location validation: " + e.getMessage());
        }
        
        return validation;
    }
    
    /**
     * Get detailed information about a place by place ID
     */
    public PlaceDetails getPlaceDetails(String placeId) {
        log.info("Getting detailed information for place ID: {}", placeId);
        
        try {
            GooglePlacesService.PlaceDetails googleDetails = googlePlacesService.getPlaceDetails(placeId);
            
            if (googleDetails != null) {
                PlaceDetails details = new PlaceDetails();
                details.setPlaceId(googleDetails.getPlaceId());
                details.setName(googleDetails.getName());
                details.setFormattedAddress(googleDetails.getFormattedAddress());
                details.setLatitude(googleDetails.getLatitude());
                details.setLongitude(googleDetails.getLongitude());
                details.setRating(googleDetails.getRating());
                details.setTypes(googleDetails.getTypes());
                details.setPlaceType(inferPlaceType(googleDetails.getTypes()));
                return details;
            }
            
        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    // Helper methods
    
    private String buildSearchQuery(String query, String city) {
        if (city != null && !city.trim().isEmpty() && !query.toLowerCase().contains(city.toLowerCase())) {
            return query + " " + city + " Sri Lanka";
        }
        if (!query.toLowerCase().contains("sri lanka")) {
            return query + " Sri Lanka";
        }
        return query;
    }
    
    private boolean isValidSriLankanLocation(LocationSearchResult result) {
        if (result.getLatitude() == null || result.getLongitude() == null) {
            return false;
        }
        return googlePlacesService.isWithinSriLanka(result.getLatitude(), result.getLongitude());
    }
    
    private boolean isValidCoordinates(double latitude, double longitude) {
        return googlePlacesService.isWithinSriLanka(latitude, longitude);
    }
    
    private boolean isDuplicate(List<LocationSearchResult> results, LocationSearchResult newResult) {
        return results.stream().anyMatch(existing -> 
            areSimilarLocations(existing, newResult));
    }
    
    private boolean areSimilarLocations(LocationSearchResult loc1, LocationSearchResult loc2) {
        if (loc1.getLatitude() != null && loc1.getLongitude() != null &&
            loc2.getLatitude() != null && loc2.getLongitude() != null) {
            
            double distance = calculateDistance(
                loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude());
            
            return distance < 0.1; // Less than 100 meters
        }
        
        // Fallback to name comparison
        return loc1.getName() != null && loc2.getName() != null &&
               loc1.getName().toLowerCase().equals(loc2.getName().toLowerCase());
    }
    
    private List<LocationSearchResult> sortByDistance(List<LocationSearchResult> results, double biasLat, double biasLng) {
        return results.stream()
                .peek(result -> {
                    if (result.getLatitude() != null && result.getLongitude() != null) {
                        double distance = calculateDistance(biasLat, biasLng, result.getLatitude(), result.getLongitude());
                        result.setDistanceFromBias(distance);
                    }
                })
                .sorted((a, b) -> {
                    if (a.getDistanceFromBias() != null && b.getDistanceFromBias() != null) {
                        return Double.compare(a.getDistanceFromBias(), b.getDistanceFromBias());
                    }
                    // Fallback to rating
                    if (a.getRating() != null && b.getRating() != null) {
                        return Double.compare(b.getRating(), a.getRating());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }
    
    private LocationSearchResult findBestMatch(List<LocationSearchResult> results, String queryName) {
        if (results.isEmpty()) return null;
        
        // First, try exact name match
        for (LocationSearchResult result : results) {
            if (result.getName() != null && 
                result.getName().toLowerCase().equals(queryName.toLowerCase())) {
                return result;
            }
        }
        
        // Then try partial match
        for (LocationSearchResult result : results) {
            if (result.getName() != null && 
                result.getName().toLowerCase().contains(queryName.toLowerCase())) {
                return result;
            }
        }
        
        // Default to first result
        return results.get(0);
    }
    
    private PlannedPlace.PlaceType inferPlaceType(List<String> googleTypes) {
        if (googleTypes == null || googleTypes.isEmpty()) {
            return PlannedPlace.PlaceType.ATTRACTION;
        }
        
        for (String type : googleTypes) {
            switch (type.toLowerCase()) {
                case "lodging":
                case "hotel":
                    return PlannedPlace.PlaceType.HOTEL;
                case "restaurant":
                case "food":
                case "meal_takeaway":
                    return PlannedPlace.PlaceType.RESTAURANT;
                case "shopping_mall":
                case "store":
                    return PlannedPlace.PlaceType.SHOPPING;
                case "tourist_attraction":
                case "museum":
                case "amusement_park":
                    return PlannedPlace.PlaceType.ATTRACTION;
                case "natural_feature":
                case "park":
                    return PlannedPlace.PlaceType.VIEWPOINT;
                case "transit_station":
                case "bus_station":
                case "airport":
                    return PlannedPlace.PlaceType.TRANSPORT_HUB;
                default:
                    continue;
            }
        }
        
        return PlannedPlace.PlaceType.ATTRACTION;
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
    
    private LocationSearchResult convertFromGoogleResult(GooglePlacesService.LocationSearchResult googleResult) {
        LocationSearchResult result = new LocationSearchResult();
        result.setPlaceId(googleResult.getPlaceId());
        result.setName(googleResult.getName());
        result.setFormattedAddress(googleResult.getFormattedAddress());
        result.setLatitude(googleResult.getLatitude());
        result.setLongitude(googleResult.getLongitude());
        result.setRating(googleResult.getRating());
        result.setTypes(googleResult.getTypes());
        result.setSource("Google Places");
        return result;
    }
    
    private LocationSearchResult convertFromTripAdvisorResult(PlannedPlace place) {
        LocationSearchResult result = new LocationSearchResult();
        result.setPlaceId(place.getPlaceId());
        result.setName(place.getName());
        result.setFormattedAddress(place.getCity() != null ? place.getCity() + ", Sri Lanka" : "Sri Lanka");
        result.setLatitude(place.getLatitude());
        result.setLongitude(place.getLongitude());
        result.setTypes(place.getCategories());
        result.setSource("TripAdvisor");
        return result;
    }
    
    // Data classes
    
    public static class LocationSearchResult {
        private String placeId;
        private String name;
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        private Double rating;
        private List<String> types;
        private String source;
        private Double distanceFromBias;
        
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
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public Double getDistanceFromBias() { return distanceFromBias; }
        public void setDistanceFromBias(Double distanceFromBias) { this.distanceFromBias = distanceFromBias; }
    }
    
    public static class PlaceValidationResult {
        private AddPlaceRequest originalRequest;
        private boolean valid;
        private Double suggestedLatitude;
        private Double suggestedLongitude;
        private String formattedAddress;
        private String placeId;
        private List<LocationSearchResult> suggestions;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        
        public void setSuggestedCoordinates(double lat, double lng) {
            this.suggestedLatitude = lat;
            this.suggestedLongitude = lng;
        }
        
        // Getters and setters
        public AddPlaceRequest getOriginalRequest() { return originalRequest; }
        public void setOriginalRequest(AddPlaceRequest originalRequest) { this.originalRequest = originalRequest; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public Double getSuggestedLatitude() { return suggestedLatitude; }
        public void setSuggestedLatitude(Double suggestedLatitude) { this.suggestedLatitude = suggestedLatitude; }
        
        public Double getSuggestedLongitude() { return suggestedLongitude; }
        public void setSuggestedLongitude(Double suggestedLongitude) { this.suggestedLongitude = suggestedLongitude; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        
        public List<LocationSearchResult> getSuggestions() { return suggestions; }
        public void setSuggestions(List<LocationSearchResult> suggestions) { this.suggestions = suggestions; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
    
    public static class PlaceDetails {
        private String placeId;
        private String name;
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        private Double rating;
        private List<String> types;
        private PlannedPlace.PlaceType placeType;
        
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
        
        public PlannedPlace.PlaceType getPlaceType() { return placeType; }
        public void setPlaceType(PlannedPlace.PlaceType placeType) { this.placeType = placeType; }
    }
}
