package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideAccountRepository extends JpaRepository<GuideAccount, UUID> {
    GuideAccount findByEmail(String email);
    boolean existsByEmail(String email);
    List<GuideAccount> findByStatus(String status);
}