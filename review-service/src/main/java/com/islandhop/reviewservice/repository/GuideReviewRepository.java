package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.GuideReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideReviewRepository extends JpaRepository<GuideReview, Long> {
    
    List<GuideReview> findByEmail(String email);
    
    List<GuideReview> findByStatus(ReviewStatus status);
    
    List<GuideReview> findByReviewerEmail(String reviewerEmail);
    
    @Query("SELECT gr FROM GuideReview gr WHERE gr.status = :status ORDER BY gr.createdAt DESC")
    List<GuideReview> findByStatusOrderByCreatedAtDesc(@Param("status") ReviewStatus status);
    
    @Query("SELECT gr FROM GuideReview gr WHERE gr.email = :email AND gr.status = :status")
    List<GuideReview> findByEmailAndStatus(@Param("email") String email, @Param("status") ReviewStatus status);
}