package com.islandhop.reviewservice.entity;

import jakarta.persistence.*;
import com.islandhop.reviewservice.enums.ReviewStatus;

@Entity
@Table(name = "pending_reviews")
public class PendingReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private String review;

    @Column(nullable = false)
    private int approved; // 0 for not approved, 1 for approved

    @Column(nullable = false)
    private String approvedBy; // Email of the approver

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(nullable = false)
    private String source; // 'driver' or 'guide'

    @Column(nullable = false)
    private String reviewerEmail;

    // Getters and Setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }
}