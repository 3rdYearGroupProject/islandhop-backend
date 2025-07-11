package com.islandhop.pooling.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for place suggestions (attractions, hotels, restaurants).
 * Reused from trip-planning-service to maintain consistency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestionResponse implements Serializable {
    
    private String id;
    private String name;
    private String location;
    private String address;
    
    // Price-related fields
    private String price;           // For hotels and attractions (e.g., "$180/night", "$45")
    private String priceRange;      // For restaurants (e.g., "$30-50")
    private String priceLevel;      // Budget indicator (e.g., "Medium", "High")
    
    // Type-specific fields
    private String cuisine;         // For restaurants
    private String duration;        // For attractions (e.g., "3-4 hours")
    private String category;        // General category (e.g., "Cultural", "Adventure")
    
    // Rating and popularity
    private Double rating;
    private Integer reviews;
    private String popularityLevel; // e.g., "High", "Medium", "Low"
    
    // Media and description
    private String image;
    private String description;
    private List<String> images;    // Multiple images if available
    
    // Location data
    private Double latitude;
    private Double longitude;
    private Double distanceKm;      // Distance from city center or selected location
    
    // Operating information
    private String openHours;
    private Boolean isOpenNow;
    private String phone;
    private String website;
    
    // Source and identification
    private String source;          // e.g., "TripAdvisor", "Google Places"
    private String externalId;      // ID from external API
    private String googlePlaceId;
    
    // Matching information
    private List<String> matchedActivities;  // Which user preferences this matches
    private List<String> matchedTerrains;
    private String matchReason;              // Why this was suggested
    
    // Additional metadata
    private List<String> tags;      // Keywords or tags
    private String bookingUrl;      // Direct booking link if available
    private Boolean isRecommended;  // Highlighted recommendation
}
