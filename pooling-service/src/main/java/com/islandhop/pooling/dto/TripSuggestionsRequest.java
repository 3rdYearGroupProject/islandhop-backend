package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for getting trip suggestions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSuggestionsRequest {
    
    @NotNull(message = "Travel dates are required")
    private Map<String, String> travelDates;
    
    private List<String> interests;
    
    private List<String> language;
    
    private String budgetLevel;
    
    private String ageRange;
    
    /**
     * Convenience methods for accessing travel dates.
     */
    public String getStartDate() {
        return travelDates != null ? travelDates.get("startDate") : null;
    }
    
    public String getEndDate() {
        return travelDates != null ? travelDates.get("endDate") : null;
    }
}
