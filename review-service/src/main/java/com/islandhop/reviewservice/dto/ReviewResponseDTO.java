package com.islandhop.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.islandhop.reviewservice.enums.ReviewStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private Long reviewId;
    private String email;
    private String review;
    private ReviewStatus status;
    private String reviewerEmail;
    private String reviewerFirstname;
    private String reviewerLastname;
    private Double aiConfidenceScore;
    private String aiAnalysis;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
