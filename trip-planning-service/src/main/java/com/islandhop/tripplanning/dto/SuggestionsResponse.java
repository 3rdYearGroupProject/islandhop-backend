package com.islandhop.tripplanning.dto;

import com.islandhop.tripplanning.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionsResponse {
    
    private String tripId;
    private List<Recommendation> attractions;
    private List<Recommendation> hotels;
    private List<Recommendation> restaurants;
    private List<String> insights;
    private List<String> warnings;
    private String message;
}
