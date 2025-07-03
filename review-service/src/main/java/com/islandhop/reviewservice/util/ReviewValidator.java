package com.islandhop.reviewservice.util;

import com.islandhop.reviewservice.dto.ReviewRequest;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    public void validateDriverReview(ReviewRequest reviewRequest) {
        if (reviewRequest.getReview() == null || reviewRequest.getReview().isEmpty()) {
            throw new IllegalArgumentException("Review cannot be empty");
        }
        if (reviewRequest.getRating() < 0 || reviewRequest.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
    }

    public void validateGuideReview(ReviewRequest reviewRequest) {
        if (reviewRequest.getReview() == null || reviewRequest.getReview().isEmpty()) {
            throw new IllegalArgumentException("Review cannot be empty");
        }
        if (reviewRequest.getRating() < 0 || reviewRequest.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
    }
}