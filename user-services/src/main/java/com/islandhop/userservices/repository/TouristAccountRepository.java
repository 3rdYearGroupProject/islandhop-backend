package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.TouristAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TouristAccountRepository extends JpaRepository<TouristAccount, UUID> {
    Optional<TouristAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}