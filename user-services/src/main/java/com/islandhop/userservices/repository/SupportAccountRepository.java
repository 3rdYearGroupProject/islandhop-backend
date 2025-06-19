package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.SupportAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SupportAccountRepository extends JpaRepository<SupportAccount, UUID> {
    Optional<SupportAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}