package com.islandhop.trip.model;

import lombok.Data;

import java.util.List;

/**
 * Represents a place (attraction, restaurant, hotel) within a trip itinerary.
 * Contains detailed information about the place including location, timing, and user selection status.
 */
@Data
public class Place {

    private String name;
    private String type;
    private List<String> terrainTags = List.of();
    private List<String> activityTags = List.of();
    private Location location;
    private Double distanceFromCenterKm;
    private Integer visitDurationMinutes;
    private String recommendedStartTime;
    private String openHours;
    private String popularityLevel;
    private Double rating;
    private String thumbnailUrl;
    private String source;
    private String placeId;
    private String googlePlaceId;
    private Boolean userSelected = false;
    private List<String> warnings = List.of();
}
