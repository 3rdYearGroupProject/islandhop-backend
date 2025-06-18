package com.islandhop.tourplanning.service.impl;

import com.islandhop.tourplanning.client.GoogleMapsClient;
import com.islandhop.tourplanning.client.TripAdvisorClient;
import com.islandhop.tourplanning.dto.PlaceDTO;
import com.islandhop.tourplanning.dto.UserPreferencesDTO;
import com.islandhop.tourplanning.model.Place;
import com.islandhop.tourplanning.repository.PlaceRepository;
import com.islandhop.tourplanning.repository.UserPreferencesRepository;
import com.islandhop.tourplanning.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private TripAdvisorClient tripAdvisorClient;

    @Autowired
    private GoogleMapsClient googleMapsClient;

    @Override
    public List<PlaceDTO> getRecommendedPlaces(String userId, double latitude, double longitude, int radius) {
        // Get user preferences
        UserPreferencesDTO preferences = userPreferencesRepository.findByUserId(userId)
                .map(this::convertToDTO)
                .orElse(null);

        // Get nearby places from Google Maps
        List<PlaceDTO> nearbyPlaces = googleMapsClient.searchNearbyPlaces(latitude, longitude, radius, null);

        // Get popular places from TripAdvisor
        List<PlaceDTO> popularPlaces = tripAdvisorClient.getPopularPlaces(latitude + "," + longitude);

        // Combine and filter results based on user preferences
        return combineAndFilterResults(nearbyPlaces, popularPlaces, preferences);
    }

    @Override
    public List<PlaceDTO> getPopularPlaces(String location, String type) {
        return tripAdvisorClient.getPopularPlaces(location);
    }

    @Override
    public List<PlaceDTO> getPlacesByPreferences(UserPreferencesDTO preferences, String location) {
        // Get places from both APIs
        List<PlaceDTO> tripAdvisorPlaces = tripAdvisorClient.searchPlaces(location, null);
        List<PlaceDTO> googlePlaces = googleMapsClient.searchNearbyPlaces(0, 0, 5000, null);

        // Combine and filter results based on preferences
        return combineAndFilterResults(tripAdvisorPlaces, googlePlaces, preferences);
    }

    @Override
    public List<PlaceDTO> getNearbyPlaces(double latitude, double longitude, int radius, String type) {
        return googleMapsClient.searchNearbyPlaces(latitude, longitude, radius, type);
    }

    @Override
    public List<PlaceDTO> getPlacesByBudget(double minBudget, double maxBudget, String location) {
        // Get places from TripAdvisor
        List<PlaceDTO> places = tripAdvisorClient.searchPlaces(location, null);

        // Filter by budget
        return places.stream()
                .filter(place -> place.getPrice() >= minBudget && place.getPrice() <= maxBudget)
                .collect(Collectors.toList());
    }

    private List<PlaceDTO> combineAndFilterResults(List<PlaceDTO> places1, List<PlaceDTO> places2, UserPreferencesDTO preferences) {
        // Combine results from both sources
        Set<PlaceDTO> combinedPlaces = new HashSet<>();
        combinedPlaces.addAll(places1);
        combinedPlaces.addAll(places2);

        // Filter based on preferences
        return combinedPlaces.stream()
                .filter(place -> matchesPreferences(place, preferences))
                .sorted(Comparator.comparingDouble(PlaceDTO::getRating).reversed())
                .collect(Collectors.toList());
    }

    private boolean matchesPreferences(PlaceDTO place, UserPreferencesDTO preferences) {
        if (preferences == null) {
            return true;
        }

        // Check place type
        if (preferences.getPreferredPlaceTypes() != null && !preferences.getPreferredPlaceTypes().isEmpty()) {
            if (!preferences.getPreferredPlaceTypes().contains(place.getType().toString())) {
                return false;
            }
        }

        // Check budget
        if (place.getPrice() > preferences.getMaxBudget()) {
            return false;
        }

        // Check popularity preference
        if (preferences.isPreferPopularPlaces() && place.getRating() < 4.0) {
            return false;
        }

        return true;
    }

    private PlaceDTO convertToDTO(Place place) {
        PlaceDTO dto = new PlaceDTO();
        dto.setId(place.getId());
        dto.setName(place.getName());
        dto.setDescription(place.getDescription());
        dto.setAddress(place.getAddress());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        dto.setType(place.getType());
        dto.setRating(place.getRating());
        dto.setReviewCount(place.getReviewCount());
        dto.setTripAdvisorId(place.getTripAdvisorId());
        dto.setGooglePlaceId(place.getGooglePlaceId());
        dto.setVisitTime(place.getVisitTime());
        dto.setEstimatedDuration(place.getEstimatedDuration());
        dto.setPrice(place.getPrice());
        dto.setCurrency(place.getCurrency());
        dto.setBookmarked(place.isBookmarked());
        return dto;
    }

    private UserPreferencesDTO convertToDTO(UserPreferences preferences) {
        UserPreferencesDTO dto = new UserPreferencesDTO();
        dto.setId(preferences.getId());
        dto.setUserId(preferences.getUserId());
        dto.setPreferredPlaceTypes(preferences.getPreferredPlaceTypes());
        dto.setMaxBudget(preferences.getMaxBudget());
        dto.setPreferredCurrency(preferences.getPreferredCurrency());
        dto.setPreferredTripDuration(preferences.getPreferredTripDuration());
        dto.setPreferPopularPlaces(preferences.isPreferPopularPlaces());
        dto.setPreferLessCrowded(preferences.isPreferLessCrowded());
        dto.setDietaryRestrictions(preferences.getDietaryRestrictions());
        dto.setAccessibilityNeeds(preferences.getAccessibilityNeeds());
        dto.setPreferredLanguage(preferences.getPreferredLanguage());
        dto.setPreferredTransportationMode(preferences.getPreferredTransportationMode());
        return dto;
    }
} 