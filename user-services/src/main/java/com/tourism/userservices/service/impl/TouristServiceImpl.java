package com.tourism.userservices.service.impl;

import com.tourism.userservices.dto.RegisterTouristRequest;
import com.tourism.userservices.dto.UpdateTouristRequest;
import com.tourism.userservices.dto.OtpVerifyRequest;
import com.tourism.userservices.entity.Tourist;
import com.tourism.userservices.exception.CustomException;
import com.tourism.userservices.repository.TouristRepository;
import com.tourism.userservices.service.TouristService;
import com.tourism.userservices.service.OtpService;
import com.tourism.userservices.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TouristServiceImpl implements TouristService {

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Override
    public void registerTourist(RegisterTouristRequest request, String firebaseUid) {
        Tourist tourist = new Tourist();
        tourist.setFirebaseUid(firebaseUid);
        tourist.setEmail(request.getEmail());
        tourist.setName(request.getName());
        tourist.setNationality(request.getNationality());
        tourist.setLanguages(request.getLanguages());
        try {
            Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(request.getDateOfBirth());
            tourist.setDateOfBirth(dob);
        } catch (Exception e) {
            throw new CustomException("Invalid date format for dateOfBirth", 400);
        }
        tourist.setStatus(Tourist.Status.ACTIVE);
        tourist.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        tourist.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        touristRepository.save(tourist);
    }

    @Override
    public Tourist getTouristByFirebaseUid(String firebaseUid) {
        return touristRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new CustomException("Tourist not found", 404));
    }

    @Override
    public Tourist updateTourist(UpdateTouristRequest request, String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        if (request.getName() != null) tourist.setName(request.getName());
        if (request.getNationality() != null) tourist.setNationality(request.getNationality());
        if (request.getLanguages() != null) tourist.setLanguages(request.getLanguages());
        tourist.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return touristRepository.save(tourist);
    }

    @Override
    public void deactivateAccount(String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        tourist.setStatus(Tourist.Status.DEACTIVATED);
        tourist.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        touristRepository.save(tourist);
        emailService.sendVerificationEmail(tourist.getEmail(), "Your account has been deactivated.");
    }

    @Override
    public void deleteAccount(OtpVerifyRequest otpVerifyRequest, String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        boolean valid = otpService.validateOtp(firebaseUid, otpVerifyRequest.getOtp());
        if (!valid) throw new CustomException("Invalid OTP", 400);
        tourist.setStatus(Tourist.Status.DELETED);
        tourist.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        touristRepository.save(tourist);
    }

    @Override
    public void sendVerificationCode(String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        otpService.sendOtp(tourist.getEmail(), firebaseUid);
    }

    @Override
    public void verifyOtp(OtpVerifyRequest otpVerifyRequest, String firebaseUid) {
        boolean valid = otpService.validateOtp(firebaseUid, otpVerifyRequest.getOtp());
        if (!valid) throw new CustomException("Invalid OTP", 400);
    }
}