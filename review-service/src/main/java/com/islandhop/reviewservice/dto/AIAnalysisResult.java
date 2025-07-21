package com.islandhop.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.islandhop.reviewservice.enums.ReviewStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisResult {
    
    private ReviewStatus recommendedStatus;
    private Double confidenceScore;
    private String analysis;
    private boolean isConfident; // true if confidence > threshold
}
