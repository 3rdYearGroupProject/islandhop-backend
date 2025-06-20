package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.DriverAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DriverAccountRepository extends JpaRepository<DriverAccount, UUID> {
    boolean existsByEmail(String email);
}