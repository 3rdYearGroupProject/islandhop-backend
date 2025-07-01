package com.islandhop.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private String reviewerEmail;
    private String review;
    private int rating; // 0-5 scale
    private String status; // approved, banned, pending
}