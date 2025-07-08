package com.islandhop.trip.model;

import lombok.Data;

/**
 * Represents geographic location data for a place.
 * Contains latitude and longitude coordinates.
 */
@Data
public class Location {

    private Double lat;
    private Double lng;
}
