package com.islandhop.userservices.repository;

import com.islandhop.userservices.model.GuideLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideLanguageRepository extends JpaRepository<GuideLanguage, Long> {
    List<GuideLanguage> findByEmail(String email);
    void deleteByEmail(String email);
}
