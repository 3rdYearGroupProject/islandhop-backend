package com.islandhop.userservices.service;

import com.islandhop.userservices.dto.TouristRegistrationRequest;
import com.islandhop.userservices.model.Tourist;

public interface TouristService {
    Tourist registerTourist(String firebaseUid, TouristRegistrationRequest request);
    Tourist getTouristByFirebaseUid(String firebaseUid);
    Tourist updateTourist(String firebaseUid, TouristRegistrationRequest request);
    void deactivateTourist(String firebaseUid);
    void deleteTourist(String firebaseUid);
    String generateAndSendOTP(String firebaseUid);
    boolean verifyOTP(String firebaseUid, String otp);
    String verifyFirebaseIdToken(String idToken);
}