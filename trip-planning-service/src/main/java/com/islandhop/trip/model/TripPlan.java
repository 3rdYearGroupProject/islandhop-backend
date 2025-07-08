package com.islandhop.trip.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB entity representing a complete trip plan.
 * Stored in the trip_plans collection and contains all trip details,
 * daily plans, and metadata.
 */
@Data
@Document(collection = "trip_plans")
public class TripPlan {

    @Id
    private String id;
    private String userId;
    private String tripName;
    private String startDate;
    private String endDate;
    private String arrivalTime;
    private String baseCity;
    private Boolean multiCityAllowed;
    private String activityPacing;
    private String budgetLevel;
    private List<String> preferredTerrains;
    private List<String> preferredActivities;
    private List<DailyPlan> dailyPlans;
    private List<MapData> mapData;
    private Instant createdAt;
    private Instant lastUpdated;
}
