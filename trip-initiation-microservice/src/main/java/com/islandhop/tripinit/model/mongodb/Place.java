package com.islandhop.tripinit.model.mongodb;

import lombok.Data;
import java.util.List;

/**
 * Place model representing attractions, restaurants, and hotels.
 * Contains comprehensive place information from various sources.
 */
@Data
public class Place {
    private String name;
    private String type;
    private List<String> terrainTags;
    private List<String> activityTags;
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
    private Boolean userSelected;
    private List<String> warnings;
}