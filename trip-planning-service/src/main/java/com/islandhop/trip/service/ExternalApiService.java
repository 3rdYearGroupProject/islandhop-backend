package com.islandhop.trip.service;

import com.islandhop.trip.config.ExternalApiConfig;
import com.islandhop.trip.dto.SuggestionResponse;
import com.islandhop.trip.dto.external.GooglePlacesResponse;
import com.islandhop.trip.dto.external.TripAdvisorResponse;
import com.islandhop.trip.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for integrating with external APIs (TripAdvisor, Google Places).
 * Provides methods to fetch suggestions for attractions, hotels, and restaurants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final WebClient webClient;
    private final ExternalApiConfig.TripAdvisorConfig tripAdvisorConfig;
    private final ExternalApiConfig.GooglePlacesConfig googlePlacesConfig;
    private final GooglePlacesService googlePlacesService;
    private final TripAdvisorService tripAdvisorService;

    private static final String TRIPADVISOR_API_BASE_URL = "https://api.content.tripadvisor.com/api/v1/location";
    private static final int DEFAULT_RADIUS = 15000; // 15km radius
    private static final int MAX_RESULTS = 10;

    // Enhanced activity keyword mappings for better preference matching
    private static final Map<String, List<String>> ACTIVITY_KEYWORDS = Map.of(
        "sightseeing", List.of("temple", "fort", "palace", "monument", "tourist", "attraction", "landmark"),
        "cultural tours", List.of("museum", "temple", "heritage", "cultural", "history", "traditional", "ancient"),
        "photography", List.of("scenic", "view", "lookout", "panoramic", "beautiful", "picturesque"),
        "hiking", List.of("trail", "peak", "mountain", "trek", "hiking", "climb", "nature walk"),
        "wildlife watching", List.of("park", "safari", "wildlife", "animals", "bird", "nature", "sanctuary"),
        "adventure", List.of("adventure", "thrill", "exciting", "extreme", "zip", "rock", "water sports"),
        "relaxation", List.of("spa", "beach", "garden", "peaceful", "tranquil", "quiet", "serene")
    );

    // Enhanced terrain keyword mappings
    private static final Map<String, List<String>> TERRAIN_KEYWORDS = Map.of(
        "historical", List.of("fort", "palace", "ancient", "historical", "heritage", "colonial", "ruins"),
        "cultural", List.of("temple", "museum", "cultural", "traditional", "religious", "spiritual"),
        "natural", List.of("park", "garden", "lake", "natural", "nature", "forest", "waterfall"),
        "adventure", List.of("adventure", "exciting", "thrill", "extreme", "sports", "activity"),
        "beach", List.of("beach", "coastal", "ocean", "sea", "marine", "shore"),
        "mountain", List.of("mountain", "hill", "peak", "highland", "elevation", "summit")
    );

    /**
     * Fetches suggestions from external APIs for the specified type and city.
     * Uses real TripAdvisor and Google Places API calls with fallback to mock data.
     *
     * @param type The type of suggestions (attractions, hotels, restaurants)
     * @param city The city to get suggestions for
     * @param preferredActivities User preferred activities for filtering
     * @param preferredTerrains User preferred terrains for filtering
     * @param budgetLevel User budget level (Low, Medium, High)
     * @return List of suggestions
     */
    public List<SuggestionResponse> fetchSuggestions(String type, String city, 
                                                   List<String> preferredActivities, 
                                                   List<String> preferredTerrains, 
                                                   String budgetLevel) {
        try {
            // Check if API keys are configured (not demo keys)
            if (isApiConfigured()) {
                log.info("Fetching {} suggestions for {} from external APIs", type, city);
                return fetchFromExternalApis(type, city, preferredActivities, preferredTerrains, budgetLevel);
            } else {
                log.warn("Using mock data due to unconfigured API keys for {} in {}", type, city);
                return generateMockSuggestions(type, city, preferredActivities, preferredTerrains, budgetLevel);
            }
        } catch (Exception e) {
            log.warn("API failure, falling back to mock data for {} in {}: {}", type, city, e.getMessage());
            return generateMockSuggestions(type, city, preferredActivities, preferredTerrains, budgetLevel);
        }
    }

    /**
     * Checks if external API keys are properly configured.
     */
    private boolean isApiConfigured() {
        return !tripAdvisorConfig.getApiKey().equals("demo-key") && 
               !googlePlacesConfig.getApiKey().equals("demo-key");
    }

    /**
     * Fetches suggestions from real external APIs.
     */
    private List<SuggestionResponse> fetchFromExternalApis(String type, String city,
                                                         List<String> preferredActivities,
                                                         List<String> preferredTerrains,
                                                         String budgetLevel) {
        try {
            // Get city location first
            GooglePlacesResponse.PlaceResult cityLocation = getCityLocation(city);
            if (cityLocation == null || cityLocation.getGeometry() == null) {
                log.warn("Could not find location for city: {}", city);
                return generateMockSuggestions(type, city, preferredActivities, preferredTerrains, budgetLevel);
            }

            double lat = cityLocation.getGeometry().getLocation().getLat();
            double lng = cityLocation.getGeometry().getLocation().getLng();
            
            log.info("Found coordinates for {}: {}, {}", city, lat, lng);

            // Fetch suggestions based on type
            return switch (type) {
                case "attractions" -> fetchAttractions(city, lat, lng, preferredActivities, preferredTerrains);
                case "hotels" -> fetchHotels(city, lat, lng, budgetLevel);
                case "restaurants" -> fetchRestaurants(city, lat, lng, budgetLevel, preferredTerrains);
                default -> throw new IllegalArgumentException("Invalid suggestion type: " + type);
            };
        } catch (Exception e) {
            log.error("Error fetching from external APIs for {} in {}: {}", type, city, e.getMessage());
            throw e;
        }
    }

    /**
     * Get city location using Google Places API.
     */
    private GooglePlacesResponse.PlaceResult getCityLocation(String city) {
        try {
            String query = city + " Sri Lanka";
            GooglePlacesResponse response = googlePlacesService.searchByText(query).block();
            
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return response.getResults().get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get city location for {}: {}", city, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch attractions using TripAdvisor API as primary source and Google Places as fallback.
     */
    private List<SuggestionResponse> fetchAttractions(String city, double lat, double lng, 
                                                     List<String> preferredActivities, 
                                                     List<String> preferredTerrains) {
        try {
            // First try TripAdvisor API for attractions
            List<SuggestionResponse> tripAdvisorResults = fetchTripAdvisorAttractions(city, lat, lng);
            
            if (!tripAdvisorResults.isEmpty()) {
                log.info("Found {} attractions from TripAdvisor for {}", tripAdvisorResults.size(), city);
                return filterAndEnhanceAttractions(tripAdvisorResults, preferredActivities, preferredTerrains)
                        .stream().limit(MAX_RESULTS).collect(Collectors.toList());
            }
            
            // Fallback to Google Places
            log.info("TripAdvisor returned no results, falling back to Google Places for attractions in {}", city);
            return fetchGooglePlacesAttractions(lat, lng, preferredActivities, preferredTerrains);
            
        } catch (Exception e) {
            log.error("Error fetching attractions for {}: {}", city, e.getMessage());
            // Fallback to Google Places on error
            try {
                return fetchGooglePlacesAttractions(lat, lng, preferredActivities, preferredTerrains);
            } catch (Exception ex) {
                log.error("Google Places fallback also failed: {}", ex.getMessage());
                return Collections.emptyList();
            }
        }
    }

    /**
     * Fetch hotels primarily from Google Places API.
     */
    private List<SuggestionResponse> fetchHotels(String city, double lat, double lng, String budgetLevel) {
        try {
            GooglePlacesResponse response = googlePlacesService.findNearbyPlaces(
                    lat, lng, DEFAULT_RADIUS, "lodging").block();
            
            if (response != null && response.getResults() != null) {
                List<SuggestionResponse> hotels = googlePlacesService.convertToSuggestions(
                        response.getResults(), "hotels", lat, lng);
                
                log.info("Found {} hotels from Google Places for {}", hotels.size(), city);
                return filterHotelsByBudget(hotels, budgetLevel)
                        .stream()
                        .sorted((h1, h2) -> Double.compare(h2.getRating() != null ? h2.getRating() : 0.0, 
                                                          h1.getRating() != null ? h1.getRating() : 0.0))
                        .limit(MAX_RESULTS)
                        .collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching hotels for {}: {}", city, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch restaurants from Google Places API.
     */
    private List<SuggestionResponse> fetchRestaurants(String city, double lat, double lng, 
                                                     String budgetLevel, List<String> preferredTerrains) {
        try {
            GooglePlacesResponse response = googlePlacesService.findNearbyPlaces(
                    lat, lng, DEFAULT_RADIUS, "restaurant").block();
            
            if (response != null && response.getResults() != null) {
                List<SuggestionResponse> restaurants = googlePlacesService.convertToSuggestions(
                        response.getResults(), "restaurants", lat, lng);
                
                log.info("Found {} restaurants from Google Places for {}", restaurants.size(), city);
                return filterRestaurantsByBudget(restaurants, budgetLevel)
                        .stream()
                        .sorted((r1, r2) -> Double.compare(r2.getRating() != null ? r2.getRating() : 0.0, 
                                                          r1.getRating() != null ? r1.getRating() : 0.0))
                        .limit(MAX_RESULTS)
                        .collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching restaurants for {}: {}", city, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch attractions from TripAdvisor API.
     */
    private List<SuggestionResponse> fetchTripAdvisorAttractions(String city, double lat, double lng) {
        try {
            // Search for the city location
            TripAdvisorResponse searchResponse = tripAdvisorService.searchLocations(city + " Sri Lanka").block();
            
            if (searchResponse != null && searchResponse.getData() != null && !searchResponse.getData().isEmpty()) {
                // Find the main city location
                TripAdvisorResponse.LocationData cityData = searchResponse.getData().stream()
                        .filter(location -> location.getName().toLowerCase().contains(city.toLowerCase()))
                        .findFirst()
                        .orElse(searchResponse.getData().get(0));
                
                if (cityData != null && cityData.getLocationId() != null) {
                    // Get attractions near this location
                    TripAdvisorResponse attractionsResponse = tripAdvisorService.findNearbyAttractions(cityData.getLocationId()).block();
                    
                    if (attractionsResponse != null && attractionsResponse.getData() != null) {
                        return tripAdvisorService.convertToSuggestions(attractionsResponse.getData(), lat, lng);
                    }
                }
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            return tripAdvisorService.handleApiError(e, city);
        }
    }

    /**
     * Fetch attractions from Google Places as fallback.
     */
    private List<SuggestionResponse> fetchGooglePlacesAttractions(double lat, double lng, 
                                                                 List<String> preferredActivities, 
                                                                 List<String> preferredTerrains) {
        try {
            GooglePlacesResponse response = googlePlacesService.findNearbyPlaces(
                    lat, lng, DEFAULT_RADIUS, "tourist_attraction").block();
            
            if (response != null && response.getResults() != null) {
                List<SuggestionResponse> attractions = googlePlacesService.convertToSuggestions(
                        response.getResults(), "attractions", lat, lng);
                
                log.info("Found {} attractions from Google Places", attractions.size());
                return filterAndEnhanceAttractions(attractions, preferredActivities, preferredTerrains)
                        .stream().limit(MAX_RESULTS).collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching Google Places attractions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Filter and enhance attractions based on user preferences.
     */
    private List<SuggestionResponse> filterAndEnhanceAttractions(List<SuggestionResponse> attractions,
                                                               List<String> preferredActivities,
                                                               List<String> preferredTerrains) {
        return attractions.stream()
                .peek(attraction -> {
                    // Set matched activities based on attraction type/category
                    List<String> matchedActivities = findMatchingActivities(attraction, preferredActivities);
                    attraction.setMatchedActivities(matchedActivities);
                    
                    // Set matched terrains
                    List<String> matchedTerrains = findMatchingTerrains(attraction, preferredTerrains);
                    attraction.setMatchedTerrains(matchedTerrains);
                })
                .sorted((a1, a2) -> {
                    // Sort by preference matches first, then by rating
                    int score1 = calculateAttractionScore(a1);
                    int score2 = calculateAttractionScore(a2);
                    if (score1 != score2) return Integer.compare(score2, score1);
                    return Double.compare(a2.getRating() != null ? a2.getRating() : 0.0,
                                        a1.getRating() != null ? a1.getRating() : 0.0);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filter hotels by budget level.
     */
    private List<SuggestionResponse> filterHotelsByBudget(List<SuggestionResponse> hotels, String budgetLevel) {
        return hotels.stream()
                .filter(hotel -> matchesBudgetLevel(hotel.getPriceLevel(), budgetLevel))
                .collect(Collectors.toList());
    }

    /**
     * Filter restaurants by budget level.
     */
    private List<SuggestionResponse> filterRestaurantsByBudget(List<SuggestionResponse> restaurants, String budgetLevel) {
        return restaurants.stream()
                .filter(restaurant -> matchesBudgetLevel(restaurant.getPriceLevel(), budgetLevel))
                .collect(Collectors.toList());
    }

    /**
     * Find matching activities for an attraction using enhanced keyword mapping.
     */
    private List<String> findMatchingActivities(SuggestionResponse attraction, List<String> preferredActivities) {
        if (preferredActivities == null || preferredActivities.isEmpty()) {
            return Collections.emptyList();
        }
        
        String content = (attraction.getName() + " " + 
                         (attraction.getCategory() != null ? attraction.getCategory() : "") + " " + 
                         (attraction.getDescription() != null ? attraction.getDescription() : "")).toLowerCase();
        
        List<String> matches = preferredActivities.stream()
                .filter(activity -> {
                    String activityLower = activity.toLowerCase();
                    // Direct match or keyword match
                    if (content.contains(activityLower)) {
                        return true;
                    }
                    // Enhanced keyword matching
                    List<String> keywords = ACTIVITY_KEYWORDS.get(activityLower);
                    return keywords != null && keywords.stream().anyMatch(content::contains);
                })
                .collect(Collectors.toList());
        
        if (matches.isEmpty() && !preferredActivities.isEmpty()) {
            log.debug("No activity matches found for {} with preferences: {}", 
                     attraction.getName(), preferredActivities);
        }
        
        return matches;
    }

    /**
     * Find matching terrains for an attraction using enhanced keyword mapping.
     */
    private List<String> findMatchingTerrains(SuggestionResponse attraction, List<String> preferredTerrains) {
        if (preferredTerrains == null || preferredTerrains.isEmpty()) {
            return Collections.emptyList();
        }
        
        String content = (attraction.getName() + " " + 
                         (attraction.getCategory() != null ? attraction.getCategory() : "") + " " + 
                         (attraction.getDescription() != null ? attraction.getDescription() : "")).toLowerCase();
        
        List<String> matches = preferredTerrains.stream()
                .filter(terrain -> {
                    String terrainLower = terrain.toLowerCase();
                    // Direct match or keyword match
                    if (content.contains(terrainLower)) {
                        return true;
                    }
                    // Enhanced keyword matching
                    List<String> keywords = TERRAIN_KEYWORDS.get(terrainLower);
                    return keywords != null && keywords.stream().anyMatch(content::contains);
                })
                .collect(Collectors.toList());
        
        if (matches.isEmpty() && !preferredTerrains.isEmpty()) {
            log.debug("No terrain matches found for {} with preferences: {}", 
                     attraction.getName(), preferredTerrains);
        }
        
        return matches;
    }

    /**
     * Calculate attraction score based on preference matches.
     */
    private int calculateAttractionScore(SuggestionResponse attraction) {
        int score = 0;
        if (attraction.getMatchedActivities() != null) {
            score += attraction.getMatchedActivities().size() * 3;
        }
        if (attraction.getMatchedTerrains() != null) {
            score += attraction.getMatchedTerrains().size() * 2;
        }
        if (attraction.getIsRecommended() != null && attraction.getIsRecommended()) {
            score += 1;
        }
        return score;
    }

    /**
     * Check if price level matches budget level.
     */
    private boolean matchesBudgetLevel(String priceLevel, String budgetLevel) {
        if (priceLevel == null || budgetLevel == null) return true;
        
        return switch (budgetLevel) {
            case "Low" -> "Low".equals(priceLevel);
            case "High" -> "High".equals(priceLevel);
            default -> true; // Medium accepts all
        };
    }

    /**
     * Generates mock suggestions for development and fallback purposes.
     * This is used when API keys are not configured or when external APIs fail.
     */
    private List<SuggestionResponse> generateMockSuggestions(String type, String city,
                                                           List<String> preferredActivities,
                                                           List<String> preferredTerrains,
                                                           String budgetLevel) {
        return switch (type) {
            case "attractions" -> generateMockAttractions(city, preferredActivities, preferredTerrains);
            case "hotels" -> generateMockHotels(city, budgetLevel);
            case "restaurants" -> generateMockRestaurants(city, budgetLevel);
            default -> List.of();
        };
    }

    private List<SuggestionResponse> generateMockAttractions(String city, List<String> activities, List<String> terrains) {
        return IntStream.range(1, 6).mapToObj(i -> {
            SuggestionResponse attraction = new SuggestionResponse();
            attraction.setId("attr_" + city.toLowerCase() + "_" + i);
            attraction.setName(generateAttractionName(city, i));
            attraction.setLocation(city);
            attraction.setAddress(generateAddress(city));
            attraction.setCategory("Cultural");
            attraction.setDuration(generateDuration());
            attraction.setPrice(generateAttractionPrice());
            attraction.setRating(4.0 + ThreadLocalRandom.current().nextDouble(1.0));
            attraction.setReviews(ThreadLocalRandom.current().nextInt(100, 2000));
            attraction.setImage(generateImageUrl("attraction"));
            attraction.setDescription(generateAttractionDescription(city));
            attraction.setOpenHours("9:00 AM - 5:00 PM");
            attraction.setSource("TripAdvisor (Mock)");
            
            // Enhanced preference matching for mock data
            attraction.setMatchedActivities(generateMatchedActivities(activities));
            attraction.setMatchedTerrains(generateMatchedTerrains(terrains));
            
            attraction.setLatitude(generateLatitude(city));
            attraction.setLongitude(generateLongitude(city));
            attraction.setDistanceKm(ThreadLocalRandom.current().nextDouble(0.5, 15.0));
            attraction.setPopularityLevel("High");
            attraction.setIsRecommended(i <= 2);
            return attraction;
        }).collect(Collectors.toList());
    }

    /**
     * Generate matched activities based on user preferences for mock data.
     */
    private List<String> generateMatchedActivities(List<String> preferredActivities) {
        if (preferredActivities == null || preferredActivities.isEmpty()) {
            return List.of("Sightseeing");
        }
        
        // Return 1-2 random activities from user preferences
        int numMatches = Math.min(preferredActivities.size(), ThreadLocalRandom.current().nextInt(1, 3));
        return preferredActivities.stream()
                .limit(numMatches)
                .collect(Collectors.toList());
    }

    /**
     * Generate matched terrains based on user preferences for mock data.
     */
    private List<String> generateMatchedTerrains(List<String> preferredTerrains) {
        if (preferredTerrains == null || preferredTerrains.isEmpty()) {
            return List.of("Cultural");
        }
        
        // Return 1 random terrain from user preferences
        int randomIndex = ThreadLocalRandom.current().nextInt(preferredTerrains.size());
        return List.of(preferredTerrains.get(randomIndex));
    }

    private List<SuggestionResponse> generateMockHotels(String city, String budgetLevel) {
        return IntStream.range(1, 6).mapToObj(i -> {
            SuggestionResponse hotel = new SuggestionResponse();
            hotel.setId("hotel_" + city.toLowerCase() + "_" + i);
            hotel.setName(generateHotelName(city, i));
            hotel.setLocation(city);
            hotel.setAddress(generateAddress(city));
            hotel.setCategory("Hotel");
            hotel.setPrice(generateHotelPrice(budgetLevel));
            hotel.setPriceLevel(budgetLevel);
            hotel.setRating(3.5 + ThreadLocalRandom.current().nextDouble(1.5));
            hotel.setReviews(ThreadLocalRandom.current().nextInt(50, 1000));
            hotel.setImage(generateImageUrl("hotel"));
            hotel.setDescription(generateHotelDescription(city));
            hotel.setSource("Google Places (Mock)");
            hotel.setLatitude(generateLatitude(city));
            hotel.setLongitude(generateLongitude(city));
            hotel.setDistanceKm(ThreadLocalRandom.current().nextDouble(0.1, 10.0));
            hotel.setPopularityLevel(i <= 2 ? "High" : "Medium");
            hotel.setIsRecommended(i <= 2);
            hotel.setBookingUrl("https://booking.com/hotel/" + hotel.getId());
            return hotel;
        }).collect(Collectors.toList());
    }

    private List<SuggestionResponse> generateMockRestaurants(String city, String budgetLevel) {
        return IntStream.range(1, 6).mapToObj(i -> {
            SuggestionResponse restaurant = new SuggestionResponse();
            restaurant.setId("rest_" + city.toLowerCase() + "_" + i);
            restaurant.setName(generateRestaurantName(city, i));
            restaurant.setLocation(city);
            restaurant.setAddress(generateAddress(city));
            restaurant.setCategory("Restaurant");
            restaurant.setCuisine(generateCuisine(i));
            restaurant.setPriceRange(generateRestaurantPriceRange(budgetLevel));
            restaurant.setPriceLevel(budgetLevel);
            restaurant.setRating(3.8 + ThreadLocalRandom.current().nextDouble(1.2));
            restaurant.setReviews(ThreadLocalRandom.current().nextInt(30, 800));
            restaurant.setImage(generateImageUrl("restaurant"));
            restaurant.setDescription(generateRestaurantDescription(city));
            restaurant.setOpenHours("11:00 AM - 10:00 PM");
            restaurant.setSource("Google Places (Mock)");
            restaurant.setLatitude(generateLatitude(city));
            restaurant.setLongitude(generateLongitude(city));
            restaurant.setDistanceKm(ThreadLocalRandom.current().nextDouble(0.2, 8.0));
            restaurant.setPopularityLevel(i <= 2 ? "High" : "Medium");
            restaurant.setIsRecommended(i <= 2);
            return restaurant;
        }).collect(Collectors.toList());
    }

    // Helper methods for generating mock data
    private String generateAttractionName(String city, int index) {
        String[] names = {"Temple", "Museum", "Fort", "Garden", "Palace"};
        return city + " " + names[index % names.length] + " " + index;
    }

    private String generateHotelName(String city, int index) {
        String[] types = {"Grand Hotel", "Resort", "Boutique Hotel", "Heritage Hotel", "Lodge"};
        return city + " " + types[index % types.length];
    }

    private String generateRestaurantName(String city, int index) {
        String[] names = {"Spice Garden", "Royal Palace", "Ocean View", "Heritage", "Garden Terrace"};
        return names[index % names.length] + " Restaurant";
    }

    private String generateAddress(String city) {
        return String.format("%d Main Street, %s, Sri Lanka", 
               ThreadLocalRandom.current().nextInt(1, 200), city);
    }

    private String generateDuration() {
        String[] durations = {"1-2 hours", "2-3 hours", "3-4 hours", "4-5 hours", "Half day"};
        return durations[ThreadLocalRandom.current().nextInt(durations.length)];
    }

    private String generateAttractionPrice() {
        int[] prices = {10, 15, 20, 25, 30};
        return "$" + prices[ThreadLocalRandom.current().nextInt(prices.length)];
    }

    private String generateHotelPrice(String budgetLevel) {
        return switch (budgetLevel) {
            case "Low" -> "$" + (50 + ThreadLocalRandom.current().nextInt(50)) + "/night";
            case "High" -> "$" + (200 + ThreadLocalRandom.current().nextInt(200)) + "/night";
            default -> "$" + (100 + ThreadLocalRandom.current().nextInt(100)) + "/night";
        };
    }

    private String generateRestaurantPriceRange(String budgetLevel) {
        return switch (budgetLevel) {
            case "Low" -> "$10-20";
            case "High" -> "$40-80";
            default -> "$20-40";
        };
    }

    private String generateCuisine(int index) {
        String[] cuisines = {"Sri Lankan", "Asian Fusion", "International", "Seafood", "Continental"};
        return cuisines[index % cuisines.length];
    }

    private String generateImageUrl(String type) {
        return String.format("https://images.unsplash.com/photo-%d?w=400&h=300&fit=crop", 
               1500000000L + ThreadLocalRandom.current().nextInt(1000000));
    }

    private String generateAttractionDescription(String city) {
        return String.format("A beautiful and historic attraction in %s, perfect for cultural exploration and photography.", city);
    }

    private String generateHotelDescription(String city) {
        return String.format("Comfortable accommodation in the heart of %s with modern amenities and excellent service.", city);
    }

    private String generateRestaurantDescription(String city) {
        return String.format("Authentic dining experience in %s featuring local and international cuisine.", city);
    }

    private Double generateLatitude(String city) {
        // Approximate coordinates for Sri Lankan cities
        return switch (city.toLowerCase()) {
            case "colombo" -> 6.9271 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "kandy" -> 7.2906 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "galle" -> 6.0535 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "sigiriya" -> 7.9568 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            default -> 7.0000 + ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        };
    }

    private Double generateLongitude(String city) {
        return switch (city.toLowerCase()) {
            case "colombo" -> 79.8612 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "kandy" -> 80.6337 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "galle" -> 80.2210 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            case "sigiriya" -> 80.7592 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
            default -> 80.0000 + ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        };
    }

    private List<String> filterMatches(List<String> userPreferences, List<String> availableOptions) {
        return userPreferences.stream()
                .filter(availableOptions::contains)
                .collect(Collectors.toList());
    }
}
