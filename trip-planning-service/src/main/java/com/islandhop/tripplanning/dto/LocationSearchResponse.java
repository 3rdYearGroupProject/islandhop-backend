package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for location search operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchResponse {
    
    private String query;
    private String city;
    private Integer totalResults;
    private List<LocationService.LocationSearchResult> results;
    private SearchMetadata metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadata {
        private Long searchTimeMs;
        private Double biasLatitude;
        private Double biasLongitude;
        private String searchSource; // "google", "tripadvisor", "hybrid"
        private Boolean sriLankaFiltered;
    }
}
