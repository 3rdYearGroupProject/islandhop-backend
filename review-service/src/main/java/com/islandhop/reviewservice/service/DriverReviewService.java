package com.islandhop.reviewservice.service;

import com.islandhop.reviewservice.dto.ReviewRequest;
import com.islandhop.reviewservice.dto.ReviewResponse;
import com.islandhop.reviewservice.entity.DriverReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.repository.DriverReviewRepository;
import com.islandhop.reviewservice.util.ReviewValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverReviewService {

    @Autowired
    private DriverReviewRepository driverReviewRepository;

    @Autowired
    private ReviewValidator reviewValidator;

    public ReviewResponse submitReview(ReviewRequest reviewRequest) {
        reviewValidator.validateReviewRequest(reviewRequest);
        
        DriverReview driverReview = new DriverReview();
        driverReview.setDriverEmail(reviewRequest.getDriverEmail());
        driverReview.setReview(reviewRequest.getReview());
        driverReview.setRating(reviewRequest.getRating());
        driverReview.setStatus(ReviewStatus.PENDING);
        
        DriverReview savedReview = driverReviewRepository.save(driverReview);
        
        return new ReviewResponse(savedReview.getId(), savedReview.getDriverEmail(), savedReview.getReview(), savedReview.getRating(), savedReview.getStatus());
    }

    public List<DriverReview> getReviewsByDriverEmail(String driverEmail) {
        return driverReviewRepository.findByDriverEmail(driverEmail);
    }

    public void changeReviewStatus(Long reviewId, ReviewStatus status) {
        Optional<DriverReview> reviewOptional = driverReviewRepository.findById(reviewId);
        if (reviewOptional.isPresent()) {
            DriverReview review = reviewOptional.get();
            review.setStatus(status);
            driverReviewRepository.save(review);
        }
    }
}