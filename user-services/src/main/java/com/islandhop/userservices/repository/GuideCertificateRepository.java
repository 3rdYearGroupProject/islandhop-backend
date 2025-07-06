package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideCertificateRepository extends JpaRepository<GuideCertificate, UUID> {
    List<GuideCertificate> findByEmail(String email);
    void deleteByEmail(String email);
}
