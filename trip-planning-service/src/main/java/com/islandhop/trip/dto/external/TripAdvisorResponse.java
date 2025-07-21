package com.islandhop.trip.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for TripAdvisor Content API responses.
 * Used for attractions, hotels, and restaurants data.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripAdvisorResponse {

    @JsonProperty("data")
    private List<LocationData> data;

    @JsonProperty("paging")
    private Paging paging;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationData {
        
        @JsonProperty("location_id")
        private String locationId;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("web_url")
        private String webUrl;
        
        @JsonProperty("address_obj")
        private AddressObj addressObj;
        
        @JsonProperty("ancestors")
        private List<Ancestor> ancestors;
        
        @JsonProperty("latitude")
        private String latitude;
        
        @JsonProperty("longitude")
        private String longitude;
        
        @JsonProperty("timezone")
        private String timezone;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("website")
        private String website;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("rating")
        private String rating;
        
        @JsonProperty("rating_image_url")
        private String ratingImageUrl;
        
        @JsonProperty("num_reviews")
        private String numReviews;
        
        @JsonProperty("photo_count")
        private String photoCount;
        
        @JsonProperty("see_all_photos")
        private String seeAllPhotos;
        
        @JsonProperty("category")
        private Category category;
        
        @JsonProperty("subcategory")
        private List<Category> subcategory;
        
        @JsonProperty("hours")
        private Hours hours;
        
        @JsonProperty("cuisine")
        private List<Category> cuisine;
        
        @JsonProperty("price_level")
        private String priceLevel;
        
        @JsonProperty("amenities")
        private List<String> amenities;
        
        @JsonProperty("groups")
        private List<Group> groups;
        
        @JsonProperty("neighborhood_info")
        private List<NeighborhoodInfo> neighborhoodInfo;
        
        @JsonProperty("trip_types")
        private List<TripType> tripTypes;
        
        @JsonProperty("awards")
        private List<Award> awards;
        
        @JsonProperty("photo")
        private Photo photo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressObj {
        
        @JsonProperty("street1")
        private String street1;
        
        @JsonProperty("street2")
        private String street2;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("state")
        private String state;
        
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("postalcode")
        private String postalcode;
        
        @JsonProperty("address_string")
        private String addressString;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ancestor {
        
        @JsonProperty("level")
        private String level;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("location_id")
        private String locationId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("localized_name")
        private String localizedName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hours {
        
        @JsonProperty("periods")
        private List<Period> periods;
        
        @JsonProperty("weekday_text")
        private List<String> weekdayText;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Period {
        
        @JsonProperty("open")
        private TimeInfo open;
        
        @JsonProperty("close")
        private TimeInfo close;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeInfo {
        
        @JsonProperty("day")
        private Integer day;
        
        @JsonProperty("time")
        private String time;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Group {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("localized_name")
        private String localizedName;
        
        @JsonProperty("categories")
        private List<Category> categories;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NeighborhoodInfo {
        
        @JsonProperty("location_id")
        private String locationId;
        
        @JsonProperty("name")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripType {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("localized_name")
        private String localizedName;
        
        @JsonProperty("value")
        private String value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Award {
        
        @JsonProperty("award_type")
        private String awardType;
        
        @JsonProperty("year")
        private String year;
        
        @JsonProperty("images")
        private Images images;
        
        @JsonProperty("categories")
        private List<String> categories;
        
        @JsonProperty("display_name")
        private String displayName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Images {
        
        @JsonProperty("tiny")
        private String tiny;
        
        @JsonProperty("small")
        private String small;
        
        @JsonProperty("large")
        private String large;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photo {
        
        @JsonProperty("images")
        private List<PhotoImage> images;
        
        @JsonProperty("caption")
        private String caption;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PhotoImage {
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("width")
        private Integer width;
        
        @JsonProperty("height")
        private Integer height;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {
        
        @JsonProperty("results")
        private String results;
        
        @JsonProperty("total_results")
        private String totalResults;
        
        @JsonProperty("limit")
        private String limit;
        
        @JsonProperty("offset")
        private String offset;
    }
}
