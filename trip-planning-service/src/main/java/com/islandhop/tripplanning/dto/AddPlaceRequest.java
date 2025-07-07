package com.islandhop.tripplanning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlaceRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty
    private String placeName;
    
    private String city;
    private String description;
    private Double latitude;
    private Double longitude;
    private Integer preferredDay; // Optional: which day user wants to visit
}
