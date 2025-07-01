package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.dto.ReviewRequest;
import com.islandhop.reviewservice.dto.ReviewResponse;
import com.islandhop.reviewservice.entity.GuideReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.service.GuideReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/guides")
public class GuideReviewController {

    private final GuideReviewService guideReviewService;

    public GuideReviewController(GuideReviewService guideReviewService) {
        this.guideReviewService = guideReviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@RequestBody ReviewRequest reviewRequest) {
        GuideReview guideReview = guideReviewService.submitReview(reviewRequest);
        return ResponseEntity.ok(new ReviewResponse(guideReview.getId(), guideReview.getEmail(), guideReview.getReview(), guideReview.getRating(), guideReview.getStatus()));
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEmail(@PathVariable String email) {
        List<ReviewResponse> reviews = guideReviewService.getReviewsByEmail(email);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}/status")
    public ResponseEntity<Void> updateReviewStatus(@PathVariable Long reviewId, @RequestParam ReviewStatus status) {
        guideReviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.noContent().build();
    }
}