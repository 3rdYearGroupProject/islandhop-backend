package com.tourism.userservices.service;

import com.tourism.userservices.dto.RegisterTouristRequest;
import com.tourism.userservices.dto.UpdateTouristRequest;
import com.tourism.userservices.dto.OtpVerifyRequest;
import com.tourism.userservices.entity.Tourist;

public interface TouristService {
    void registerTourist(RegisterTouristRequest request, String firebaseUid);
    Tourist getTouristByFirebaseUid(String firebaseUid);
    Tourist updateTourist(UpdateTouristRequest request, String firebaseUid);
    void deactivateAccount(String firebaseUid);
    void deleteAccount(OtpVerifyRequest otpVerifyRequest, String firebaseUid);
    void sendVerificationCode(String firebaseUid);
    void verifyOtp(OtpVerifyRequest otpVerifyRequest, String firebaseUid);
}