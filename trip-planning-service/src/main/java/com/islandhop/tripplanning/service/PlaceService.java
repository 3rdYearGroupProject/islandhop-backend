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
    
    /**
     * Create a planned place from add place request
     */
    public PlannedPlace createPlannedPlace(AddPlaceRequest request) {
        log.info("Creating planned place for: {}", request.getPlaceName());
        
        PlannedPlace place = new PlannedPlace();
        place.setPlaceId(UUID.randomUUID().toString());
        place.setName(request.getPlaceName());
        place.setCity(request.getCity());
        place.setDescription(request.getDescription());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setType(PlannedPlace.PlaceType.ATTRACTION); // Default type
        place.setCategories(new ArrayList<>());
        place.setUserAdded(true);
        place.setConfirmed(false);
        
        // Set default visit duration based on place type
        place.setEstimatedVisitDurationMinutes(120); // 2 hours default
        
        if (request.getPreferredDay() != null) {
            place.setDayNumber(request.getPreferredDay());
        }
        
        log.info("Created planned place with ID: {}", place.getPlaceId());
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
}
