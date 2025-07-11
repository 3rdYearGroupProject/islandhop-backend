package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.dto.ReviewRequestDTO;
import com.islandhop.reviewservice.dto.ReviewResponseDTO;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.service.DriverReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews/drivers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DriverReviewController {

    private final DriverReviewService driverReviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> submitDriverReview(@Valid @RequestBody ReviewRequestDTO request) {
        log.info("[API] Received driver review submission: reviewerEmail={}, driverEmail={}, comment={}",
                request.getReviewerEmail(), request.getEmail(), request.getReview());
        ReviewResponseDTO response = driverReviewService.submitDriverReview(request);
        log.info("[API] Review submission result: reviewId={}, status={}, aiConfidenceScore={}, aiAnalysis={}",
                response.getReviewId(), response.getStatus(), response.getAiConfidenceScore(), response.getAiAnalysis());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/driver/{email}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByDriverEmail(@PathVariable String email) {
        log.info("[API] Fetching reviews for driver email={}", email);
        List<ReviewResponseDTO> reviews = driverReviewService.getDriverReviewsByEmail(email);
        log.info("[API] Found {} reviews for driver email={}", reviews.size(), email);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByStatus(@PathVariable int status) {
        ReviewStatus reviewStatus = ReviewStatus.fromValue(status);
        log.info("[API] Fetching reviews by status={}", reviewStatus);
        List<ReviewResponseDTO> reviews = driverReviewService.getDriverReviewsByStatus(reviewStatus);
        log.info("[API] Found {} reviews with status={}", reviews.size(), reviewStatus);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReviewResponseDTO>> getPendingReviews() {
        log.info("[API] Fetching pending driver reviews");
        List<ReviewResponseDTO> reviews = driverReviewService.getPendingDriverReviews();
        log.info("[API] Found {} pending driver reviews", reviews.size());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/support")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForSupport() {
        log.info("[API] Fetching reviews for support agents");
        List<ReviewResponseDTO> reviews = driverReviewService.getReviewsForSupportAgents();
        log.info("[API] Found {} reviews for support agents", reviews.size());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long reviewId) {
        log.info("[API] Fetching driver review by ID={}", reviewId);
        ReviewResponseDTO review = driverReviewService.getDriverReviewById(reviewId);
        log.info("[API] Found review: {}", review);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}/status")
    public ResponseEntity<ReviewResponseDTO> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestParam int status) {
        ReviewStatus newStatus = ReviewStatus.fromValue(status);
        log.info("[API] Updating review status: reviewId={}, newStatus={}", reviewId, newStatus);
        ReviewResponseDTO updatedReview = driverReviewService.updateReviewStatus(reviewId, newStatus);
        log.info("[API] Updated review: reviewId={}, status={}", updatedReview.getReviewId(), updatedReview.getStatus());
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/low-confidence")
    public ResponseEntity<List<ReviewResponseDTO>> getLowConfidenceReviews() {
        log.info("[API] Fetching reviews with AI confidence below threshold");
        List<ReviewResponseDTO> reviews = driverReviewService.getLowConfidenceReviews();
        log.info("[API] Found {} reviews with low AI confidence", reviews.size());
        return ResponseEntity.ok(reviews);
    }
}