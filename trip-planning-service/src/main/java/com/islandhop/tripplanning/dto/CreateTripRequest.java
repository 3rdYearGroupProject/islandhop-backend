package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {
    
    private String tripName;
    
    @NotNull
    @Future
    private LocalDate startDate;
    
    @NotNull
    @Future
    private LocalDate endDate;
    
    private LocalTime arrivalTime;
    
    @NotEmpty
    private String baseCity;
    
    private boolean multiCity = false;
    
    @NotEmpty
    private List<String> categories; // Nature, Culture, Adventure, Leisure
    
    @NotNull
    private Trip.ActivityPacing pacing;
}
