package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.GuideReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuideReviewRepository extends JpaRepository<GuideReview, Long> {
    // Custom query methods can be defined here if needed
}