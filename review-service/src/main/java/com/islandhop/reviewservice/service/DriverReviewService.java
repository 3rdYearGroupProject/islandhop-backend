package com.islandhop.reviewservice.service;

import com.islandhop.reviewservice.dto.AIAnalysisResult;
import com.islandhop.reviewservice.dto.ReviewRequestDTO;
import com.islandhop.reviewservice.dto.ReviewResponseDTO;
import com.islandhop.reviewservice.entity.DriverReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.repository.DriverReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverReviewService {

    private final DriverReviewRepository driverReviewRepository;
    private final GeminiAIService geminiAIService;
    private final ConfigurationService configurationService;

    @Transactional
    public ReviewResponseDTO submitDriverReview(ReviewRequestDTO request) {
        log.info("Submitting driver review for email: {}", request.getEmail());

        // Analyze review with AI
        double confidenceThreshold = configurationService.getConfidenceThreshold();
        AIAnalysisResult aiResult = geminiAIService.analyzeReview(request.getReview(), confidenceThreshold);

        // Create and save driver review
        DriverReview review = DriverReview.builder()
                .email(request.getEmail())
                .review(request.getReview())
                .reviewerEmail(request.getReviewerEmail())
                .reviewerFirstname(request.getReviewerFirstname())
                .reviewerLastname(request.getReviewerLastname())
                .status(aiResult.getRecommendedStatus())
                .aiConfidenceScore(aiResult.getConfidenceScore())
                .aiAnalysis(aiResult.getAnalysis())
                .build();

        DriverReview savedReview = driverReviewRepository.save(review);
        
        log.info("Driver review saved with ID: {} and status: {}", 
                savedReview.getReviewId(), savedReview.getStatus());

        return mapToResponseDTO(savedReview);
    }

    public List<ReviewResponseDTO> getDriverReviewsByEmail(String email) {
        return driverReviewRepository.findByEmail(email)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getDriverReviewsByStatus(ReviewStatus status) {
        return driverReviewRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getPendingDriverReviews() {
        return getDriverReviewsByStatus(ReviewStatus.PENDING);
    }

    public List<ReviewResponseDTO> getReviewsForSupportAgents() {
        return getDriverReviewsByStatus(ReviewStatus.TO_SUPPORT_AGENTS);
    }

    @Transactional
    public ReviewResponseDTO updateReviewStatus(Long reviewId, ReviewStatus newStatus) {
        DriverReview review = driverReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Driver review not found with ID: " + reviewId));

        log.info("Updating driver review {} status from {} to {}", 
                reviewId, review.getStatus(), newStatus);

        review.setStatus(newStatus);
        DriverReview updatedReview = driverReviewRepository.save(review);

        return mapToResponseDTO(updatedReview);
    }

    public ReviewResponseDTO getDriverReviewById(Long reviewId) {
        DriverReview review = driverReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Driver review not found with ID: " + reviewId));
        return mapToResponseDTO(review);
    }

    private ReviewResponseDTO mapToResponseDTO(DriverReview review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .email(review.getEmail())
                .review(review.getReview())
                .status(review.getStatus())
                .reviewerEmail(review.getReviewerEmail())
                .reviewerFirstname(review.getReviewerFirstname())
                .reviewerLastname(review.getReviewerLastname())
                .aiConfidenceScore(review.getAiConfidenceScore())
                .aiAnalysis(review.getAiAnalysis())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}