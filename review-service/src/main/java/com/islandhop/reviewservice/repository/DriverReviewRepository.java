package com.islandhop.reviewservice.repository;

import com.islandhop.reviewservice.entity.DriverReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverReviewRepository extends JpaRepository<DriverReview, Long> {
    // Custom query methods can be defined here if needed
    DriverReview findByDriverEmail(String driverEmail);
}