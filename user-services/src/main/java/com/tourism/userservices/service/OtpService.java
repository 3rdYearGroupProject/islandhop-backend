package com.tourism.userservices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    @Autowired
    public OtpService(RedisTemplate<String, String> redisTemplate, EmailService emailService) {
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    public String generateOtp(String userId) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(userId + ":otp", otp, 10, TimeUnit.MINUTES);
        return otp;
    }

    public boolean validateOtp(String userId, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(userId + ":otp");
        return otp.equals(storedOtp);
    }

    public void sendOtp(String email, String userId) throws MessagingException {
        String otp = generateOtp(userId);
        emailService.sendOtpEmail(email, otp);
    }
}