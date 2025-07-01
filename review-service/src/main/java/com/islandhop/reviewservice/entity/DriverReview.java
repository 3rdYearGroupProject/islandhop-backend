package com.islandhop.reviewservice.entity;

import jakarta.persistence.*;
import com.islandhop.reviewservice.enums.ReviewStatus;

@Entity
@Table(name = "driver_reviews")
public class DriverReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "driver_email", nullable = false)
    private String driverEmail;

    @Column(name = "review", nullable = false)
    private String review;

    @Column(name = "rating", nullable = false)
    private int rating; // Rating on a scale of 0-5

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status; // Status can be APPROVED, BANNED, PENDING

    // Getters and Setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
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