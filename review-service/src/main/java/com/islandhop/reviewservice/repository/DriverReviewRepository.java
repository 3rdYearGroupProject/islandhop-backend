package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.DriverReview;
import com.islandhop.reviewservice.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverReviewRepository extends JpaRepository<DriverReview, Long> {
    
    List<DriverReview> findByEmail(String email);
    
    List<DriverReview> findByStatus(ReviewStatus status);
    
    List<DriverReview> findByReviewerEmail(String reviewerEmail);
    
    @Query("SELECT dr FROM DriverReview dr WHERE dr.status = :status ORDER BY dr.createdAt DESC")
    List<DriverReview> findByStatusOrderByCreatedAtDesc(@Param("status") ReviewStatus status);
    
    @Query("SELECT dr FROM DriverReview dr WHERE dr.email = :email AND dr.status = :status")
    List<DriverReview> findByEmailAndStatus(@Param("email") String email, @Param("status") ReviewStatus status);
}