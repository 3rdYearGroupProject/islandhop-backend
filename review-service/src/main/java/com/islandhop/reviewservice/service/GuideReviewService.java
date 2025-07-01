package com.islandhop.reviewservice.service;

import com.islandhop.reviewservice.dto.ReviewRequest;
import com.islandhop.reviewservice.dto.ReviewResponse;
import com.islandhop.reviewservice.entity.GuideReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import com.islandhop.reviewservice.repository.GuideReviewRepository;
import com.islandhop.reviewservice.util.ReviewValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuideReviewService {

    @Autowired
    private GuideReviewRepository guideReviewRepository;

    @Autowired
    private ReviewValidator reviewValidator;

    public ReviewResponse submitReview(ReviewRequest reviewRequest) {
        reviewValidator.validateReviewRequest(reviewRequest);

        GuideReview guideReview = new GuideReview();
        guideReview.setGuideEmail(reviewRequest.getGuideEmail());
        guideReview.setReview(reviewRequest.getReview());
        guideReview.setRate(reviewRequest.getRate());
        guideReview.setStatus(ReviewStatus.PENDING);

        GuideReview savedReview = guideReviewRepository.save(guideReview);
        return new ReviewResponse(savedReview.getId(), savedReview.getGuideEmail(), savedReview.getReview(), savedReview.getRate(), savedReview.getStatus());
    }

    public List<GuideReview> getReviewsByGuideEmail(String guideEmail) {
        return guideReviewRepository.findByGuideEmail(guideEmail);
    }

    public void changeReviewStatus(Long reviewId, ReviewStatus status) {
        Optional<GuideReview> reviewOptional = guideReviewRepository.findById(reviewId);
        if (reviewOptional.isPresent()) {
            GuideReview guideReview = reviewOptional.get();
            guideReview.setStatus(status);
            guideReviewRepository.save(guideReview);
        }
    }
}