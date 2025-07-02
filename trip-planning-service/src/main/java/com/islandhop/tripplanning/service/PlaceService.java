package com.islandhop.tripplanning.service;

import com.islandhop.tripplanning.dto.AddPlaceRequest;
import com.islandhop.tripplanning.model.PlannedPlace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {
    
    private final LocationService locationService;
    
    /**
     * Create a planned place from add place request with enhanced validation
     */
    public PlannedPlace createPlannedPlace(AddPlaceRequest request) {
        log.info("Creating planned place for: {}", request.getPlaceName());
        
        // Validate and enrich the place data
        LocationService.PlaceValidationResult validation = locationService.validateAndEnrichPlace(request);
        
        PlannedPlace place = new PlannedPlace();
        place.setPlaceId(UUID.randomUUID().toString());
        place.setName(request.getPlaceName());
        place.setCity(request.getCity());
        place.setDescription(request.getDescription());
        
        // Use validated/suggested coordinates if available
        if (validation.isValid()) {
            if (validation.getSuggestedLatitude() != null && validation.getSuggestedLongitude() != null) {
                place.setLatitude(validation.getSuggestedLatitude());
                place.setLongitude(validation.getSuggestedLongitude());
            } else if (request.getLatitude() != null && request.getLongitude() != null) {
                place.setLatitude(request.getLatitude());
                place.setLongitude(request.getLongitude());
            }
            
            // Use formatted address if available
            if (validation.getFormattedAddress() != null) {
                place.setCity(extractCityFromFormattedAddress(validation.getFormattedAddress()));
            }
        } else {
            // Fallback to original coordinates if validation failed
            place.setLatitude(request.getLatitude());
            place.setLongitude(request.getLongitude());
            log.warn("Using unvalidated coordinates for place: {}", request.getPlaceName());
        }
        
        // Infer place type from location data if available
        if (validation.getSuggestions() != null && !validation.getSuggestions().isEmpty()) {
            LocationService.LocationSearchResult bestMatch = validation.getSuggestions().get(0);
            place.setType(inferPlaceTypeFromGoogleTypes(bestMatch.getTypes()));
        } else {
            place.setType(PlannedPlace.PlaceType.ATTRACTION); // Default type
        }
        
        place.setCategories(inferCategories(place.getName(), place.getDescription()));
        place.setUserAdded(true);
        place.setConfirmed(validation.isValid());
        
        // Set default visit duration based on place type
        place.setEstimatedVisitDurationMinutes(getDefaultVisitDuration(place.getType()));
        
        if (request.getPreferredDay() != null) {
            place.setDayNumber(request.getPreferredDay());
        }
        
        log.info("Created planned place with ID: {} (validated: {})", place.getPlaceId(), validation.isValid());
        return place;
    }
    
    /**
     * Enrich place data with additional information
     */
    public PlannedPlace enrichPlaceData(PlannedPlace place) {
        // This would typically call external APIs to get more details
        // For now, we'll add some default enrichment
        
        if (place.getEstimatedVisitDurationMinutes() == null) {
            place.setEstimatedVisitDurationMinutes(getDefaultVisitDuration(place.getType()));
        }
        
        if (place.getCategories() == null || place.getCategories().isEmpty()) {
            place.setCategories(inferCategories(place.getName(), place.getDescription()));
        }
        
        return place;
    }
    
    /**
     * Get default visit duration based on place type
     */
    private Integer getDefaultVisitDuration(PlannedPlace.PlaceType type) {
        switch (type) {
            case ATTRACTION:
                return 120; // 2 hours
            case HOTEL:
                return 60;  // 1 hour (check-in/out)
            case RESTAURANT:
                return 90;  // 1.5 hours
            case SHOPPING:
                return 150; // 2.5 hours
            case VIEWPOINT:
                return 45;  // 45 minutes
            case TRANSPORT_HUB:
                return 30;  // 30 minutes
            default:
                return 90;  // 1.5 hours
        }
    }
    
    /**
     * Infer categories from place name and description
     */
    private java.util.List<String> inferCategories(String name, String description) {
        java.util.List<String> categories = new ArrayList<>();
        
        String text = (name + " " + (description != null ? description : "")).toLowerCase();
        
        // Nature keywords
        if (text.contains("beach") || text.contains("forest") || text.contains("park") || 
            text.contains("garden") || text.contains("lake") || text.contains("waterfall")) {
            categories.add("Nature");
        }
        
        // Culture keywords
        if (text.contains("temple") || text.contains("museum") || text.contains("historic") || 
            text.contains("palace") || text.contains("monument") || text.contains("archaeological")) {
            categories.add("Culture");
        }
        
        // Adventure keywords
        if (text.contains("hiking") || text.contains("safari") || text.contains("surfing") || 
            text.contains("diving") || text.contains("climbing") || text.contains("adventure")) {
            categories.add("Adventure");
        }
        
        // Leisure keywords
        if (text.contains("shopping") || text.contains("spa") || text.contains("restaurant") || 
            text.contains("cafe") || text.contains("market") || text.contains("mall")) {
            categories.add("Leisure");
        }
        
        // Default category if none detected
        if (categories.isEmpty()) {
            categories.add("General");
        }
        
        return categories;
    }
    
    /**
     * Extract city name from Google's formatted address
     */
    private String extractCityFromFormattedAddress(String formattedAddress) {
        if (formattedAddress == null) return null;
        
        // Split address by commas and try to find the city
        String[] parts = formattedAddress.split(",");
        
        // For Sri Lankan addresses, city is usually the second-to-last part before "Sri Lanka"
        for (int i = parts.length - 2; i >= 0; i--) {
            String part = parts[i].trim();
            if (!part.equalsIgnoreCase("Sri Lanka") && !part.matches("\\d+")) {
                return part;
            }
        }
        
        return null;
    }
    
    /**
     * Infer place type from Google Places API types
     */
    private PlannedPlace.PlaceType inferPlaceTypeFromGoogleTypes(java.util.List<String> googleTypes) {
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
    
    /**
     * Find nearby places based on coordinates
     */
    public java.util.List<PlannedPlace> findNearbyPlaces(Double latitude, Double longitude, 
                                                     double radiusKm, int maxResults) {
        log.info("Finding places near ({}, {}) within {}km", latitude, longitude, radiusKm);
        
        // This would normally call a real API or database
        // For now, we'll generate some mock places
        java.util.List<PlannedPlace> nearbyPlaces = new ArrayList<>();
        
        // Generate some mock places at random distances within the radius
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < maxResults; i++) {
            PlannedPlace place = new PlannedPlace();
            place.setPlaceId(UUID.randomUUID().toString());
            place.setName("Nearby Place " + (i + 1));
            
            // Generate a random point within the radius
            double distance = random.nextDouble() * radiusKm;
            double bearing = random.nextDouble() * 360; // Random direction
            
            // Calculate new coordinates
            double[] newCoords = calculateCoordinatesAtDistance(latitude, longitude, distance, bearing);
            place.setLatitude(newCoords[0]);
            place.setLongitude(newCoords[1]);
            
            // Set other details
            place.setDescription("A nearby point of interest");
            place.setType(PlannedPlace.PlaceType.ATTRACTION);
            place.setCategories(java.util.List.of("Point of Interest", "Landmark"));
            place.setAddress(distance + "km from reference point");
            place.setEstimatedVisitDurationMinutes(60); // 1 hour
            
            nearbyPlaces.add(place);
        }
        
        return nearbyPlaces;
    }
    
    /**
     * Calculate new coordinates at a given distance and bearing from a point
     */
    private double[] calculateCoordinatesAtDistance(double lat, double lng, double distanceKm, double bearing) {
        final double R = 6371; // Earth radius in km
        final double bearingRad = Math.toRadians(bearing);
        final double latRad = Math.toRadians(lat);
        final double lngRad = Math.toRadians(lng);
        
        double distance = distanceKm / R; // Angular distance
        
        double newLatRad = Math.asin(Math.sin(latRad) * Math.cos(distance) +
                          Math.cos(latRad) * Math.sin(distance) * Math.cos(bearingRad));
                          
        double newLngRad = lngRad + Math.atan2(Math.sin(bearingRad) * Math.sin(distance) * Math.cos(latRad),
                                             Math.cos(distance) - Math.sin(latRad) * Math.sin(newLatRad));
        
        // Convert back to degrees
        double newLat = Math.toDegrees(newLatRad);
        double newLng = Math.toDegrees(newLngRad);
        
        return new double[] { newLat, newLng };
    }
}
