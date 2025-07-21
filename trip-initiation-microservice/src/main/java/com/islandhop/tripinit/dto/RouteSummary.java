package com.islandhop.tripinit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Route summary DTO containing daily plan information.
 * Provides structured route data for frontend display.
 */
@Data
@Builder
public class RouteSummary {
    private Integer day;
    private String city;
    private List<AttractionSummary> attractions;

    /**
     * Attraction summary containing name and location.
     */
    @Data
    @Builder
    public static class AttractionSummary {
        private String name;
        private LocationSummary location;
    }

    /**
     * Location summary with latitude and longitude.
     */
    @Data
    @Builder
    public static class LocationSummary {
        private Double lat;
        private Double lng;
    }
}