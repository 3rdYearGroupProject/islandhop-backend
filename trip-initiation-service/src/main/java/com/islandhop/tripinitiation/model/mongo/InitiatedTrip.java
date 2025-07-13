package com.islandhop.tripinitiation.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "initiated_trips")
public class InitiatedTrip {
    @Id
    private String id; // UUID for the initiated trip
    private String userId; // User ID associated with the trip
    private String tripId; // ID of the original trip plan
    private boolean driverNeeded; // Indicates if a driver is needed
    private boolean guideNeeded; // Indicates if a guide is needed
    private double averageTripDistance; // Total distance for the trip
    private double averageDriverCost; // Calculated driver cost
    private double averageGuideCost; // Calculated guide cost
    private String vehicleType; // Preferred vehicle type ID
    private Instant createdAt; // Timestamp for when the trip was initiated
    private Instant lastUpdated; // Timestamp for the last update to the trip
}