package com.islandhop.userservices.service;

public interface SupportService {
    String getEmailFromIdToken(String idToken);
    boolean isSupport(String email);
}