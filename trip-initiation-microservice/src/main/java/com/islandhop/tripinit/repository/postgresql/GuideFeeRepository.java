package com.islandhop.tripinit.repository.postgresql;

import com.islandhop.tripinit.model.postgresql.GuideFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for guide fees in PostgreSQL.
 */
@Repository
public interface GuideFeeRepository extends JpaRepository<GuideFee, Long> {
    Optional<GuideFee> findByCity(String city);
}