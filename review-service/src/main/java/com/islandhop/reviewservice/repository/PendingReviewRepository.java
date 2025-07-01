package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.PendingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingReviewRepository extends JpaRepository<PendingReview, Long> {
    // Custom query methods can be defined here if needed
}