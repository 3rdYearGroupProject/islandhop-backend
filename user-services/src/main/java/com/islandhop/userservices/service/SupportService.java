package com.islandhop.userservices.service;

import com.islandhop.userservices.model.SupportProfile;

import java.util.Map;

public interface SupportService {
    SupportProfile getProfileByEmail(String email);
    SupportProfile updateProfile(Map<String, String> request);
    boolean changeAccountStatus(String email, String status);
}