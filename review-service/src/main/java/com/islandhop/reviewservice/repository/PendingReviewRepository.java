package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.PendingReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingReviewRepository extends JpaRepository<PendingReview, Long> {
    List<PendingReview> findByStatus(ReviewStatus status);
}