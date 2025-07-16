package com.islandhop.tripinit.model.mongodb;

import lombok.Data;

/**
 * Location model for geographical coordinates.
 * Used for attractions, restaurants, and hotels.
 */
@Data
public class Location {
    private Double lat;
    private Double lng;
}