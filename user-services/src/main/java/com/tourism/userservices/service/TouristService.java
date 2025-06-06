package com.tourism.userservices.service;

import com.tourism.userservices.dto.RegisterTouristRequest;
import com.tourism.userservices.dto.UpdateTouristRequest;
import com.tourism.userservices.entity.Tourist;
import com.tourism.userservices.exception.CustomException;
import com.tourism.userservices.repository.TouristRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TouristService {

    @Autowired
    private TouristRepository touristRepository;

    @Transactional
    public Tourist registerTourist(RegisterTouristRequest request) {
        Tourist tourist = new Tourist();
        tourist.setFirebaseUid(request.getFirebaseUid());
        tourist.setEmail(request.getEmail());
        tourist.setName(request.getName());
        tourist.setNationality(request.getNationality());
        tourist.setLanguages(request.getLanguages());
        tourist.setDateOfBirth(request.getDateOfBirth());
        tourist.setStatus(Tourist.Status.ACTIVE);
        tourist.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        tourist.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return touristRepository.save(tourist);
    }

    @Transactional(readOnly = true)
    public Optional<Tourist> getTouristById(UUID id) {
        return touristRepository.findById(id);
    }

    @Transactional
    public Tourist updateTourist(UUID id, UpdateTouristRequest request) {
        Tourist tourist = touristRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tourist not found"));
        if (request.getName() != null) {
            tourist.setName(request.getName());
        }
        if (request.getNationality() != null) {
            tourist.setNationality(request.getNationality());
        }
        if (request.getLanguages() != null) {
            tourist.setLanguages(request.getLanguages());
        }
        tourist.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return touristRepository.save(tourist);
    }

    @Transactional
    public void deactivateTourist(UUID id) {
        Tourist tourist = touristRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tourist not found"));
        tourist.setStatus(Tourist.Status.DEACTIVATED);
        tourist.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        touristRepository.save(tourist);
    }

    @Transactional
    public void deleteTourist(UUID id) {
        Tourist tourist = touristRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tourist not found"));
        tourist.setStatus(Tourist.Status.DELETED);
        tourist.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        touristRepository.save(tourist);
    }
}