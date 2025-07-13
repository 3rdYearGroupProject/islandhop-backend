package com.islandhop.tripinitiation.repository;

import com.islandhop.tripinitiation.model.postgres.GuideFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuideFeeRepository extends JpaRepository<GuideFee, String> {
    // Additional query methods can be defined here if needed
}