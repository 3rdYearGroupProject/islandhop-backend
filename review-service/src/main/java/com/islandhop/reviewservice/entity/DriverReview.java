package com.islandhop.reviewservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.islandhop.reviewservice.enums.ReviewStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "email", nullable = false)
    private String email; // Driver's email

    @Column(name = "review", nullable = false, columnDefinition = "TEXT")
    private String review;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "reviewer_email", nullable = false)
    private String reviewerEmail;

    @Column(name = "reviewer_firstname", nullable = false)
    private String reviewerFirstname;

    @Column(name = "reviewer_lastname", nullable = false)
    private String reviewerLastname;

    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore;

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}