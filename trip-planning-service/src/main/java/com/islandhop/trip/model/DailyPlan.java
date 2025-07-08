package com.islandhop.trip.model;

import lombok.Data;

import java.util.List;

/**
 * Represents a single day's plan within a trip itinerary.
 * Contains the day number, city information, and lists of places for different categories.
 */
@Data
public class DailyPlan {

    private Integer day;
    private String city = "";
    private Boolean userSelected = false;
    private List<Place> attractions = List.of();
    private List<Place> restaurants = List.of();
    private List<Place> hotels = List.of();
    private List<String> notes = List.of();
}
