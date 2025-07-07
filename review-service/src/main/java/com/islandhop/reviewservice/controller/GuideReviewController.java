package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.dto.ReviewRequestDTO;
import com.islandhop.reviewservice.dto.ReviewResponseDTO;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.service.GuideReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews/guides")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GuideReviewController {

    private final GuideReviewService guideReviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> submitGuideReview(@Valid @RequestBody ReviewRequestDTO request) {
        log.info("Received guide review submission for: {}", request.getEmail());
        ReviewResponseDTO response = guideReviewService.submitGuideReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/guide/{email}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByGuideEmail(@PathVariable String email) {
        List<ReviewResponseDTO> reviews = guideReviewService.getGuideReviewsByEmail(email);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByStatus(@PathVariable int status) {
        ReviewStatus reviewStatus = ReviewStatus.fromValue(status);
        List<ReviewResponseDTO> reviews = guideReviewService.getGuideReviewsByStatus(reviewStatus);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReviewResponseDTO>> getPendingReviews() {
        List<ReviewResponseDTO> reviews = guideReviewService.getPendingGuideReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/support")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForSupport() {
        List<ReviewResponseDTO> reviews = guideReviewService.getReviewsForSupportAgents();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDTO review = guideReviewService.getGuideReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}/status")
    public ResponseEntity<ReviewResponseDTO> updateReviewStatus(
            @PathVariable Long reviewId, 
            @RequestParam int status) {
        ReviewStatus newStatus = ReviewStatus.fromValue(status);
        ReviewResponseDTO updatedReview = guideReviewService.updateReviewStatus(reviewId, newStatus);
        return ResponseEntity.ok(updatedReview);
    }
}