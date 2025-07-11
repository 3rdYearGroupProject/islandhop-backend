package com.islandhop.reviewservice.service;

import com.islandhop.reviewservice.dto.AIAnalysisResult;
import com.islandhop.reviewservice.dto.ReviewRequestDTO;
import com.islandhop.reviewservice.dto.ReviewResponseDTO;
import com.islandhop.reviewservice.entity.GuideReview;
import com.islandhop.reviewservice.entity.PendingReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.repository.GuideReviewRepository;
import com.islandhop.reviewservice.repository.PendingReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuideReviewService {

    private final GuideReviewRepository guideReviewRepository;
    private final PendingReviewRepository pendingReviewRepository;
    private final GeminiAIService geminiAIService;
    private final ConfigurationService configurationService;

    @Transactional
    public ReviewResponseDTO submitGuideReview(ReviewRequestDTO request) {
        log.info("Submitting guide review for email: {}", request.getEmail());

        // Analyze review with AI
        double confidenceThreshold = configurationService.getConfidenceThreshold();
        AIAnalysisResult aiResult = geminiAIService.analyzeReview(request.getReview(), confidenceThreshold);

        // If AI confidence is below 0.95, add to pending_reviews
        if (aiResult.getConfidenceScore() < 0.95) {
            PendingReview pending = new PendingReview();
            pending.setReview(request.getReview());
            pending.setApproved(0);
            pending.setApprovedBy("");
            pending.setStatus(ReviewStatus.PENDING);
            pending.setSource("guide");
            pending.setReviewerEmail(request.getReviewerEmail());
            pendingReviewRepository.save(pending);
            log.info("Review added to pending_reviews due to low AI confidence: {}", aiResult.getConfidenceScore());
        }

        // Create and save guide review
        GuideReview review = GuideReview.builder()
                .email(request.getEmail())
                .review(request.getReview())
                .reviewerEmail(request.getReviewerEmail())
                .reviewerFirstname(request.getReviewerFirstname())
                .reviewerLastname(request.getReviewerLastname())
                .rating(request.getRating())
                .status(aiResult.getRecommendedStatus())
                .aiConfidenceScore(aiResult.getConfidenceScore())
                .aiAnalysis(aiResult.getAnalysis())
                .build();

        GuideReview savedReview = guideReviewRepository.save(review);
        
        log.info("Guide review saved with ID: {} and status: {}", 
                savedReview.getReviewId(), savedReview.getStatus());

        return mapToResponseDTO(savedReview);
    }

    public List<ReviewResponseDTO> getGuideReviewsByEmail(String email) {
        return guideReviewRepository.findByEmail(email)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getGuideReviewsByStatus(ReviewStatus status) {
        return guideReviewRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getPendingGuideReviews() {
        return getGuideReviewsByStatus(ReviewStatus.PENDING);
    }

    public List<ReviewResponseDTO> getReviewsForSupportAgents() {
        return getGuideReviewsByStatus(ReviewStatus.TO_SUPPORT_AGENTS);
    }

    @Transactional
    public ReviewResponseDTO updateReviewStatus(Long reviewId, ReviewStatus newStatus) {
        GuideReview review = guideReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Guide review not found with ID: " + reviewId));

        log.info("Updating guide review {} status from {} to {}", 
                reviewId, review.getStatus(), newStatus);

        review.setStatus(newStatus);
        GuideReview updatedReview = guideReviewRepository.save(review);

        return mapToResponseDTO(updatedReview);
    }

    public ReviewResponseDTO getGuideReviewById(Long reviewId) {
        GuideReview review = guideReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Guide review not found with ID: " + reviewId));
        return mapToResponseDTO(review);
    }

    @Transactional
    public List<ReviewResponseDTO> getLowConfidenceReviews() {
        log.info("Fetching guide reviews with AI confidence below threshold using stored procedure");
        List<GuideReview> reviews = guideReviewRepository.findLowConfidenceReviews();
        log.info("Found {} guide reviews with low AI confidence", reviews.size());
        return reviews.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToResponseDTO(GuideReview review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .email(review.getEmail())
                .review(review.getReview())
                .status(review.getStatus())
                .reviewerEmail(review.getReviewerEmail())
                .reviewerFirstname(review.getReviewerFirstname())
                .reviewerLastname(review.getReviewerLastname())
                .rating(review.getRating())
                .aiConfidenceScore(review.getAiConfidenceScore())
                .aiAnalysis(review.getAiAnalysis())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}