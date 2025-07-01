package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.dto.ReviewRequest;
import com.islandhop.reviewservice.dto.ReviewResponse;
import com.islandhop.reviewservice.entity.DriverReview;
import com.islandhop.reviewservice.service.DriverReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/drivers")
public class DriverReviewController {

    private final DriverReviewService driverReviewService;

    public DriverReviewController(DriverReviewService driverReviewService) {
        this.driverReviewService = driverReviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@RequestBody ReviewRequest reviewRequest) {
        DriverReview review = driverReviewService.submitReview(reviewRequest);
        return ResponseEntity.ok(new ReviewResponse(review));
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByDriverEmail(@PathVariable String email) {
        List<ReviewResponse> reviews = driverReviewService.getReviewsByDriverEmail(email);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/update-status/{reviewId}")
    public ResponseEntity<Void> updateReviewStatus(@PathVariable Long reviewId, @RequestParam String status) {
        driverReviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.noContent().build();
    }
}