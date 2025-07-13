package com.islandhop.tripinit.repository.postgresql;

import com.islandhop.tripinit.model.postgresql.GuideFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuideFeeRepository extends JpaRepository<GuideFee, String> {
    // Additional query methods can be defined here if needed
}