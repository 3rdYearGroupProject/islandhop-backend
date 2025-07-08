package com.islandhop.trip.model;

import lombok.Data;

/**
 * Represents map data for displaying locations on a map.
 * Contains a label and geographic coordinates.
 */
@Data
public class MapData {

    private String label;
    private Double lat;
    private Double lng;
}
