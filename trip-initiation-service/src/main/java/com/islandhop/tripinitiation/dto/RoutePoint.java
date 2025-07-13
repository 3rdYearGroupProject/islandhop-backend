package com.islandhop.tripinitiation.dto;

import lombok.Data;

@Data
public class RoutePoint {
    private String name;
    private Location location;

    @Data
    public static class Location {
        private double lat;
        private double lng;
    }
}