package com.islandhop.userservices.service;

import com.islandhop.userservices.model.SupportProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SupportService {
    SupportProfile getProfileByEmail(String email);
    SupportProfile updateProfile(Map<String, String> request);
    boolean changeAccountStatus(String email, String status);
    SupportProfile createProfile(Map<String, String> request);
    /**
     * Upload profile photo for support user
     * @param email Support user email
     * @param photo MultipartFile containing the photo
     * @return URL of uploaded photo or null if failed
     */
    String uploadProfilePhoto(String email, MultipartFile photo);
}