package com.islandhop.tripinitiation.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "trip_plans")
public class TripPlan {
    @Id
    private String id; // UUID as String
    private String userId;
    private String tripName;
    private String startDate; // YYYY-MM-DD
    private String endDate; // YYYY-MM-DD
    private String arrivalTime; // HH:mm
    private String baseCity;
    private boolean multiCityAllowed;
    private String activityPacing; // Relaxed, Normal, Fast
    private String budgetLevel; // Low, Medium, High
    private List<String> preferredTerrains; // e.g., ["Beach", "Mountain"]
    private List<String> preferredActivities; // e.g., ["Hiking", "Cultural Tours"]
    private List<DailyPlan> dailyPlans;
    private List<Location> mapData; // List of locations for mapping
    private Instant createdAt; // ISODate
    private Instant lastUpdated; // ISODate
}