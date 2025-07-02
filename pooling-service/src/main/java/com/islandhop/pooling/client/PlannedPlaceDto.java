package com.islandhop.pooling.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedPlaceDto {
    private String placeId;
    private String name;
    private String city;
    private double latitude;
    private double longitude;
    private String description;
    private List<String> categories;
    private String type;
    private Integer estimatedVisitDurationMinutes;
    private LocalTime suggestedArrivalTime;
    private LocalTime suggestedDepartureTime;
    private Integer dayNumber;
    private Integer orderInDay;
    private String address;
    private Double rating;
    private String priceLevel;
    private boolean userAdded;
    private boolean confirmed;
}
