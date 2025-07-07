package com.islandhop.reviewservice.util;

import com.islandhop.reviewservice.dto.ReviewRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    public void validateDriverReview(ReviewRequestDTO reviewRequest) {
        if (reviewRequest.getReview() == null || reviewRequest.getReview().isEmpty()) {
            throw new IllegalArgumentException("Review cannot be empty");
        }
        if (reviewRequest.getEmail() == null || reviewRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (reviewRequest.getReviewerEmail() == null || reviewRequest.getReviewerEmail().isEmpty()) {
            throw new IllegalArgumentException("Reviewer email cannot be empty");
        }
    }

    public void validateGuideReview(ReviewRequestDTO reviewRequest) {
        if (reviewRequest.getReview() == null || reviewRequest.getReview().isEmpty()) {
            throw new IllegalArgumentException("Review cannot be empty");
        }
        if (reviewRequest.getEmail() == null || reviewRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (reviewRequest.getReviewerEmail() == null || reviewRequest.getReviewerEmail().isEmpty()) {
            throw new IllegalArgumentException("Reviewer email cannot be empty");
        }
    }
}