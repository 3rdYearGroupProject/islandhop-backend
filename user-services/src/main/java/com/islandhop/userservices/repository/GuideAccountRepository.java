package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GuideAccountRepository extends JpaRepository<GuideAccount, UUID> {
    boolean existsByEmail(String email);
}