package com.islandhop.tripinitiation.dto;

import lombok.Data;

@Data
public class TripInitiationResponse {
    private String tripId;
    private String userId;
    private double averageTripDistance;
    private double averageDriverCost;
    private double averageGuideCost;
    private String vehicleType;
    private RoutePoint[] routeSummary;

    @Data
    public static class RoutePoint {
        private int day;
        private String city;
        private Place[] attractions;

        @Data
        public static class Place {
            private String name;
            private Location location;

            @Data
            public static class Location {
                private double lat;
                private double lng;
            }
        }
    }
}