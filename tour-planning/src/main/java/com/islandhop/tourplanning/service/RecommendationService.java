package com.islandhop.tourplanning.service;

import com.islandhop.tourplanning.dto.PlaceDTO;
import com.islandhop.tourplanning.dto.UserPreferencesDTO;
import java.util.List;

public interface RecommendationService {
    List<PlaceDTO> getRecommendedPlaces(String userId, double latitude, double longitude, int radius);
    List<PlaceDTO> getPopularPlaces(String location, String type);
    List<PlaceDTO> getPlacesByPreferences(UserPreferencesDTO preferences, String location);
    List<PlaceDTO> getNearbyPlaces(double latitude, double longitude, int radius, String type);
    List<PlaceDTO> getPlacesByBudget(double minBudget, double maxBudget, String location);
} 