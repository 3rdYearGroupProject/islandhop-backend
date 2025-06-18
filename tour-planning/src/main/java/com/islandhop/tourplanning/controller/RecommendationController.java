package com.islandhop.tourplanning.controller;

import com.islandhop.tourplanning.dto.PlaceDTO;
import com.islandhop.tourplanning.dto.UserPreferencesDTO;
import com.islandhop.tourplanning.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlaceDTO>> getRecommendedPlaces(
            @PathVariable String userId,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") int radius) {
        return ResponseEntity.ok(recommendationService.getRecommendedPlaces(userId, latitude, longitude, radius));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PlaceDTO>> getPopularPlaces(
            @RequestParam String location,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(recommendationService.getPopularPlaces(location, type));
    }

    @PostMapping("/preferences")
    public ResponseEntity<List<PlaceDTO>> getPlacesByPreferences(
            @RequestBody UserPreferencesDTO preferences,
            @RequestParam String location) {
        return ResponseEntity.ok(recommendationService.getPlacesByPreferences(preferences, location));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceDTO>> getNearbyPlaces(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") int radius,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(recommendationService.getNearbyPlaces(latitude, longitude, radius, type));
    }

    @GetMapping("/budget")
    public ResponseEntity<List<PlaceDTO>> getPlacesByBudget(
            @RequestParam double minBudget,
            @RequestParam double maxBudget,
            @RequestParam String location) {
        return ResponseEntity.ok(recommendationService.getPlacesByBudget(minBudget, maxBudget, location));
    }
} 