package com.islandhop.tripinit.model.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

/**
 * Initiated trip document stored in MongoDB initiated_trips collection.
 * Extended trip plan with additional cost and route information.
 */
@Data
@Document(collection = "initiated_trips")
public class InitiatedTrip {
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
    
    // Additional fields for initiated trips
    private Integer driverNeeded;
    private Integer guideNeeded;
    private Double averageTripDistance;
    private Double averageDriverCost;
    private Double averageGuideCost;
    private String vehicleType;
}