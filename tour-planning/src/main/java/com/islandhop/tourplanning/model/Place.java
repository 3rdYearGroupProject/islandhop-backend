package com.islandhop.tourplanning.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
@Document(collection = "places")
public class Place {
    @Id
    private String id;

    @Indexed
    private String googlePlaceId;

    @Indexed
    private String tripAdvisorId;

    private String name;
    private String description;
    private PlaceType type;
    private List<String> categories;
    private Double rating;
    private Integer totalReviews;
    private PriceLevel priceLevel;
    private String address;
    private String phoneNumber;
    private String website;

    @GeoSpatialIndexed
    private Location location;

    private OpeningHours openingHours;
    private List<String> photos;
    private Map<String, Object> additionalDetails;

    private LocalTime averageVisitDuration;
    private Integer popularityScore;
    private List<String> tags;
    private List<String> amenities;

    private LocalDateTime visitTime;
    private int estimatedDuration; // in minutes
    private double price;
    private String currency;
    private boolean isBookmarked;

    @Data
    public static class Location {
        private Double latitude;
        private Double longitude;
    }

    @Data
    public static class OpeningHours {
        private List<TimeSlot> monday;
        private List<TimeSlot> tuesday;
        private List<TimeSlot> wednesday;
        private List<TimeSlot> thursday;
        private List<TimeSlot> friday;
        private List<TimeSlot> saturday;
        private List<TimeSlot> sunday;
    }

    @Data
    public static class TimeSlot {
        private LocalTime open;
        private LocalTime close;
    }

    public enum PlaceType {
        ATTRACTION,
        RESTAURANT,
        HOTEL,
        SHOPPING,
        ENTERTAINMENT
    }

    public enum PriceLevel {
        FREE,
        INEXPENSIVE,
        MODERATE,
        EXPENSIVE,
        VERY_EXPENSIVE
    }
} 