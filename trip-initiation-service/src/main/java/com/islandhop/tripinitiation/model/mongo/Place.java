package com.islandhop.tripinitiation.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Place {
    private String name;
    private String type;
    private String[] terrainTags;
    private String[] activityTags;
    private Location location;
    private double distanceFromCenterKm;
    private int visitDurationMinutes;
    private String recommendedStartTime;
    private String openHours;
    private String popularityLevel;
    private double rating;
    private String thumbnailUrl;
    private String source;
    private String placeId;
    private String googlePlaceId;
    private boolean userSelected;
    private String[] warnings;
}