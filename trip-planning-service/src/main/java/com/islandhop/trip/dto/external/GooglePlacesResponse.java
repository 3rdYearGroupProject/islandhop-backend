package com.islandhop.trip.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for Google Places API responses.
 * Used for location search and place details.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePlacesResponse {

    @JsonProperty("results")
    private List<PlaceResult> results;

    @JsonProperty("status")
    private String status;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceResult {
        
        @JsonProperty("place_id")
        private String placeId;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        @JsonProperty("geometry")
        private Geometry geometry;
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("user_ratings_total")
        private Integer userRatingsTotal;
        
        @JsonProperty("price_level")
        private Integer priceLevel;
        
        @JsonProperty("types")
        private List<String> types;
        
        @JsonProperty("photos")
        private List<Photo> photos;
        
        @JsonProperty("opening_hours")
        private OpeningHours openingHours;
        
        @JsonProperty("vicinity")
        private String vicinity;
        
        @JsonProperty("business_status")
        private String businessStatus;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        
        @JsonProperty("location")
        private Location location;
        
        @JsonProperty("viewport")
        private Viewport viewport;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        
        @JsonProperty("lat")
        private Double lat;
        
        @JsonProperty("lng")
        private Double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Viewport {
        
        @JsonProperty("northeast")
        private Location northeast;
        
        @JsonProperty("southwest")
        private Location southwest;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photo {
        
        @JsonProperty("photo_reference")
        private String photoReference;
        
        @JsonProperty("height")
        private Integer height;
        
        @JsonProperty("width")
        private Integer width;
        
        @JsonProperty("html_attributions")
        private List<String> htmlAttributions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpeningHours {
        
        @JsonProperty("open_now")
        private Boolean openNow;
        
        @JsonProperty("weekday_text")
        private List<String> weekdayText;
    }
}
