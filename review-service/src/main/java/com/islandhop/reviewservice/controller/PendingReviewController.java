package com.islandhop.reviewservice.controller;

import com.islandhop.reviewservice.entity.PendingReview;
import com.islandhop.reviewservice.service.PendingReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pending-reviews")
@RequiredArgsConstructor
public class PendingReviewController {

    private final PendingReviewService pendingReviewService;

    @GetMapping
    public ResponseEntity<List<PendingReview>> getAllPendingReviews() {
        List<PendingReview> pendingReviews = pendingReviewService.getAllPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<PendingReview>> getPendingReviewsByEmail(@PathVariable String email) {
        List<PendingReview> pendingReviews = pendingReviewService.getPendingReviewsByEmail(email);
        return ResponseEntity.ok(pendingReviews);
    }

    @PutMapping("/{reviewId}/status")
    public ResponseEntity<String> changeReviewStatus(@PathVariable Long reviewId, @RequestParam String status) {
        pendingReviewService.changeReviewStatus(reviewId, status);
        return ResponseEntity.ok("Review status updated successfully");
    }

    @PostMapping
    public ResponseEntity<String> addPendingReview(@RequestBody PendingReview pendingReview) {
        pendingReviewService.addPendingReview(pendingReview);
        return ResponseEntity.ok("Pending review added successfully");
    }
}