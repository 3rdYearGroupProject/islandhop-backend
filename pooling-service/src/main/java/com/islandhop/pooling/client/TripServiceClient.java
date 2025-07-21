package com.islandhop.pooling.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * REST client for communicating with the Trip Planning Service microservice.
 * Handles trip plan retrieval and data extraction.
 */
@Service
@Slf4j
public class TripServiceClient {
    
    private final RestTemplate restTemplate;
    private final String tripServiceBaseUrl;
    
    public TripServiceClient(RestTemplate restTemplate, 
                           @Value("${services.trip-service.base-url:http://localhost:8082}") String tripServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.tripServiceBaseUrl = tripServiceBaseUrl;
    }
    
    /**
     * Gets trip plan details by trip ID.
     * 
     * @param tripId The trip ID
     * @param userId The user ID for authentication
     * @return TripDetails containing cities, attractions, etc., or null if not found
     */
    public TripDetails getTripDetails(String tripId, String userId) {
        try {
            String url = tripServiceBaseUrl + "/api/v1/itinerary/" + tripId + "?userId=" + userId;
            log.debug("Fetching trip details from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                return mapToTripDetails(response);
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Trip not found for ID: {} and user: {}", tripId, userId);
        } catch (Exception e) {
            log.error("Error fetching trip details for ID {} and user {}: {}", tripId, userId, e.getMessage());
        }
        
        return null;
    }

    /**
     * Updates the city for a specific day in a trip itinerary.
     * 
     * @param tripId The trip ID
     * @param day The day number (1-based)
     * @param userId The user ID for authentication
     * @param cityName The new city name
     * @return true if successful, false otherwise
     */
    public boolean updateCityForDay(String tripId, int day, String userId, String cityName) {
        try {
            String url = tripServiceBaseUrl + "/api/v1/itinerary/" + tripId + "/day/" + day + "/city";
            log.debug("Updating city for trip {} day {} to: {}", tripId, day, cityName);
            
            UpdateCityRequest request = new UpdateCityRequest(userId, cityName);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            return response != null && "success".equals(response.get("status"));
            
        } catch (Exception e) {
            log.error("Error updating city for trip {} day {}: {}", tripId, day, e.getMessage());
            return false;
        }
    }

    /**
     * Adds a place to a specific day and type in the trip itinerary.
     * 
     * @param tripId The trip ID
     * @param day The day number (1-based)
     * @param type The type of place (attractions, hotels, restaurants)
     * @param userId The user ID for authentication
     * @param place The place details to add
     * @return true if successful, false otherwise
     */
    public boolean addPlaceToItinerary(String tripId, int day, String type, String userId, PlaceDetails place) {
        try {
            String url = tripServiceBaseUrl + "/api/v1/itinerary/" + tripId + "/day/" + day + "/" + type + "?userId=" + userId;
            log.debug("Adding place {} to trip {} day {} type {}", place.getName(), tripId, day, type);
            
            SuggestionRequest suggestionRequest = mapToSuggestionRequest(place);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, suggestionRequest, Map.class);
            
            return response != null && "success".equals(response.get("status"));
            
        } catch (Exception e) {
            log.error("Error adding place to trip {} day {} type {}: {}", tripId, day, type, e.getMessage());
            return false;
        }
    }

    private SuggestionRequest mapToSuggestionRequest(PlaceDetails place) {
        SuggestionRequest suggestion = new SuggestionRequest();
        suggestion.setId(place.getId());
        suggestion.setName(place.getName());
        suggestion.setAddress(place.getAddress());
        suggestion.setPrice(place.getPrice());
        suggestion.setPriceLevel(place.getPriceLevel());
        suggestion.setCategory(place.getCategory());
        suggestion.setRating(place.getRating());
        suggestion.setReviews(place.getReviews());
        suggestion.setPopularityLevel(place.getPopularityLevel());
        suggestion.setImage(place.getImage());
        suggestion.setLatitude(place.getLatitude());
        suggestion.setLongitude(place.getLongitude());
        suggestion.setDistanceKm(place.getDistanceKm());
        suggestion.setIsOpenNow(place.getIsOpenNow());
        suggestion.setSource(place.getSource());
        suggestion.setGooglePlaceId(place.getGooglePlaceId());
        suggestion.setIsRecommended(place.getIsRecommended());
        
        return suggestion;
    }
    
    private TripDetails mapToTripDetails(Map<String, Object> response) {
        TripDetails details = new TripDetails();
        
        details.setTripId((String) response.get("id"));
        details.setTripName((String) response.get("tripName"));
        details.setStartDate((String) response.get("startDate"));
        details.setEndDate((String) response.get("endDate"));
        details.setBaseCity((String) response.get("baseCity"));
        details.setBudgetLevel((String) response.get("budgetLevel"));
        details.setActivityPacing((String) response.get("activityPacing"));
        
        @SuppressWarnings("unchecked")
        List<String> preferredActivities = (List<String>) response.get("preferredActivities");
        details.setPreferredActivities(preferredActivities != null ? preferredActivities : new ArrayList<>());
        
        @SuppressWarnings("unchecked")
        List<String> preferredTerrains = (List<String>) response.get("preferredTerrains");
        details.setPreferredTerrains(preferredTerrains != null ? preferredTerrains : new ArrayList<>());
        
        // Extract cities and attractions from daily plans
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyPlans = (List<Map<String, Object>>) response.get("dailyPlans");
        if (dailyPlans != null) {
            details.setCities(extractCities(dailyPlans));
            details.setTopAttractions(extractTopAttractions(dailyPlans));
        }
        
        return details;
    }
    
    private List<String> extractCities(List<Map<String, Object>> dailyPlans) {
        return dailyPlans.stream()
                .map(plan -> (String) plan.get("city"))
                .filter(city -> city != null && !city.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<String> extractTopAttractions(List<Map<String, Object>> dailyPlans) {
        List<String> attractions = new ArrayList<>();
        
        for (Map<String, Object> plan : dailyPlans) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attractionsList = (List<Map<String, Object>>) plan.get("attractions");
            
            if (attractionsList != null) {
                for (Map<String, Object> attraction : attractionsList) {
                    String name = (String) attraction.get("name");
                    Boolean userSelected = (Boolean) attraction.get("userSelected");
                    
                    if (name != null && Boolean.TRUE.equals(userSelected)) {
                        attractions.add(name);
                    }
                }
            }
        }
        
        // Return top 3 attractions
        return attractions.stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }
    
    @Data
    public static class TripDetails {
        private String tripId;
        private String tripName;
        private String startDate;
        private String endDate;
        private String baseCity;
        private String budgetLevel;
        private String activityPacing;
        private List<String> preferredActivities = new ArrayList<>();
        private List<String> preferredTerrains = new ArrayList<>();
        private List<String> cities = new ArrayList<>();
        private List<String> topAttractions = new ArrayList<>();
    }

    @Data
    public static class UpdateCityRequest {
        private String userId;
        private String city;
        
        public UpdateCityRequest(String userId, String city) {
            this.userId = userId;
            this.city = city;
        }
    }

    @Data
    public static class SuggestionRequest {
        private String id;
        private String name;
        private String location;
        private String address;
        private String price;
        private String priceRange;
        private String priceLevel;
        private String cuisine;
        private String duration;
        private String category;
        private Double rating;
        private Integer reviews;
        private String popularityLevel;
        private String image;
        private String description;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
        private String openHours;
        private Boolean isOpenNow;
        private String phone;
        private String website;
        private String source;
        private String externalId;
        private String googlePlaceId;
        private String bookingUrl;
        private Boolean isRecommended;
    }

    @Data
    public static class PlaceDetails {
        private String id;
        private String name;
        private String address;
        private String price;
        private String priceLevel;
        private String category;
        private Double rating;
        private Integer reviews;
        private String popularityLevel;
        private String image;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
        private Boolean isOpenNow;
        private String source;
        private String googlePlaceId;
        private Boolean isRecommended;
    }
}
