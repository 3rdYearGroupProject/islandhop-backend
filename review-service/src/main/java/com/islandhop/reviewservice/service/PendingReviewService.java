package com.islandhop.reviewservice.service;

import com.islandhop.reviewservice.entity.PendingReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.repository.PendingReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PendingReviewService {

    @Autowired
    private PendingReviewRepository pendingReviewRepository;

    public List<PendingReview> getAllPendingReviews() {
        return pendingReviewRepository.findByStatus(ReviewStatus.PENDING);
    }

    public Optional<PendingReview> getPendingReviewById(Long reviewId) {
        return pendingReviewRepository.findById(reviewId);
    }

    public PendingReview changeReviewStatus(Long reviewId, ReviewStatus status, String approvedBy) {
        PendingReview review = pendingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(status);
        review.setApprovedBy(approvedBy);
        return pendingReviewRepository.save(review);
    }

    public void updateMLModelRange(double newRange) {
        // Logic to update the ML model's detection range
    }
}