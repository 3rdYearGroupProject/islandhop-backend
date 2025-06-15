package com.islandhop.userservices.service.impl;

import com.islandhop.userservices.dto.TouristRegistrationRequest;
import com.islandhop.userservices.exception.ResourceNotFoundException;
import com.islandhop.userservices.model.Tourist;
import com.islandhop.userservices.model.TouristStatus;
import com.islandhop.userservices.repository.TouristRepository;
import com.islandhop.userservices.service.TouristService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TouristServiceImpl implements TouristService {

    private final TouristRepository touristRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_MINUTES = 10;

    @Override
    @Transactional
    public Tourist registerTourist(String firebaseUid, TouristRegistrationRequest request) {
        if (touristRepository.existsByFirebaseUid(firebaseUid)) {
            throw new IllegalArgumentException("Tourist already registered with this Firebase UID");
        }
        if (touristRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Tourist tourist = new Tourist();
        tourist.setFirebaseUid(firebaseUid);
        tourist.setEmail(request.getEmail());
        tourist.setName(request.getName());
        tourist.setDateOfBirth(request.getDateOfBirth());
        tourist.setNationality(request.getNationality());
        tourist.setLanguages(request.getLanguages());
        tourist.setStatus(TouristStatus.ACTIVE);

        return touristRepository.save(tourist);
    }

    @Override
    public Tourist getTouristByFirebaseUid(String firebaseUid) {
        return touristRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
    }

    @Override
    @Transactional
    public Tourist updateTourist(String firebaseUid, TouristRegistrationRequest request) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);

        if (!tourist.getEmail().equals(request.getEmail()) &&
                touristRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        tourist.setName(request.getName());
        tourist.setEmail(request.getEmail());
        tourist.setDateOfBirth(request.getDateOfBirth());
        tourist.setNationality(request.getNationality());
        tourist.setLanguages(request.getLanguages());

        return touristRepository.save(tourist);
    }

    @Override
    @Transactional
    public void deactivateTourist(String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        tourist.setStatus(TouristStatus.DEACTIVATED);
        touristRepository.save(tourist);
    }

    @Override
    @Transactional
    public void deleteTourist(String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        tourist.setStatus(TouristStatus.DELETED);
        touristRepository.save(tourist);
    }

    @Override
    public String generateAndSendOTP(String firebaseUid) {
        Tourist tourist = getTouristByFirebaseUid(firebaseUid);
        String otp = generateOTP();

        // Store OTP in Redis with TTL
        String key = OTP_PREFIX + firebaseUid;
        redisTemplate.opsForValue().set(key, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        // TODO: Send OTP via AWS SNS
        // For now, just return the OTP for testing
        return otp;
    }

    @Override
    public boolean verifyOTP(String firebaseUid, String otp) {
        String key = OTP_PREFIX + firebaseUid;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public String verifyFirebaseIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid Firebase ID token", e);
        }
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}