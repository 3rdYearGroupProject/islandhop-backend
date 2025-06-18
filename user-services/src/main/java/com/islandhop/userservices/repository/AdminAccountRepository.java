package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, UUID> {
    Optional<AdminAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}