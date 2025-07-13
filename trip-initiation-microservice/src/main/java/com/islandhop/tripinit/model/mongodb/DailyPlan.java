package com.islandhop.tripinit.model.mongodb;

import lombok.Data;
import java.util.List;

/**
 * Daily plan model containing city and places for a specific day.
 * Part of the trip plan structure.
 */
@Data
public class DailyPlan {
    private Integer day;
    private String city;
    private Boolean userSelected;
    private List<Place> attractions;
    private List<Place> restaurants;
    private List<Place> hotels;
    private List<String> notes;
}