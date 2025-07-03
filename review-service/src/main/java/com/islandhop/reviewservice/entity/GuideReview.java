package com.islandhop.reviewservice.entity;

import jakarta.persistence.*;
import com.islandhop.reviewservice.enums.ReviewStatus;

@Entity
@Table(name = "guide_reviews")
public class GuideReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private String guideEmail;

    @Column(nullable = false)
    private String review;

    @Column(nullable = false)
    private int rating; // Rating on a scale of 0-5

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status; // Status can be APPROVED, BANNED, PENDING

    // Getters and Setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getGuideEmail() {
        return guideEmail;
    }

    public void setGuideEmail(String guideEmail) {
        this.guideEmail = guideEmail;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }
}