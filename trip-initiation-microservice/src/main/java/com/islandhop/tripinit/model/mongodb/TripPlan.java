package com.islandhop.tripinit.model.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

/**
 * Trip plan document stored in MongoDB trip_plans collection.
 * Contains comprehensive trip information and daily plans.
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
    private List<Location> mapData;
    private Instant createdAt;
    private Instant lastUpdated;
}