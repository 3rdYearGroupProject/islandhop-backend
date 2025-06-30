package com.islandhop.pooling.dto;

import com.islandhop.pooling.model.TripPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolSuggestion {
    
    private TripPool suggestedPool;
    private Double compatibilityScore;
    private String matchReason;
    private List<String> commonInterests;
    private List<String> sharedLocations;
    private Integer overlapDays;
    
    // Why this pool is suggested
    private List<String> strengths;
    private List<String> considerations;
    
    // Pool statistics
    private Integer currentMembers;
    private Double averageGroupCompatibility;
    private String poolCreatedBy;
}
