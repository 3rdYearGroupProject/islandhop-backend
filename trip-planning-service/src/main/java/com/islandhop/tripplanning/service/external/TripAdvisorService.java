package com.islandhop.tripplanning.service.external;

import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripAdvisorService {
    
    private final WebClient webClient;
    
    @Value("${tripadvisor.api.base-url}")
    private String baseUrl;
    
    @Value("${tripadvisor.api.key}")
    private String apiKey;
    
    /**
     * Search for attractions near a location
     */
    public List<PlannedPlace> searchAttractions(double latitude, double longitude, double radiusKm) {
        log.info("Searching attractions near lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);
        
        try {
            // Check if we should use actual API or mock data
            if (!"your-tripadvisor-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return searchAttractionsViaAPI(latitude, longitude, radiusKm);
            } else {
                log.info("Using mock data - TripAdvisor API key not configured");
                return getMockAttractions(latitude, longitude, radiusKm);
            }
        } catch (Exception e) {
            log.error("Error searching attractions: {}", e.getMessage(), e);
            // Fallback to mock data on error
            return getMockAttractions(latitude, longitude, radiusKm);
        }
    }
    
    /**
     * Search for hotels near a location
     */
    public List<PlannedPlace> searchHotels(double latitude, double longitude, double radiusKm) {
        log.info("Searching hotels near lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);
        
        try {
            if (!"your-tripadvisor-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return searchHotelsViaAPI(latitude, longitude, radiusKm);
            } else {
                return getMockHotels(latitude, longitude, radiusKm);
            }
        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage(), e);
            return getMockHotels(latitude, longitude, radiusKm);
        }
    }
    
    /**
     * Search for restaurants near a location
     */
    public List<PlannedPlace> searchRestaurants(double latitude, double longitude, double radiusKm) {
        log.info("Searching restaurants near lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);
        
        try {
            if (!"your-tripadvisor-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return searchRestaurantsViaAPI(latitude, longitude, radiusKm);
            } else {
                return getMockRestaurants(latitude, longitude, radiusKm);
            }
        } catch (Exception e) {
            log.error("Error searching restaurants: {}", e.getMessage(), e);
            return getMockRestaurants(latitude, longitude, radiusKm);
        }
    }
    
    /**
     * Search for places by name and optional city
     */
    public List<PlannedPlace> searchByName(String placeName, String city) {
        log.info("Searching for place: {} in city: {}", placeName, city);
        
        try {
            if (!"your-tripadvisor-key".equals(apiKey) && !"your-api-key-here".equals(apiKey)) {
                return searchByNameViaAPI(placeName, city);
            } else {
                return getMockPlacesByName(placeName, city);
            }
        } catch (Exception e) {
            log.error("Error searching by name: {}", e.getMessage(), e);
            return getMockPlacesByName(placeName, city);
        }
    }
    
    // Actual TripAdvisor API methods
    
    private List<PlannedPlace> searchAttractionsViaAPI(double latitude, double longitude, double radiusKm) {
        String url = baseUrl + "/locations/nearby_search";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("latLong", latitude + "," + longitude)
                            .queryParam("radius", (int)(radiusKm * 1000)) // Convert to meters
                            .queryParam("radiusUnit", "m")
                            .queryParam("category", "attractions")
                            .queryParam("language", "en")
                            .build())
                    .header("X-TripAdvisor-API-Key", apiKey)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertTripAdvisorResponseToPlaces(response, PlannedPlace.PlaceType.ATTRACTION);
            
        } catch (Exception e) {
            log.error("TripAdvisor API error for attractions: {}", e.getMessage());
            return getMockAttractions(latitude, longitude, radiusKm);
        }
    }
    
    private List<PlannedPlace> searchHotelsViaAPI(double latitude, double longitude, double radiusKm) {
        String url = baseUrl + "/locations/nearby_search";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("latLong", latitude + "," + longitude)
                            .queryParam("radius", (int)(radiusKm * 1000))
                            .queryParam("radiusUnit", "m")
                            .queryParam("category", "hotels")
                            .queryParam("language", "en")
                            .build())
                    .header("X-TripAdvisor-API-Key", apiKey)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertTripAdvisorResponseToPlaces(response, PlannedPlace.PlaceType.HOTEL);
            
        } catch (Exception e) {
            log.error("TripAdvisor API error for hotels: {}", e.getMessage());
            return getMockHotels(latitude, longitude, radiusKm);
        }
    }
    
    private List<PlannedPlace> searchRestaurantsViaAPI(double latitude, double longitude, double radiusKm) {
        String url = baseUrl + "/locations/nearby_search";
        
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("latLong", latitude + "," + longitude)
                            .queryParam("radius", (int)(radiusKm * 1000))
                            .queryParam("radiusUnit", "m")
                            .queryParam("category", "restaurants")
                            .queryParam("language", "en")
                            .build())
                    .header("X-TripAdvisor-API-Key", apiKey)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertTripAdvisorResponseToPlaces(response, PlannedPlace.PlaceType.RESTAURANT);
            
        } catch (Exception e) {
            log.error("TripAdvisor API error for restaurants: {}", e.getMessage());
            return getMockRestaurants(latitude, longitude, radiusKm);
        }
    }
    
    private List<PlannedPlace> searchByNameViaAPI(String placeName, String city) {
        String url = baseUrl + "/locations/search";
        
        try {
            String searchQuery = city != null ? placeName + " " + city : placeName;
            
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("searchQuery", searchQuery)
                            .queryParam("language", "en")
                            .build())
                    .header("X-TripAdvisor-API-Key", apiKey)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> response = responseMono.block();
            return convertTripAdvisorResponseToPlaces(response, null); // Mixed types
            
        } catch (Exception e) {
            log.error("TripAdvisor API error for search by name: {}", e.getMessage());
            return getMockPlacesByName(placeName, city);
        }
    }
    
    private List<PlannedPlace> convertTripAdvisorResponseToPlaces(Map<String, Object> response, PlannedPlace.PlaceType defaultType) {
        List<PlannedPlace> places = new ArrayList<>();
        
        if (response == null || !response.containsKey("data")) {
            return places;
        }
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        
        for (Map<String, Object> item : data) {
            try {
                PlannedPlace place = mapTripAdvisorItemToPlace(item, defaultType);
                if (place != null) {
                    places.add(place);
                }
            } catch (Exception e) {
                log.warn("Error mapping TripAdvisor item: {}", e.getMessage());
            }
        }
        
        return places;
    }
    
    private PlannedPlace mapTripAdvisorItemToPlace(Map<String, Object> item, PlannedPlace.PlaceType defaultType) {
        PlannedPlace place = new PlannedPlace();
        
        // Basic info
        place.setPlaceId((String) item.get("location_id"));
        place.setName((String) item.get("name"));
        place.setDescription((String) item.get("description"));
        
        // Location
        if (item.get("latitude") != null) {
            place.setLatitude(Double.parseDouble(item.get("latitude").toString()));
        }
        if (item.get("longitude") != null) {
            place.setLongitude(Double.parseDouble(item.get("longitude").toString()));
        }
        
        // Address
        Map<String, Object> address = (Map<String, Object>) item.get("address_obj");
        if (address != null) {
            place.setAddress(buildFullAddress(address));
            place.setCity((String) address.get("city"));
        }
        
        // Rating and reviews
        if (item.get("rating") != null) {
            place.setRating(Double.parseDouble(item.get("rating").toString()));
        }
        if (item.get("num_reviews") != null) {
            place.setReviewCount(Integer.parseInt(item.get("num_reviews").toString()));
        }
        
        // Place type
        place.setType(determinePlaceType(item, defaultType));
        
        // Categories based on TripAdvisor subcategory
        place.setCategories(mapTripAdvisorCategories(item));
        
        // Price level
        place.setPriceLevel(mapPriceLevel(item));
        
        // Visit duration estimate
        place.setEstimatedVisitDurationMinutes(estimateVisitDuration(place.getType()));
        
        // Contact info
        place.setPhoneNumber((String) item.get("phone"));
        place.setWebsite((String) item.get("website"));
        
        // Opening hours (if available)
        if (item.get("hours") != null) {
            place.setOpeningHours(parseOpeningHours(item.get("hours")));
        }
        
        place.setUserAdded(false);
        place.setConfirmed(false);
        
        return place;
    }
    
    // Mock data methods (remove when TripAdvisor API is configured)
    
    private List<PlannedPlace> getMockAttractions(double latitude, double longitude) {
        List<PlannedPlace> attractions = new ArrayList<>();
        
        // Sri Lanka specific mock attractions
        if (isNearColombo(latitude, longitude)) {
            attractions.addAll(getColomboAttractions());
        } else if (isNearKandy(latitude, longitude)) {
            attractions.addAll(getKandyAttractions());
        } else if (isNearGalle(latitude, longitude)) {
            attractions.addAll(getGalleAttractions());
        } else {
            // General attractions
            attractions.addAll(getGeneralAttractions(latitude, longitude));
        }
        
        return attractions;
    }
    
    private List<PlannedPlace> getMockHotels(double latitude, double longitude) {
        List<PlannedPlace> hotels = new ArrayList<>();
        
        PlannedPlace hotel1 = createMockPlace("H001", "Luxury Beach Resort", 
                latitude + 0.001, longitude + 0.001, PlannedPlace.PlaceType.HOTEL);
        hotel1.setRating(4.5);
        hotel1.setReviewCount(523);
        hotel1.setPriceLevel("EXPENSIVE");
        hotels.add(hotel1);
        
        PlannedPlace hotel2 = createMockPlace("H002", "Budget Friendly Inn", 
                latitude - 0.002, longitude + 0.002, PlannedPlace.PlaceType.HOTEL);
        hotel2.setRating(4.0);
        hotel2.setReviewCount(234);
        hotel2.setPriceLevel("BUDGET");
        hotels.add(hotel2);
        
        return hotels;
    }
    
    private List<PlannedPlace> getMockRestaurants(double latitude, double longitude) {
        List<PlannedPlace> restaurants = new ArrayList<>();
        
        PlannedPlace restaurant1 = createMockPlace("R001", "Local Cuisine Restaurant", 
                latitude + 0.0005, longitude - 0.0005, PlannedPlace.PlaceType.RESTAURANT);
        restaurant1.setRating(4.3);
        restaurant1.setReviewCount(345);
        restaurant1.setPriceLevel("MODERATE");
        restaurants.add(restaurant1);
        
        PlannedPlace restaurant2 = createMockPlace("R002", "Street Food Corner", 
                latitude - 0.001, longitude - 0.001, PlannedPlace.PlaceType.RESTAURANT);
        restaurant2.setRating(4.1);
        restaurant2.setReviewCount(156);
        restaurant2.setPriceLevel("BUDGET");
        restaurants.add(restaurant2);
        
        return restaurants;
    }
    
    private List<PlannedPlace> getColomboAttractions() {
        List<PlannedPlace> attractions = new ArrayList<>();
        
        PlannedPlace gangaramaya = createMockPlace("A001", "Gangaramaya Temple", 
                6.9162, 79.8562, PlannedPlace.PlaceType.ATTRACTION);
        gangaramaya.setCategories(List.of("Culture", "Religion"));
        gangaramaya.setRating(4.3);
        gangaramaya.setEstimatedVisitDurationMinutes(90);
        attractions.add(gangaramaya);
        
        PlannedPlace nationalMuseum = createMockPlace("A002", "National Museum of Colombo", 
                6.9094, 79.8606, PlannedPlace.PlaceType.ATTRACTION);
        nationalMuseum.setCategories(List.of("Culture", "History"));
        nationalMuseum.setRating(4.1);
        nationalMuseum.setEstimatedVisitDurationMinutes(120);
        attractions.add(nationalMuseum);
        
        PlannedPlace galleFace = createMockPlace("A003", "Galle Face Green", 
                6.9248, 79.8434, PlannedPlace.PlaceType.ATTRACTION);
        galleFace.setCategories(List.of("Nature", "Leisure"));
        galleFace.setRating(4.0);
        galleFace.setEstimatedVisitDurationMinutes(60);
        attractions.add(galleFace);
        
        return attractions;
    }
    
    private List<PlannedPlace> getKandyAttractions() {
        List<PlannedPlace> attractions = new ArrayList<>();
        
        PlannedPlace templeOfTooth = createMockPlace("A004", "Temple of the Sacred Tooth Relic", 
                7.2906, 80.6337, PlannedPlace.PlaceType.ATTRACTION);
        templeOfTooth.setCategories(List.of("Culture", "Religion"));
        templeOfTooth.setRating(4.6);
        templeOfTooth.setEstimatedVisitDurationMinutes(120);
        attractions.add(templeOfTooth);
        
        PlannedPlace botanicalGarden = createMockPlace("A005", "Royal Botanical Gardens", 
                7.2684, 80.5979, PlannedPlace.PlaceType.ATTRACTION);
        botanicalGarden.setCategories(List.of("Nature", "Garden"));
        botanicalGarden.setRating(4.5);
        botanicalGarden.setEstimatedVisitDurationMinutes(180);
        attractions.add(botanicalGarden);
        
        return attractions;
    }
    
    private List<PlannedPlace> getGalleAttractions() {
        List<PlannedPlace> attractions = new ArrayList<>();
        
        PlannedPlace galleFort = createMockPlace("A006", "Galle Dutch Fort", 
                6.0329, 80.2168, PlannedPlace.PlaceType.ATTRACTION);
        galleFort.setCategories(List.of("Culture", "History"));
        galleFort.setRating(4.4);
        galleFort.setEstimatedVisitDurationMinutes(150);
        attractions.add(galleFort);
        
        return attractions;
    }
    
    private List<PlannedPlace> getGeneralAttractions(double latitude, double longitude) {
        List<PlannedPlace> attractions = new ArrayList<>();
        
        PlannedPlace scenic = createMockPlace("A999", "Scenic Viewpoint", 
                latitude + 0.01, longitude + 0.01, PlannedPlace.PlaceType.VIEWPOINT);
        scenic.setCategories(List.of("Nature", "Photography"));
        scenic.setRating(4.2);
        scenic.setEstimatedVisitDurationMinutes(45);
        attractions.add(scenic);
        
        return attractions;
    }
    
    private PlannedPlace createMockPlace(String id, String name, double lat, double lng, PlannedPlace.PlaceType type) {
        PlannedPlace place = new PlannedPlace();
        place.setPlaceId(id);
        place.setName(name);
        place.setLatitude(lat);
        place.setLongitude(lng);
        place.setType(type);
        place.setCategories(new ArrayList<>());
        place.setUserAdded(false);
        place.setConfirmed(false);
        return place;
    }
    
    private boolean isNearColombo(double lat, double lng) {
        return Math.abs(lat - 6.9271) < 0.5 && Math.abs(lng - 79.8612) < 0.5;
    }
    
    private boolean isNearKandy(double lat, double lng) {
        return Math.abs(lat - 7.2906) < 0.5 && Math.abs(lng - 80.6337) < 0.5;
    }
    
    private boolean isNearGalle(double lat, double lng) {
        return Math.abs(lat - 6.0329) < 0.5 && Math.abs(lng - 80.2168) < 0.5;
    }
    
    private PlannedPlace mapToPlannedPlace(Map<String, Object> response) {
        // Map TripAdvisor API response to PlannedPlace
        PlannedPlace place = new PlannedPlace();
        
        place.setPlaceId((String) response.get("location_id"));
        place.setName((String) response.get("name"));
        place.setDescription((String) response.get("description"));
        
        // Handle address
        Map<String, Object> address = (Map<String, Object>) response.get("address_obj");
        if (address != null) {
            place.setAddress((String) address.get("street1"));
            place.setCity((String) address.get("city"));
        }
        
        // Handle coordinates
        if (response.get("latitude") != null) {
            place.setLatitude(Double.parseDouble(response.get("latitude").toString()));
        }
        if (response.get("longitude") != null) {
            place.setLongitude(Double.parseDouble(response.get("longitude").toString()));
        }
        
        // Handle rating
        if (response.get("rating") != null) {
            place.setRating(Double.parseDouble(response.get("rating").toString()));
        }
        
        // Handle review count
        if (response.get("num_reviews") != null) {
            place.setReviewCount(Integer.parseInt(response.get("num_reviews").toString()));
        }
        
        return place;
    }
}
